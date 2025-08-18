package org.radarbase.appserver.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import java.util.Locale

interface IntegrationTestExtension {
    val sourceSetName: Property<String>
    val duplicatesStrategy: Property<DuplicatesStrategy>
    val hookIntoCheck: Property<Boolean>
}

fun Project.integrationTestConfig(config: IntegrationTestExtension.() -> Unit) = config.run {
        configure<IntegrationTestExtension>(config)
}

class IntegrationTestSourceSetPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.run {
        val extension = extensions.create<IntegrationTestExtension>("integrationTestConfig").apply {
            sourceSetName.convention("integrationTest")
            hookIntoCheck.convention(true)
        }

        afterEvaluate {
            val ssName = extension.sourceSetName.get()
            val sourceSets = extensions.getByType<SourceSetContainer>()

            sourceSets.create(ssName) {
                compileClasspath += sourceSets["main"].output
                runtimeClasspath += sourceSets["main"].output
            }

            val integrationTestImplementation = configurations.getByName("${ssName}Implementation")
            integrationTestImplementation.extendsFrom(configurations.getByName("testImplementation"))

            configurations.getByName("${ssName}RuntimeOnly")
                .extendsFrom(configurations.getByName("testRuntimeOnly"))

            val integrationTestTask = tasks.register<Test>(ssName) {
                description = "Runs ${ssName} tests."
                group = "verification"

                testClassesDirs = sourceSets[ssName].output.classesDirs
                classpath = sourceSets[ssName].runtimeClasspath
                shouldRunAfter("test")

                outputs.upToDateWhen { false }
                useJUnitPlatform { excludeEngines("junit-vintage") }
                testLogging { events("passed") }
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