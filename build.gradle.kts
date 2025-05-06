import org.radarbase.appserver.convention.customSourceSet
import org.radarbase.gradle.plugin.radarKotlin

plugins {
    id("org.radarbase.appserver-conventions")
    id("org.radarbase.radar-dependency-management") version Versions.radarCommonsVersion
    id("org.radarbase.radar-kotlin") version Versions.radarCommonsVersion apply false
    id("org.radarbase.appserver-custom-source-sets") apply false
//    id("com.avast.gradle.docker-compose") version Versions.dockerCompose apply false
}

appserverProject {
    version.set(Versions.project)
    gradleWrapper.set(Versions.wrapper)
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")
    apply(plugin = "org.radarbase.appserver-custom-source-sets")

    radarKotlin {
        javaVersion.set(Versions.java)
        kotlinVersion.set(Versions.kotlinVersion)
        kotlinApiVersion.set(Versions.kotlinVersion)
        junitVersion.set(Versions.junit5Version)
    }

    customSourceSet {
        sourceSetName = "integrationTest"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        hookIntoCheck = true
    }
}