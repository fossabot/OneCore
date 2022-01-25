package cc.woverflow.gradle.crossversion

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class RootCrossVersionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("crossversion", RootCrossVersionExtension::class)
    }
}