plugins {
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    kotlin("plugin.serialization") version Versions.kotlinVersion
}

description = "Core library containing shared configurations, utilities, and common data models required by appserver microservices."

allOpen {
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
}

dependencies {
    api("org.radarbase:radar-jersey-hibernate:${Versions.radarJerseyVersion}") {
        runtimeOnly("org.postgresql:postgresql:${Versions.postgresqlVersion}")
    }
    api("org.glassfish.jersey.ext:jersey-bean-validation:3.1.10")
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

}

allOpen {
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
}

radarKotlin {
    javaVersion.set(Versions.java)
    kotlinVersion.set(Versions.kotlinVersion)
//    kotlinApiVersion.set(Versions.kotlinVersion)
    junitVersion.set(Versions.junit5Version)
    log4j2Version.set(Versions.log4j2)
}
