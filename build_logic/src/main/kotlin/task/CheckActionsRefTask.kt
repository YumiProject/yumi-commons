package task

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

/**
 * Represents a task which checks that the ref name used for the Git tag is accurate to the version that's being published.
 */
abstract class CheckActionsRefTask : DefaultTask() {
	@get:Input
	val version: Property<String> = project.objects.property()

	init {
		this.group = "verification"
		this.enabled = (System.getenv("GITHUB_ACTIONS") ?: "") == "true"

		this.version.convention(project.version.toString())
	}

	@TaskAction
	fun execute() {
		val refName = System.getenv("ACTIONS_REF")

		if (refName != null && refName != "refs/tags/v${this.version.get()}") {
			throw GradleException("Failed to validate ref name.")
		}
	}
}
