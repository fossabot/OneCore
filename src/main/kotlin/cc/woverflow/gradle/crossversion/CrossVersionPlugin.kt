package cc.woverflow.gradle.crossversion

import net.fabricmc.mapping.tree.TinyMappingFactory
import org.cadixdev.lorenz.MappingSet
import org.cadixdev.lorenz.io.MappingFormats
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.AbstractCompile

import org.gradle.kotlin.dsl.*
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.stream.Collectors

class CrossVersionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val parent = project.parent
        if (parent == null) {
            project.apply<RootCrossVersionPlugin>()
            return
        }

        project.evaluationDependsOn(parent.path)
        val rootExtension = parent.extensions.getByType<RootCrossVersionExtension>()
        val graph = rootExtension.rootNode ?: throw IllegalStateException("CrossVersion graph was not configured.")
        val projectNode = graph.findNode(project.name) ?: throw IllegalStateException("CrossVersion graph does not contain ${project.name}.")

        val coreProjectFile = project.file("../mainProject")
        val coreProject = coreProjectFile.readText().trim()
        val mcVersion = projectNode.mcVersion
        project.extra["mcVersion"] = mcVersion
        val ext = project.extensions.create("crossversion", CrossVersionExtension::class, project.objects, mcVersion)

        val kotlin = project.plugins.hasPlugin("kotlin")

        if (coreProject == project.name) {
            project.the<SourceSetContainer>().configureEach {
                java.setSrcDirs(listOf(parent.file("src/$name/java")))
                resources.setSrcDirs(listOf(parent.file("src/$name/resources")))
                if (kotlin) {
                    withGroovyBuilder { getProperty("kotlin") as SourceDirectorySet }.setSrcDirs(listOf(
                            parent.file("src/$name/kotlin"),
                            parent.file("src/$name/java")
                    ))
                }
            }
        } else {
            val inheritedLink = projectNode.links.find { it.first.findNode(coreProject) != null }
            val (inheritedNode, extraMappings) = inheritedLink ?: graph.findParent(projectNode)!!
            val (mappingFile, mappingFileInverted) = extraMappings
            val reverseMappings = (inheritedLink != null) != mappingFileInverted
            val inherited = parent.evaluationDependsOn(inheritedNode.project)

            project.the<SourceSetContainer>().configureEach {
                val inheritedSourceSet = inherited.the<SourceSetContainer>()[name]
                val cName = if (name == "main") "" else name.capitalize()
                val overwritesKotlin = project.file("src/$name/kotlin").also { it.mkdirs() }
                val overwritesJava = project.file("src/$name/java").also { it.mkdirs() }
                val overwriteResources = project.file("src/$name/resources").also { it.mkdirs() }
                val crossversionedRoot = project.buildDir.resolve("crossversioned/$name")

                val crossversionJava = project.tasks.register<CrossVersionTask>("CrossVersion${cName}Java") {
                    inherited.tasks.findByPath("crossversion${cName}Java")?.let { dependsOn(it) }
                    source = inherited.files(inheritedSourceSet.java.srcDirs)
                    overwrites = overwritesJava
                    generated = crossversionedRoot.resolve("java")
                    compileTask(inherited.tasks["compile${cName}Java"] as AbstractCompile)
                    if (kotlin) {
                        compileTask(inherited.tasks["compile${cName}Kotlin"] as AbstractCompile)
                    }
                    remappedClasspath = (project.tasks["compile${cName}${if (kotlin) "Kotlin" else "Java"}"] as AbstractCompile).classpath
                    mapping = mappingFile
                    reverseMapping = reverseMappings
                    vars.convention(ext.vars)
                    keywords.convention(ext.keywords)
                    patternAnnotation.convention(ext.patternAnnotation)
                }
                val sourceJavaTask = project.tasks.findByName("source${name.capitalize()}Java")
                (sourceJavaTask ?: project.tasks["compile${cName}Java"]).dependsOn(crossversionJava)
                java.setSrcDirs(listOf(overwritesJava, crossversionJava.map { it.generated!! }))

                if (kotlin) {
                    val crossversionKotlin = project.tasks.register<CrossVersionTask>("crossversion${cName}Kotlin") {
                        inherited.tasks.findByPath("crossversion${cName}Kotlin")?.let { dependsOn(it) }
                        source =
                            inherited.files(inheritedSourceSet.withGroovyBuilder { getProperty("kotlin") as SourceDirectorySet }.srcDirs.filter {
                                it.endsWith("kotlin")
                            })
                        overwrites = overwritesKotlin
                        generated = crossversionedRoot.resolve("kotlin")
                        compileTask(inherited.tasks["compile${cName}Java"] as AbstractCompile)
                        compileTask(inherited.tasks["compile${cName}Kotlin"] as AbstractCompile)
                        remappedClasspath = (project.tasks["compile${cName}Kotlin"] as AbstractCompile).classpath
                        mapping = mappingFile
                        reverseMapping = reverseMappings
                        vars.convention(ext.vars)
                        keywords.convention(ext.keywords)
                        patternAnnotation.convention(ext.patternAnnotation)
                    }
                    val kotlinConsumerTask = project.tasks.findByName("source${name.capitalize()}Kotlin")
                            ?: project.tasks["compile${cName}Kotlin"]
                    kotlinConsumerTask.dependsOn(crossversionKotlin)
                    kotlinConsumerTask.dependsOn(crossversionJava)
                    withGroovyBuilder { getProperty("kotlin") as SourceDirectorySet }.setSrcDirs(
                            listOf(
                                overwritesKotlin,
                                crossversionKotlin.map { it.generated!! },
                                overwritesJava,
                                crossversionJava.map { it.generated!! },
                            ))
                }

                val crossversionResources = project.tasks.register<CrossVersionTask>("crossversion${cName}Resources") {
                    inherited.tasks.findByPath("crossversion${cName}Resources")?.let { dependsOn(it) }
                    source = inherited.files(inheritedSourceSet.resources.srcDirs)
                    overwrites = overwriteResources
                    generated = crossversionedRoot.resolve("resources")
                    vars.convention(ext.vars)
                    keywords.convention(ext.keywords)
                    patternAnnotation.convention(ext.patternAnnotation)
                }
                project.tasks["process${cName}Resources"].dependsOn(crossversionResources)
                resources.setSrcDirs(listOf(overwriteResources, crossversionResources.map { it.generated!! }))
            }

            project.afterEvaluate {
                val prepareTaskName = "prepareMappingsForCrossVersion"
                val projectIntermediaryMappings = project.intermediaryMappings
                val inheritedIntermediaryMappings = inherited.intermediaryMappings
                val projectNotchMappings = project.notchMappings
                val inheritedNotchMappings = inherited.notchMappings
                val sourceSrg = project.buildDir.resolve(prepareTaskName).resolve("source.srg")
                val destinationSrg = project.buildDir.resolve(prepareTaskName).resolve("destination.srg")
                val prepareTask = if (inheritedIntermediaryMappings != null && projectIntermediaryMappings != null
                        && inheritedIntermediaryMappings.type == projectIntermediaryMappings.type) {
                    tasks.register(prepareTaskName) {
                        bakeNamedToIntermediaryMappings(inheritedIntermediaryMappings, sourceSrg)
                        bakeNamedToIntermediaryMappings(projectIntermediaryMappings, destinationSrg)
                    }
                } else if (inheritedNotchMappings != null && projectNotchMappings != null
                        && inheritedNode.mcVersion == projectNode.mcVersion) {
                    tasks.register(prepareTaskName) {
                        bakeNamedToOfficialMappings(inheritedNotchMappings, inheritedIntermediaryMappings, sourceSrg)
                        bakeNamedToOfficialMappings(projectNotchMappings, projectIntermediaryMappings, destinationSrg)
                    }
                } else {
                    throw IllegalStateException("Failed to find mappings from $inherited to $project.")
                }
                tasks.withType<CrossVersionTask>().configureEach {
                    sourceMappings = sourceSrg
                    destinationMappings = destinationSrg
                    dependsOn(prepareTask)
                }
            }

            project.tasks.register<Copy>("setCoreVersion") {
                outputs.upToDateWhen { false }

                from(project.file("src"))
                from(File(project.buildDir, "crossversioned"))
                into(File(parent.projectDir, "src"))

                project.the<SourceSetContainer>().all {
                    val cName = if (name == "main") "" else name.capitalize()

                    if (kotlin) {
                        dependsOn(project.tasks.named("crossversion${cName}Kotlin"))
                    }
                    dependsOn(project.tasks.named("crossversion${cName}Java"))
                    dependsOn(project.tasks.named("crossversion${cName}Resources"))
                }

                doFirst {
                    // "Overwrites" for the core project are just stored in the main sources
                    // which will get overwritten soon, so we need to preserve those.
                    // Specifically, assume we've got projects A, B and C with C being the current
                    // core project and A being the soon to be one:
                    // If there is an overwrite in B, we need to preserve A's source version in A's overwrites.
                    // If there is an overwrite in C, we need to preserve B's version in B's overwrites and get
                    // rid of C's overwrite since it will now be stored in the main sources.
                    fun preserveOverwrites(project: Project, toBePreserved: List<Path>?) {
                        val overwrites = project.file("src").toPath()
                        val overwritten = overwrites.toFile()
                                .walk()
                                .filter { it.isFile }
                                .map { overwrites.relativize(it.toPath()) }
                                .toList()

                        // For the soon-to-be-core project, we must not yet delete the overwrites
                        // as they have yet to be copied into the main sources.
                        if (toBePreserved != null) {
                            val source = if (project.name == coreProject) {
                                project.parent!!.file( "src").toPath()
                            } else {
                                project.buildDir.toPath().resolve("crossversioned")
                            }
                            project.delete(overwrites)
                            toBePreserved.forEach { name ->
                                project.copy {
                                    from(source.resolve(name))
                                    into(overwrites.resolve(name).parent)
                                }
                            }
                        }

                        if (project.name != coreProject) {
                            val node = graph.findNode(project.name)!!
                            val nextLink = node.links.find { it.first.findNode(coreProject) != null }
                            val (nextNode, _) = nextLink ?: graph.findParent(node)!!
                            val nextProject = parent.project(nextNode.project)
                            preserveOverwrites(nextProject, overwritten)
                        }
                    }
                    preserveOverwrites(project, null)
                }

                doLast {
                    // Once our own overwrites have been copied into the main sources, we should remove them.
                    val overwrites = project.file("src")
                    project.delete(overwrites)
                    project.mkdir(overwrites)
                }

                doLast {
                    coreProjectFile.writeText(project.name)
                }
            }
        }
    }
}

private fun Task.bakeNamedToIntermediaryMappings(mappings: Mappings, destination: File) {
    mappings.tasks.forEach { this.dependsOn(it) }
    inputs.file(mappings.file)
    outputs.file(destination)
    doLast {
        val mapping = if (mappings.format == "tiny") {
            val tiny = mappings.file.inputStream().use { TinyMappingFactory.loadWithDetection(it.bufferedReader()) }
            TinyReader(tiny, "named", if (mappings.type == "searge") "srg" else "intermediary").read()
        } else {
            readMappings(mappings.format, mappings.file.toPath())
        }
        MappingFormats.SRG.write(mapping, destination.toPath())
    }
}

private fun Task.bakeNamedToOfficialMappings(mappings: Mappings, namedToIntermediaryMappings: Mappings?, destination: File) {
    mappings.tasks.forEach { this.dependsOn(it) }
    namedToIntermediaryMappings?.tasks?.forEach { dependsOn(it) }
    inputs.file(mappings.file)
    namedToIntermediaryMappings?.let { inputs.file(it.file) }
    outputs.file(destination)
    doLast {
        val mapping = if (mappings.format == "tiny") {
            val tiny = mappings.file.inputStream().use { TinyMappingFactory.loadWithDetection(it.bufferedReader()) }
            TinyReader(tiny, "named", "official").read()
        } else {
            val iMappings = namedToIntermediaryMappings!!
            val iMapSet = readMappings(iMappings.format, iMappings.file.toPath())
            val oMapSet = readMappings(mappings.format, mappings.file.toPath())
            oMapSet.join(iMapSet.reverse()).reverse()
        }
        MappingFormats.SRG.write(mapping, destination.toPath())
    }
}

private fun readMappings(format: String, path: Path): MappingSet {
    // special handling for TSRG2
    return if (format == "tsrg2") {
        // remove lines starting with "tsrg2" (header) and \t\t (method parameter / static indicator)
        val modifiedTSRG2 = path.toFile().readLines(StandardCharsets.UTF_8).stream().filter {
            !it.startsWith("tsrg2") && !it.startsWith("\t\t")
        }.collect(Collectors.joining("\n"))

        val inputStream = ByteArrayInputStream(modifiedTSRG2.toByteArray(StandardCharsets.UTF_8))

        // Proceed as the file would be normal TSRG:
        // This is fine because the the output of createMcpToSrg specifically only contains two names: `left` and `right`.
        // The other TSRG2 features, static indicators and parameter names, are irrelevant to our purpose and thus can be safely omitted as well.
        MappingFormats.byId("tsrg").createReader(inputStream).read()
    } else {
        MappingFormats.byId(format).read(path)
    }
}

private val Project.intermediaryMappings: Mappings?
    get() {
        project.tasks.findByName("genSrgs")?.let { // FG2
            return Mappings("searge", it.property("mcpToSrg") as File, "srg", listOf(it))
        }
        project.tasks.findByName("createMcpToSrg")?.let { // FG3-5
            val output = it.property("output")
            // FG3+4 returns a File, FG5 a RegularFileProperty
            return if (output is File) {
                Mappings("searge", output, "tsrg", listOf(it))
            } else {
                Mappings("searge", (output as RegularFileProperty).get().asFile, "tsrg2", listOf(it))
            }
        }
        mappingsProvider?.maybeGetGroovyProperty("tinyMappingsWithSrg")?.let { // architectury
            val file = (it as Path).toFile()
            if (file.exists()) {
                return Mappings("searge", file, "tiny", emptyList())
            }
        }
        tinyMappings?.let { return Mappings("yarn", it, "tiny", emptyList()) }
        return null
    }

data class Mappings(val type: String, val file: File, val format: String, val tasks: List<Task>)

private val Project.notchMappings: Mappings?
    get() {
        project.tasks.findByName("extractSrg")?.let { // FG3-5
            val output = it.property("output")
            // FG3+4 returns a File, FG5 a RegularFileProperty
            return if (output is File) {
                Mappings("notch", output, "tsrg", listOf(it))
            } else {
                Mappings("notch", (output as RegularFileProperty).get().asFile, "tsrg2", listOf(it))
            }
        }
        tinyMappings?.let { return Mappings("notch", it, "tiny", emptyList()) }
        return null
    }

private val Project.mappingsProvider: Any?
    get() {
        val extension = extensions.findByName("loom") ?: extensions.findByName("minecraft") ?: return null
        if (!extension.javaClass.name.contains("LoomGradleExtension")) return null
        return extension.withGroovyBuilder { getProperty("mappingsProvider") }
    }

private val Project.tinyMappings: File?
    get() {
        val mappingsProvider = mappingsProvider ?: return null
        mappingsProvider.maybeGetGroovyProperty("MAPPINGS_TINY")?.let { return it as File } // loom 0.2.5
        mappingsProvider.maybeGetGroovyProperty("tinyMappings")?.also {
            when (it) {
                is File -> return it // loom 0.2.6
                is Path -> return it.toFile() // loom 0.10.17
            }
        }
        throw GradleException("loom version not supported by crossversion plugin")
    }

private fun Any.maybeGetGroovyProperty(name: String) = withGroovyBuilder { metaClass }.hasProperty(this, name)?.getProperty(this)