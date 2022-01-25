plugins {
    `kotlin-dsl`
    `maven-publish`
    groovy
}

group = "cc.woverflow.wcore"
version = "1.0.0"

val kotestVersion: String by project.extra

gradlePlugin {
    plugins {
        register("crossversion") {
            id = "cc.woverflow.crossversion"
            implementationClass = "cc.woverflow.gradle.crossversion.CrossVersionPlugin"
        }
        register("crossversion-root") {
            id = "cc.woverflow.crossversion-root"
            implementationClass = "cc.woverflow.gradle.crossversion.RootCrossVersionPlugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.fabricmc.net")
}

dependencies {
    implementation(gradleApi())
    compile(localGroovy())
    implementation("com.github.replaymod:remap:f9fe968")
    implementation("net.fabricmc:tiny-mappings-parser:0.2.1.13")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
}