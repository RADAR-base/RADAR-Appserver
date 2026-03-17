plugins {
    application
}

application {
    mainClass.set("org.radarbase.appserver.microservices.user.UserMicroserviceKt")
}

description = "AppServer User Microservices Implementation"

dependencies {
    implementation("com.h2database:h2:${Versions.h2Version}")

    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
    implementation(project(":microservices:core"))
    implementation(project(":microservices:contract"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerializationVersion}")
}

ktlint {
    ignoreFailures.set(true)
    outputColorName.set("RED")
}
