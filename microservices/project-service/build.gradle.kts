plugins {
    application
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
}

description = "AppServer Project Microservices Implementation"

application {
    mainClass.set("org.radarbase.appserver.microservices.project.ProjectMicroserviceKt")
}

dependencies {
    implementation("com.h2database:h2:${Versions.h2Version}")

    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
    implementation(project(":microservices:core"))
    implementation(project(":microservices:contract"))
}
ktlint {
    ignoreFailures.set(true)
    outputColorName.set("RED")
}

