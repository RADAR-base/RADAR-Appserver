plugins {
}

description = "A library that provides a validated and contracted connection between microservices"

dependencies {
    implementation("io.ktor:ktor-client-core:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktorVersion}")
}
