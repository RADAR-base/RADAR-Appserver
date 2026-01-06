plugins {
    kotlin("plugin.serialization") version Versions.kotlinVersion
}

description = "A library that provides a validated and contracted connection between microservices"

dependencies {
    implementation(project(":microservices:core"))

    implementation("io.ktor:ktor-client-core:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktorVersion}")

    implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktorVersion}")
}

ktlint {
    ignoreFailures.set(true)
    outputColorName.set("RED")
}
