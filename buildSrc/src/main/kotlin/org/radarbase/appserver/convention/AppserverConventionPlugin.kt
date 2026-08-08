package org.radarbase.appserver.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

interface AppserverProjectExtension {
    val group: Property<String>
    val version: Property<String>
    val gradleWrapper: Property<String>
}

@Suppress("unused")
fun Project.appserverProject(config: AppserverProjectExtension.() -> Unit) {
    configure<AppserverProjectExtension>(config)
}

@Suppress("unused")
class AppserverConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) = project.run {
        val extension = extensions.create("appserverProject", AppserverProjectExtension::class.java).apply {
            group.convention("org.radarbase")
        }

        allprojects {
            afterEvaluate {
                version = extension.version.get()
                group = extension.group.get()
            }
        }

        afterEvaluate {
            tasks.named<Wrapper>("wrapper") {
                gradleVersion = extension.gradleWrapper.get()
            }
        }
    }
}