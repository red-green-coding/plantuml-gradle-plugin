package com.github.redgreencoding.plantuml

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'redgreencoding.plantuml' plugin.
 */
class PlantumlGradlePluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("redgreencoding.plantuml")

        // Verify the result
        assertNotNull(project.tasks.findByName("plantumlAll"))
    }
}
