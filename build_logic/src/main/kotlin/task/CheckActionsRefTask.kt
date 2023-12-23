package task

import Constants
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Represents a task which checks that the ref name used for the Git tag is accurate to the version that's being published.
 */
open class CheckActionsRefTask : DefaultTask() {
	init {
		this.group = "verification"
		this.enabled = (System.getenv("GITHUB_ACTIONS") ?: "") == "true"
	}

	@TaskAction
	fun execute() {
		val refName = System.getenv("ACTIONS_REF")

		if (refName != null && refName != "refs/tags/v${Constants.VERSION}") {
			throw GradleException("Failed to validate ref name.")
		}
	}
}
