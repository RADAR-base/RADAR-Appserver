package org.radarbase.appserver.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import java.util.*

interface CustomSourceSetExtension {
    val sourceSetName: Property<String>
    val duplicatesStrategy: Property<DuplicatesStrategy>
    val hookIntoCheck: Property<Boolean>
}

@Suppress("unused")
fun Project.customSourceSet(config: CustomSourceSetExtension.() -> Unit) {
    configure<CustomSourceSetExtension>(config)
}

@Suppress("unused")
class CustomSourceSetCreatorPlugin : Plugin<Project> {
    override fun apply(project: Project) = project.run {
        val extension = extensions.create<CustomSourceSetExtension>("integrationTestConfig")

        afterEvaluate {
            val ssName = extension.sourceSetName.get()
            val sourceSets = extensions.getByType<SourceSetContainer>()
            sourceSets.create(ssName) {
                compileClasspath += sourceSets["main"].output + sourceSets["test"].output
                runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
            }

            val integrationTestImplementation = configurations.getByName("${ssName}Implementation")
            integrationTestImplementation.extendsFrom(configurations.getByName("testImplementation"))
            configurations.getByName("${ssName}RuntimeOnly")
                .extendsFrom(configurations.getByName("runtimeOnly"))

            val integrationTestTask = tasks.register<Test>(ssName) {
                description = "Runs $ssName tests."
                group = "verification"

                testClassesDirs = sourceSets[ssName].output.classesDirs
                classpath = sourceSets[ssName].runtimeClasspath
                shouldRunAfter("test")

                useJUnitPlatform { excludeEngines("junit-vintage") }
                testLogging { events("passed", "skipped", "failed") }
            }

            if (extension.hookIntoCheck.get()) {
                tasks.named("check") { dependsOn(integrationTestTask) }
            }

            tasks.named<Copy>("process${ssName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}Resources") {
                duplicatesStrategy = extension.duplicatesStrategy.get()
            }
        }
    }
}

