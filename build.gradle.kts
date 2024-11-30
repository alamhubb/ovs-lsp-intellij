plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/gradle-plugin")
    }
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
    maven {
        url = uri("https://mirrors.huaweicloud.com/repository/maven")
    }
    maven {
        url = uri("https://www.jetbrains.com/intellij-repository/releases")
        isAllowInsecureProtocol = true
    }

    /*maven {
        url = uri("https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
    }
    maven {
        url = uri("https://repo.spring.io/release")
    }
    maven {
        url = uri("https://www.jetbrains.com/intellij-repository/releases")
    }*/
    gradlePluginPortal()
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

allprojects {
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            url = uri("https://mirrors.huaweicloud.com/repository/maven")
        }
        maven {
            url = uri("https://www.jetbrains.com/intellij-repository/releases")
            isAllowInsecureProtocol = true
        }
        gradlePluginPortal()
        mavenCentral()
    }
}


dependencies {
    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/com.jetbrains.intellij.platform/ide-util-io
//    runtimeOnly("com.jetbrains.intellij.platform:ide-util-io:242.23726.103")

    intellijPlatform {
        intellijIdeaUltimate("2024.2.4")
        instrumentationTools()
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}