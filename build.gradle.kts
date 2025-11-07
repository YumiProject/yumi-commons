import task.CheckActionsRefTask

plugins {
	id("yumi-commons-base")
	alias(libs.plugins.nexus.publish)
	`java-library`
}

base.archivesName = "yumi-commons"

module {
	description = Constants.PROJECT_DESCRIPTION
}

tasks.check.get().dependsOn(tasks.register<CheckActionsRefTask>("checkActions"))

// Add root project specifics to maven publication.
publishing.publications.getByName<MavenPublication>(Constants.PUBLICATION_NAME) {
	val dependencies = project(":libraries").subprojects.map {
		it.providers.provider {
			YumiModuleIdentifier(it.group as String, it.base.archivesName.get(), it.version as String)
		}
	}

	pom {
		packaging = "pom"
		name = Constants.PROJECT_NAME

		// Apparently Gradle has no way to use the components.java without an artifact which sucks.
		withXml {
			val dependenciesNode = asNode().appendNode("dependencies")

			dependencies.map { it.get() }.forEach {
				val dep = dependenciesNode.appendNode("dependency")
				dep.appendNode("groupId", it.group)
				dep.appendNode("artifactId", it.artifact)
				dep.appendNode("version", it.version)
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

				nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
				snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
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
		api(rootProject.libs.jspecify)
		api(rootProject.libs.jetbrains.annotations)

		// Use JUnit Jupiter for testing.
		testImplementation(platform(rootProject.libs.junit.bom))
		testImplementation(rootProject.libs.junit.jupiter)
		testRuntimeOnly(rootProject.libs.junit.launcher)
	}
}
