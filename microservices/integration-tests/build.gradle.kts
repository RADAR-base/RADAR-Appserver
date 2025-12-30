plugins {
    kotlin("plugin.serialization") version Versions.kotlinVersion
    id("com.avast.gradle.docker-compose") version Versions.dockerCompose
}

description = "Integration tests for radar appserver microservices."

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    testLogging.showStandardStreams = true
    shouldRunAfter("test")
    outputs.upToDateWhen { false }
}

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    integrationTestImplementation(project(":microservices:core"))
    integrationTestImplementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
    integrationTestImplementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommonsVersion}")
    integrationTestImplementation("io.mockk:mockk:1.14.4")
    integrationTestImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    integrationTestImplementation("org.hamcrest:hamcrest:2.1")
    integrationTestImplementation("org.assertj:assertj-core:3.24.2")
    integrationTestImplementation(platform("io.ktor:ktor-bom:${Versions.ktorVersion}"))
    integrationTestImplementation("io.ktor:ktor-client-core:${Versions.ktorVersion}")
    integrationTestImplementation("io.ktor:ktor-client-cio:${Versions.ktorVersion}")
    integrationTestImplementation("io.ktor:ktor-client-content-negotiation")
    integrationTestImplementation("io.ktor:ktor-serialization-kotlinx-json")
}

