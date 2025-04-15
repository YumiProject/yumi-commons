import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

abstract class ModuleExtension(private val project: Project) {
	val description: Property<String> = project.objects.property()

	val dependencies: ListProperty<String> = project.objects.listProperty<String>()

	fun require(library: String) {
		val dependency = this.project.project(":libraries:$library")

		this.project.dependencies.add("api", dependency)
		this.dependencies.add(dependency.name)
	}
}
