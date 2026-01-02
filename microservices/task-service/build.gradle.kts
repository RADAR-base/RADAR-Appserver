plugins {
    application
}

application {
    mainClass.set("org.radarbase.appserver.microservices.task.TaskMicroserviceKt")
}

description = "AppServer Task State Event Microservices Implementation"

dependencies {
    implementation("com.h2database:h2:${Versions.h2Version}")
    implementation("com.google.guava:guava:${Versions.guavaVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
    implementation(project(":microservices:core"))
    implementation(project(":microservices:contract"))
}

ktlint {
    ignoreFailures.set(true)
    outputColorName.set("RED")
}
