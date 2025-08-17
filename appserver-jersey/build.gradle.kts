plugins {
    application
    kotlin("plugin.serialization") version Versions.kotlinVersion
    id("org.radarbase.radar-kotlin") version Versions.radarCommonsVersion
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
}

application {
    mainClass.set("org.radarbase.appserver.jersey.JerseyAppserverKt")

    applicationDefaultJvmArgs = listOf(
        "-Dcom.sun.management.jmxremote",
        "-Dcom.sun.management.jmxremote.local.only=false",
        "-Dcom.sun.management.jmxremote.port=9010",
        "-Dcom.sun.management.jmxremote.authenticate=false",
        "-Dcom.sun.management.jmxremote.ssl=false",
    )
}

description = "RADAR Appserver for scheduling tasks and notifications."

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

allOpen {
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation(kotlin("reflect"))

    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommonsVersion}")
    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
    implementation("org.radarbase:radar-jersey-hibernate:${Versions.radarJerseyVersion}") {
        runtimeOnly("org.postgresql:postgresql:${Versions.postgresqlVersion}")
    }
    implementation("com.h2database:h2:${Versions.h2Version}")

    implementation("io.ktor:ktor-client-core:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktorVersion}")
    implementation("org.glassfish.jersey.ext:jersey-bean-validation:3.1.10")

    implementation("com.google.firebase:firebase-admin:9.3.0") {
        constraints {
            implementation("com.google.protobuf:protobuf-java:3.25.5") {
                because("Provided version of protobuf has security vulnerabilities")
            }
            implementation("com.google.protobuf:protobuf-java-util:3.25.5") {
                because("Provided version of protobuf has security vulnerabilities")
            }
        }
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("org.quartz-scheduler:quartz:2.5.0")

    testImplementation("io.mockk:mockk:1.14.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation("org.assertj:assertj-core:3.24.2")

    integrationTestImplementation(platform("io.ktor:ktor-bom:${Versions.ktorVersion}"))
    integrationTestImplementation("io.ktor:ktor-client-content-negotiation")
    integrationTestImplementation("io.ktor:ktor-serialization-kotlinx-json")
}

ktlint {
    ignoreFailures.set(false)
    outputColorName.set("RED")
}

radarKotlin {
    javaVersion.set(Versions.java)
    kotlinVersion.set(Versions.kotlinVersion)
//    kotlinApiVersion.set(Versions.kotlinVersion)
    junitVersion.set(Versions.junit5Version)
    log4j2Version.set(Versions.log4j2)
}
