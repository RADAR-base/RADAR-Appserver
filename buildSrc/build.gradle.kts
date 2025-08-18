import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.10"
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
    }
}

gradlePlugin {
    plugins {
        create("appserverConvention") {
            id = "org.radarbase.appserver-conventions"
            implementationClass = "org.radarbase.appserver.convention.AppserverConventionPlugin"
            displayName = "RADAR-AppServer conventions"
            description = "Common conventions for RADAR-AppServer "
        }
        create("customSourceSetConvention") {
            id = "org.radarbase.appserver.int-test-source-sets"
            implementationClass = "org.radarbase.appserver.convention.IntegrationTestSourceSetPlugin"
            displayName = "RADAR-AppServer custom source sets"
            description = "Custom source sets for RADAR-AppServer"
        }
    }
}