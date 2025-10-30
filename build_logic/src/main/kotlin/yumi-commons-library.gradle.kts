import task.GenerateMetadataTask

plugins {
	id("yumi-commons-base")
	id("dev.yumi.gradle.licenser")
	`java-library`
}

base.archivesName = Constants.getArtifactName(project.name)
val fullName = "${Constants.PROJECT_NAME}: ${project.name.replaceFirstChar(Char::titlecase)}"

val javaVersion = Integer.parseInt(project.property("java_version") as String)

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(javaVersion))
	}

	withSourcesJar()
	withJavadocJar()

	testResultsDir.set(layout.buildDirectory.dir("junit-xml"))
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.release.set(javaVersion)

	// Java Modules
	options.javaModuleVersion = provider { version as String }
}

tasks.withType<Javadoc>().configureEach {
	options {
		this as StandardJavadocDocletOptions

		addStringOption("Xdoclint:all,-missing", "-quiet")
		links(
			"https://jspecify.dev/docs/api/",
			"https://javadoc.io/doc/org.jetbrains/annotations/26.0.2/",
		)
	}
}

val generateMetadata = tasks.register<GenerateMetadataTask>("generateMetadata") {
	val moduleExt = project.extensions.getByType(ModuleExtension::class.java)

	name.set(fullName)
	description.set(moduleExt.description)

	dependencies.set(moduleExt.dependencies)
}

tasks.jar {
	inputs.property("archivesName", base.archivesName)
	inputs.property("version", project.version)

	manifest {
		attributes(
			mapOf(
				"Implementation-Version" to inputs.properties["version"],
				"FMLModType" to "GAMELIBRARY", // Needed for event hidden classes.
				"Fabric-Loom-Remap" to "false"
			)
		)
	}

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}

	from(generateMetadata)
}

license {
	rule(rootProject.file("codeformat/HEADER"))
}

tasks.withType<Test>().configureEach {
	// Using JUnitPlatform for running tests
	useJUnitPlatform()

	testLogging {
		events("passed")
	}
}

publishing.publications.getByName<MavenPublication>(Constants.PUBLICATION_NAME) {
	from(components["java"])

	pom {
		name = fullName
	}
}
