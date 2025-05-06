plugins {
    id("org.radarbase.radar-appserver-conventions")
}

appserverProject {
    version.set(Versions.project)
    gradleWrapper.set(Versions.wrapper)
}