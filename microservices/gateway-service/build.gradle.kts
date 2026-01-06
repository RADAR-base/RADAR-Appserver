plugins {
    application
}

application {
    mainClass.set("org.radarbase.appserver.microservices.gateway.GatewayMicroserviceKt")
}

description = "AppServer Gateway Microservices Implementation"

dependencies {
    implementation(project(":microservices:core"))
    implementation(project(":microservices:contract"))
    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

ktlint {
    ignoreFailures.set(true)
    outputColorName.set("RED")
}
