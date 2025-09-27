plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.9.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/com.jetbrains.intellij.platform/ide-util-io
//    runtimeOnly("com.jetbrains.intellij.platform:ide-util-io:242.23726.103")

    intellijPlatform {
        intellijIdeaUltimate("2025.2.1")
        bundledPlugins("JavaScript")
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}