package task

import Constants
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gradle.kotlin.dsl.accessors._e6d6ee632fa4d9f88efc00012ebafdee.java
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.nio.file.Files

abstract class GenerateMetadataTask : DefaultTask() {
	@get:Input
	val group: Property<String> = project.objects.property()

	@get:Input
	val namespace: Property<String> = project.objects.property()

	@get:Input
	val providedNamespaces: ListProperty<String> = project.objects.listProperty()

	@get:Input
	val version: Property<String> = project.objects.property()

	@get:Input
	val name: Property<String> = project.objects.property()

	@get:Input
	val description: Property<String> = project.objects.property()

	@get:Input
	val dependencies: ListProperty<String> = project.objects.listProperty<String>()

	@get:Input
	val javaVersion: Property<Int> = project.objects.property()

	@get:OutputFile
	val fmjPath: RegularFileProperty = project.objects.fileProperty()

	init {
		this.setGroup("generation")

		val base = project.extensions.getByType(BasePluginExtension::class.java)

		this.group.convention(project.group.toString())
		this.namespace.convention(base.archivesName.map { it.replace("-", "_") })
		this.providedNamespaces.convention(base.archivesName.map { listOf(it) })
		this.version.convention(project.version.toString())

		this.javaVersion.convention(project.java.toolchain.languageVersion.map { it.asInt() })
		this.fmjPath.convention(project.layout.buildDirectory.file("generated/fabric.mod.json"))
	}

	@TaskAction
	fun generateMetadata() {
		this.generateFmj()
	}

	private fun generateFmj() {
		// This is for compatibility with some loaders.
		val json = JsonObject()
		json.addProperty("schemaVersion", 1)
		json.addProperty("id", this.namespace.get())
		json.addProperty("version", this.version.get())
		json.addProperty("name", this.name.get())
		json.addProperty("description", this.description.get())

		val providedNamespaces = this.providedNamespaces.get()
		if (providedNamespaces.isNotEmpty()) {
			val namespaces = JsonArray()
			providedNamespaces.forEach { namespace -> namespaces.add(namespace) }
			json.add("provides", namespaces)
		}

		val authors = JsonArray()
		Constants.DEVELOPERS.map {
			val authorJson = JsonObject()
			authorJson.addProperty("name", it.name)
			val contactJson = JsonObject()
			contactJson.addProperty("email", it.email)
			authorJson.add("contact", contactJson)
			authorJson
		}.forEach(authors::add)
		json.add("authors", authors)

		val contact = JsonObject()
		json.add("contact", contact)
		contact.addProperty("homepage", Constants.PROJECT_URL)
		contact.addProperty("sources", Constants.GIT_URL)
		contact.addProperty("issues", Constants.ISSUES_URL)
		json.addProperty("license", Constants.LICENSE_NAME)

		val dependencies = JsonObject()
		this.dependencies.get().forEach {
			dependencies.addProperty(Constants.getArtifactName(it), "^${this.version.get()}")
		}
		dependencies.addProperty("java", ">=${this.javaVersion.get()}")
		json.add("depends", dependencies)

		val custom = JsonObject()
		json.add("custom", custom)
		val modmenu = JsonObject()
		custom.add("modmenu", modmenu)
		modmenu.add("badges", BADGES)
		val parent = JsonObject()
		modmenu.add("parent", parent)
		parent.addProperty("id", "yumi-commons")
		parent.addProperty("name", Constants.PROJECT_NAME)
		parent.addProperty("description", Constants.PROJECT_DESCRIPTION)
		parent.add("badges", BADGES)
		modmenu.addProperty("update_checker", false)

		Files.writeString(this.fmjPath.get().asFile.toPath(), GsonBuilder().create().toJson(json))
	}

	companion object {
		private val BADGES = JsonArray()

		init {
			BADGES.add("library")
		}
	}
}