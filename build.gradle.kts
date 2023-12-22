plugins {
	id("yumi-commons-base")
	`java-library`
}

base.archivesName = "yumi-commons"

configure<ModuleExtension> {
	description = "The Yumi Commons libraries providing various utilities."
}

publishing.publications.getByName<MavenPublication>("maven") {
	pom {
		packaging = "pom"

		// Apparently Gradle has no way to use the components.java without an artifact which sucks.
		withXml {
			val dependencies = asNode().appendNode("dependencies")

			project(":libraries").subprojects {
				val dep = dependencies.appendNode("dependency")
				dep.appendNode("groupId", project.group)
				dep.appendNode("artifactId", project.base.archivesName.get())
				dep.appendNode("version", project.version)
				dep.appendNode("scope", "compile")
			}
		}
	}
}

project(":libraries").subprojects {
	apply {
		plugin("java-library")
		plugin("maven-publish")
		plugin("signing")
	}

	dependencies {
		api(rootProject.libs.jetbrains.annotations)

		// Use JUnit Jupiter for testing.
		testImplementation(rootProject.libs.junit.jupiter)
		testRuntimeOnly(rootProject.libs.junit.launcher)
	}
}
