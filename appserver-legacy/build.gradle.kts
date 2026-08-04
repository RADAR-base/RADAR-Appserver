import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    eclipse
    scala
    checkstyle
    pmd
    id("io.gatling.gradle") version Versions.gatlingVersion
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("org.springframework.boot") version Versions.springBootVersion
    id("io.spring.dependency-management") version Versions.springDependencyManagementVersion
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("com.github.ben-manes.versions")
    id("io.sentry.jvm.gradle")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.java))
    }
}

springBoot {
    mainClass.set("org.radarbase.appserver.AppserverApplication")
}

tasks.bootJar {
    mainClass.set("org.radarbase.appserver.AppserverApplication")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

sourceSets {
    create("integrationTest") {
        java {
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output + sourceSets.test.get().compileClasspath
            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output + sourceSets.test.get().runtimeClasspath
            setSrcDirs(listOf(file("src/integrationTest/java")))
        }
        resources.setSrcDirs(listOf(file("src/integrationTest/resources")))
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestRuntimeOnly: Configuration by configurations.getting

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

// --- Vulnerability fixes ---
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.netty" && requested.name.startsWith("netty-codec")) {
            useVersion(Versions.nettyVersion)
            because("Force safe version of Netty across all modules")
        }
    }
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.18.0")

    // Force transitive dependency versions to mitigate vulnerabilities
    implementation("org.apache.tomcat.embed:tomcat-embed-core:${Versions.tomcatVersion}")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:${Versions.springOauth2AutoconfigureVersion}")
    implementation("org.springframework.security.oauth:spring-security-oauth2:${Versions.springOauth2Version}")
    runtimeOnly("org.hibernate.validator:hibernate-validator:${Versions.hibernateValidatorVersion}")
    implementation("io.minio:minio:${Versions.minioVersion}")

    // Open API spec
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.springDocVersion}")

    // runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.hsqldb:hsqldb")
    runtimeOnly("org.liquibase:liquibase-core:4.20.0")
    runtimeOnly("org.postgresql:postgresql:42.5.5")

    annotationProcessor("org.projectlombok:lombok:${Versions.lombokVersion}")
    implementation("org.projectlombok:lombok:${Versions.lombokVersion}")

    annotationProcessor("org.springframework:spring-context-indexer:${Versions.springVersion}")

    // FCM Admin SDK
    implementation("com.google.firebase:firebase-admin:${Versions.firebaseAdminVersion}")

    // AOP
    runtimeOnly("org.springframework:spring-aop:${Versions.springVersion}")
    implementation("org.radarbase:radar-spring-auth:${Versions.radarSpringAuthVersion}")

    testImplementation("io.gatling.highcharts:gatling-charts-highcharts:3.9.2")

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

    gatlingImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

javafx {
    version = "19"
    modules = listOf("javafx.controls")
}

checkstyle {
    configDirectory.set(file("config/checkstyle"))
    toolVersion = "10.8.0"
    isShowViolations = false
    isIgnoreFailures = true
    sourceSets = listOf(project.sourceSets.main.get())
}

pmd {
    sourceSets = listOf(project.sourceSets.main.get())
}

tasks.javadoc {
    (options as CoreJavadocOptions).addBooleanOption("html5", true)
    setDestinationDir(layout.projectDirectory.dir("src/main/resources/static/java-docs").asFile)
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")

    useJUnitPlatform {
        excludeEngines("junit-vintage")
    }

    environment("RADAR_IS_CONFIG_LOCATION", "src/integrationTest/resources/radar-is.yml")

    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest) }

tasks.named<Copy>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.test {
    useJUnitPlatform {
        excludeEngines("junit-vintage")
    }
    testLogging {
        events("failed")
        exceptionFormat = TestExceptionFormat.FULL

        error.exceptionFormat = TestExceptionFormat.FULL
    }
}

val bootJarProvider = tasks.named<BootJar>("bootJar")

tasks.register<Copy>("unpack") {
    dependsOn(bootJarProvider)
    from(bootJarProvider.map { zipTree(it.outputs.files.singleFile) })
    into(layout.buildDirectory.dir("dependency"))
}

tasks.register<JavaExec>("loadTest") {
    dependsOn("testClasses")
    description = "Load Test With Gatling"
    group = "Load Test"
    classpath = sourceSets.main.get().runtimeClasspath
    jvmArgs = listOf(
        "-Dgatling.core.directory.binaries=${sourceSets.main.get().output.classesDirs}"
    )
    mainClass.set("io.gatling.app.Gatling")
    args = listOf(
        "--simulation", "org.radarbase.appserver.ApiGatlingSimulationTest",
        "--results-folder", "${layout.buildDirectory.get()}/gatling-results",
        "--binaries-folder", sourceSets.main.get().output.classesDirs.toString(),
        "--bodies-folder", sourceSets.main.get().resources.srcDirs.first().toString() + "/gatling/bodies",
    )
}

tasks.register("downloadDependencies") {
    description = "Pre-downloads dependencies"
    doLast {
        configurations.named("compileClasspath").get().files
        configurations.named("runtimeClasspath").get().files
    }
}

tasks.register<Copy>("copyDependencies") {
    from(configurations.named("runtimeClasspath").get().files)
    into(layout.buildDirectory.dir("third-party"))
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return !stableKeyword && !regex.matches(version)
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}
