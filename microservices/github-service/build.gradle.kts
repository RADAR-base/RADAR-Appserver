plugins {
    application
}

description = "AppServer Github Microservices Implementation"

dependencies {
    implementation(project(":microservices:core"))
    implementation(project(":microservices:contract"))
    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")

    implementation("io.ktor:ktor-client-core:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktorVersion}")

    implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktorVersion}")
}
