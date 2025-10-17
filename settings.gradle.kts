pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

rootProject.name = "radar-appserver"

include("appserver-legacy")
include("appserver-jersey")

file("microservices").listFiles()
    ?.filter { File(it, "build.gradle.kts").exists() }
    ?.forEach { dir ->
        include("microservices:${dir.name}")
    }
