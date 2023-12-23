import task.CheckActionsRefTask

plugins {
	id("yumi-commons-base")
	id("io.github.gradle-nexus.publish-plugin").version("1.3.0")
	`java-library`
}

base.archivesName = "yumi-commons"

configure<ModuleExtension> {
	description = "The Yumi Commons libraries providing various utilities."
}

tasks.check.get().dependsOn(tasks.register<CheckActionsRefTask>("checkActions"))

// Add root project specifics to maven publication.
publishing.publications.getByName<MavenPublication>(Constants.PUBLICATION_NAME) {
	pom {
		packaging = "pom"
		name = Constants.PROJECT_NAME

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

nexusPublishing {
	repositories {
		val mavenCentralKey: String? by project
		val mavenCentralSecret: String? by project

		if (mavenCentralKey != null && mavenCentralSecret != null) {
			sonatype {
				username = mavenCentralKey
				password = mavenCentralSecret

				nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
				snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
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
