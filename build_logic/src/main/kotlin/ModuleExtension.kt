import org.gradle.api.provider.Property

interface ModuleExtension {
	val description: Property<String>
}