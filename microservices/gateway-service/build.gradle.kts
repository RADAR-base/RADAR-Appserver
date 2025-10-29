plugins {
    application
}

dependencies {
    implementation(project(":microservices:core"))
    implementation(project(":microservices:contract"))
    implementation("org.radarbase:radar-jersey:${Versions.radarJerseyVersion}")
}
