plugins {
    application
}

description = "Radar Appserver User Microservices Implementation"

dependencies {
    implementation(project(":microservices:core"))
    implementation(project(":microservices:contract"))

    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
}
