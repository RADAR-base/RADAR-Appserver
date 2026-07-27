import java.time.Duration

plugins {
    application
    kotlin("plugin.serialization") version Versions.kotlinVersion
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    id("org.radarbase.radar-kotlin")
    id("com.avast.gradle.docker-compose") version Versions.dockerCompose
}

application {
    mainClass.set("org.radarbase.appserver.jersey.JerseyAppserverKt")
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

dockerCompose {
    useComposeFiles.set(listOf("src/integrationTest/resources/docker/docker-compose.yml"))
    val dockerComposeBuild: String? by project
    val doBuild = dockerComposeBuild?.toBoolean() ?: true
    buildBeforeUp.set(doBuild)
    buildBeforePull.set(doBuild)
    buildAdditionalArgs.set(emptyList<String>())
    val dockerComposeStopContainers: String? by project
    stopContainers.set(dockerComposeStopContainers?.toBoolean() ?: true)
    waitForTcpPortsTimeout.set(Duration.ofMinutes(3))
    environment.put("SERVICES_HOST", "localhost")
    captureContainersOutputToFiles.set(project.file("build/container-logs"))
    isRequiredBy(integrationTest)
}

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
    implementation("org.glassfish.jersey.ext:jersey-bean-validation:${Versions.jerseyBeanValidationVersion}")

    implementation("com.google.firebase:firebase-admin:${Versions.firebaseAdminVersion}") {
        constraints {
            implementation("com.google.protobuf:protobuf-java:${Versions.protobufVersion}") {
                because("Provided version of protobuf has security vulnerabilities")
            }
            implementation("com.google.protobuf:protobuf-java-util:${Versions.protobufVersion}") {
                because("Provided version of protobuf has security vulnerabilities")
            }
        }
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerializationVersion}")

    implementation("com.sun.mail:jakarta.mail:${Versions.jakartaMailVersion}")
    implementation("com.google.guava:guava:${Versions.guavaVersion}")
    implementation("org.quartz-scheduler:quartz:${Versions.quartzVersion}")

    testImplementation("io.mockk:mockk:${Versions.mockkVersion}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlinVersion}")
    testImplementation("org.hamcrest:hamcrest:${Versions.hamcrestVersion}")
    testImplementation("org.assertj:assertj-core:${Versions.assertjVersion}")

    integrationTestImplementation(platform("io.ktor:ktor-bom:${Versions.ktorVersion}"))
    integrationTestImplementation("io.ktor:ktor-client-content-negotiation")
    integrationTestImplementation("io.ktor:ktor-serialization-kotlinx-json")
}

ktlint {
    ignoreFailures.set(true)
    outputColorName.set("RED")
}

radarKotlin {
    javaVersion.set(Versions.java)
    kotlinVersion.set(Versions.kotlinVersion)
    junitVersion.set(Versions.junit5Version)
    log4j2Version.set(Versions.log4j2)
}
