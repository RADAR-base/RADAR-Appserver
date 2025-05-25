plugins {
    idea
    id("org.radarbase.appserver-conventions")
    id("org.radarbase.radar-dependency-management") version Versions.radarCommonsVersion apply false
    id("org.radarbase.radar-kotlin") version Versions.radarCommonsVersion apply false

    id("org.jetbrains.kotlin.plugin.spring") version Versions.kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version Versions.kotlinVersion
    kotlin("plugin.allopen") version Versions.kotlinVersion
    kotlin("plugin.noarg") version Versions.kotlinVersion

//    id("com.avast.gradle.docker-compose") version Versions.dockerCompose apply false
}

appserverProject {
    version.set(Versions.project)
    gradleWrapper.set(Versions.wrapper)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

