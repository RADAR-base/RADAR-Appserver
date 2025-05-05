pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "appserver"

include("appserver-legacy")
include("appserver-jersey")