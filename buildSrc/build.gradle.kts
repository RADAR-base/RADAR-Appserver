import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

//dependencies {
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-quartz")
//    implementation("org.springframework.boot:spring-boot-starter-security")
//    implementation("org.springframework.boot:spring-boot-starter-actuator")
//    implementation("org.springframework.boot:spring-boot-starter-mail")
//
//    implementation("org.springframework.security:spring-security-config:$Versions.springSecurityVersion")
//    implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:$Versions.springOauth2AutoconfigureVersion")
//    implementation("org.springframework.security.oauth:spring-security-oauth2:$Versions.springOauth2Version")
//
//    runtimeOnly("org.hibernate.validator:hibernate-validator:$Versions.hibernateValidatorVersion")
//
//    implementation("io.minio:minio:$Versions.minioVersion") {
//        exclude(group = "org.jetbrains.kotlin")
//    }
//
//    // Open API spec
//    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$Versions.springDocVersion")
//
//    //runtimeOnly("org.springframework.boot:spring-boot-devtools")
//    runtimeOnly("org.hsqldb:hsqldb")
//    runtimeOnly("org.liquibase:liquibase-core:4.20.0")
//    runtimeOnly("org.postgresql:postgresql:42.5.5")
//
//    annotationProcessor("org.projectlombok:lombok:$Versions.lombokVersion")
//    implementation("org.projectlombok:lombok:$Versions.lombokVersion")
//
//    kapt("org.springframework:spring-context-indexer:$Versions.springVersion")
//    annotationProcessor("org.springframework:spring-context-indexer:$Versions.springVersion")
//
//    // FCM Admin SDK
//    implementation("com.google.firebase:firebase-admin:9.3.0") {
//        constraints {
//            implementation("com.google.protobuf:protobuf-java:3.25.5") {
//                because("Provided version of protobuf has security vulnerabilities")
//            }
//            implementation("com.google.protobuf:protobuf-java-util:3.25.5") {
//                because("Provided version of protobuf has security vulnerabilities")
//            }
//        }
//    }
//
//    // AOP
//    runtimeOnly("org.springframework:spring-aop:$Versions.springVersion")
//    implementation("org.radarbase:radar-spring-auth:$Versions.radarSpringAuthVersion")
//
//    // Kotlin
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$Versions.kotlinVersion")
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$Versions.jacksonKotlinVersion")
//    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")
//    kapt("org.springframework.boot:spring-boot-configuration-processor")
//    implementation("io.ktor:ktor-client-core:$Versions.ktorVersion")
//    implementation("io.ktor:ktor-client-cio:$Versions.ktorVersion")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$Versions.coroutinesVersion")
//    testImplementation("org.mockito.kotlin:mockito-kotlin:$Versions.mockitoKotlinVersion")
//
//    testImplementation("io.gatling.highcharts:gatling-charts-highcharts:3.9.2")
//
//    implementation("org.liquibase.ext:liquibase-hibernate6:4.20.0")
//
//    testImplementation("org.springframework.boot:spring-boot-starter-test") {
//        exclude(group = "org.junit", module = "junit")
//    }
//
//    testImplementation("org.junit.jupiter:junit-jupiter:$Versions.junit5Version")
//    testImplementation("org.junit.jupiter:junit-jupiter-api:$Versions.junit5Version")
//    testImplementation("org.junit.jupiter:junit-jupiter-engine:$Versions.junit5Version")
//    testImplementation("org.junit.platform:junit-platform-commons:1.8.2")
//    testImplementation("org.junit.platform:junit-platform-launcher:1.8.2")
//    testImplementation("org.junit.platform:junit-platform-engine:1.8.2")
//
//    gatlingImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
//}


