import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    eclipse
    idea
//    scala
    kotlin("kapt")
    kotlin("plugin.allopen") version Versions.kotlinVersion
    kotlin("plugin.noarg") version Versions.kotlinVersion
//    id("io.gatling.gradle") version Versions.gatlingVersion
    id("org.springframework.boot") version Versions.springBootVersion
    id("io.spring.dependency-management") version Versions.springDependencyManagementVersion
    id("org.radarbase.radar-dependency-management")
    id("org.radarbase.radar-kotlin")
    id("org.jetbrains.kotlin.plugin.spring") version Versions.kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version Versions.kotlinVersion
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

kapt {
    keepJavacAnnotationProcessors = true
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

springBoot {
    mainClass.set("org.radarbase.appserver.AppserverApplicationKt")
}

tasks.bootJar {
    mainClass.set("org.radarbase.appserver.AppserverApplicationKt")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().runtimeClasspath
    }
}
//
val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestRuntimeOnly: Configuration by configurations.getting

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())
//
// kotlin {
//    compilerOptions {
//        jvmTarget.set(JvmTarget.JVM_17)
//        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
//        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
//    }
// }

// java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(17))
//    }
// }

// tasks.withType<KotlinJvmCompile>().configureEach {
//    jvmTargetValidationMode.set(JvmTargetValidationMode.ERROR)
// }

radarDependencies {
    rejectMajorVersionUpdates.set(true)
}

radarKotlin {
    javaVersion.set(Versions.java)
    kotlinVersion.set(Versions.kotlinVersion)
//    kotlinApiVersion.set(Versions.kotlinVersion)
    junitVersion.set(Versions.junit5Version)
}

// integrationTestConfig {
//    sourceSetName = "IntegrationTest"
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    hookIntoCheck = true
// }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.security:spring-security-config:${Versions.springSecurityVersion}")
    implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:${Versions.springOauth2AutoconfigureVersion}")
    implementation("org.springframework.security.oauth:spring-security-oauth2:${Versions.springOauth2Version}")

    runtimeOnly("org.hibernate.validator:hibernate-validator:${Versions.hibernateValidatorVersion}")

    implementation("io.minio:minio:${Versions.minioVersion}") {
        exclude(group = "org.jetbrains.kotlin")
    }

    // Open API spec
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.springDocVersion}")

    // runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.hsqldb:hsqldb")
    runtimeOnly("org.liquibase:liquibase-core:4.20.0")
    runtimeOnly("org.postgresql:postgresql:42.5.5")

    annotationProcessor("org.projectlombok:lombok:${Versions.lombokVersion}")
    implementation("org.projectlombok:lombok:${Versions.lombokVersion}")

    kapt("org.springframework:spring-context-indexer:${Versions.springVersion}")
    annotationProcessor("org.springframework:spring-context-indexer:${Versions.springVersion}")

    // FCM Admin SDK
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

    // AOP
    runtimeOnly("org.springframework:spring-aop:${Versions.springVersion}")
    implementation("org.radarbase:radar-spring-auth:${Versions.radarSpringAuthVersion}")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonKotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("io.ktor:ktor-client-core:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktorVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutinesVersion}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlinVersion}")

//    testImplementation("io.gatling.highcharts:gatling-charts-highcharts:3.9.2")

    implementation("org.liquibase.ext:liquibase-hibernate6:4.20.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit", module = "junit")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5Version}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit5Version}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${Versions.junit5Version}")
    testImplementation("org.junit.platform:junit-platform-commons:1.8.2")
    testImplementation("org.junit.platform:junit-platform-launcher:1.8.2")
    testImplementation("org.junit.platform:junit-platform-engine:1.8.2")

//    gatlingImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

ktlint {
    ignoreFailures.set(true)
}

noArg {
    annotation("org.radarbase.appserver.util.GenerateZeroArgs")
}

allOpen {
    annotation("org.radarbase.appserver.util.OpenClass")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform {
        excludeEngines("junit-vintage")
    }

    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest) }

tasks.named<Copy>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    testLogging {
        events("failed")
        exceptionFormat = TestExceptionFormat.FULL

        error.exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.javadoc {
    (options as CoreJavadocOptions).addBooleanOption("html5", true)
    setDestinationDir(layout.projectDirectory.dir("src/main/resources/static/java-docs").asFile)
}

val bootJarProvider = tasks.named<BootJar>("bootJar")

tasks.register<Copy>("unpack") {
    dependsOn(bootJarProvider)
    from(bootJarProvider.map { zipTree(it.outputs.files.singleFile) })
    into(layout.buildDirectory.dir("dependency"))
}

// tasks.register("downloadDependencies") {
//    description = "Pre download dependencies"
//
//    doLast {
//        configurations.compileClasspath.get().files
//        configurations.runtimeClasspath.get().files
//    }
// }
//
// tasks.register<Copy>("copyDependencies") {
//    from(configurations.runtimeClasspath)
//    into(layout.buildDirectory.dir("third-party"))
// }

val isNonStable: (String) -> Boolean = { version: String ->
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any {
        version.uppercase().contains(it)
    }
    val regex = Regex("^[0-9,.v-]+(-r)?$")
    !stableKeyword && !(regex.matches(version))
}

// tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
//    rejectVersionIf {
//        isNonStable(candidate.version)
//    }
// }
