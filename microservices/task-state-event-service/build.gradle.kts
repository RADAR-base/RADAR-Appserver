plugins {
    application
}

description = "AppServer Task State Event Microservices Implementation"

dependencies {
    implementation("com.h2database:h2:${Versions.h2Version}")

    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
    implementation(project(":microservices:core"))
    implementation(project(":microservices:contract"))
}
