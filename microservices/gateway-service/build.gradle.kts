plugins {
    application
}

dependencies {
    implementation(project(":microservices:core"))
    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
}
