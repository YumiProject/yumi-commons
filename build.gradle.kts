plugins {
	id("dev.yumi.gradle.licenser").version("1.0.+")
	id("java-library")
}

group = "dev.yumi"
version = "1.0.0-alpha.1-SNAPSHOT"
val javaVersion = 17

project(":libraries").subprojects {
	apply {
		plugin("dev.yumi.gradle.licenser")
		plugin("java-library")
	}

	repositories {
		mavenCentral()
	}

	dependencies {
		api(rootProject.libs.jetbrains.annotations)

		// Use JUnit Jupiter for testing.
		testImplementation(rootProject.libs.junit.jupiter)
		testRuntimeOnly(rootProject.libs.junit.launcher)
	}

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
	}

	tasks.withType<Javadoc>().configureEach {
		options {
			this as StandardJavadocDocletOptions

			addStringOption("Xdoclint:all,-missing", "-quiet")
		}
	}

	tasks.jar {
		from(rootProject.file("LICENSE")) {
			rename { "${it}_${base.archivesName.get()}" }
		}
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
}
