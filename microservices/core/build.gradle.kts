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
    api("org.glassfish.jersey.ext:jersey-bean-validation:${Versions.jerseyBeanValidationVersion}")
    api("com.google.firebase:firebase-admin:${Versions.firebaseAdminVersion}") {
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

    implementation("com.google.guava:guava:${Versions.guavaVersion}")
    api("org.quartz-scheduler:quartz:${Versions.quartzVersion}")

}

allOpen {
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
}

ktlint {
    ignoreFailures.set(true)
    outputColorName.set("RED")
}
