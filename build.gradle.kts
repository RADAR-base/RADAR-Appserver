plugins {
    id("org.radarbase.appserver-conventions")
    id("org.radarbase.radar-dependency-management") version Versions.radarCommonsVersion apply false
    id("org.radarbase.radar-kotlin") version Versions.radarCommonsVersion apply false
//    id("com.avast.gradle.docker-compose") version Versions.dockerCompose apply false
}

appserverProject {
    version.set(Versions.project)
    gradleWrapper.set(Versions.wrapper)
}

