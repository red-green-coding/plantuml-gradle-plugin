package io.github.redgreencoding.plantuml

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testfixtures.ProjectBuilder

/**
 * A simple unit test for the 'redgreencoding.plantuml' plugin.
 */
class PlantumlGradlePluginSpec : StringSpec({
    "plugin registers task" {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.redgreencoding.plantuml")

        // Verify the result
        project.tasks.findByName("plantumlAll").shouldNotBeNull()
    }
})
