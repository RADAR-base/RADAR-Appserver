plugins {
    application
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
}

description = "AppServer Project Microservices Implementation"

dependencies {
    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
    implementation(project(":microservices:core"))
}
