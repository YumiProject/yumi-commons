import org.gradle.configurationcache.extensions.capitalized

plugins {
	id("yumi-commons-base")
	id("dev.yumi.gradle.licenser")
	`java-library`
}

base.archivesName = "yumi-commons-" + project.name

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(Constants.JAVA_VERSION))
	}

	withSourcesJar()
	withJavadocJar()

	testResultsDir.set(layout.buildDirectory.dir("junit-xml"))
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.release.set(Constants.JAVA_VERSION)
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

publishing.publications.getByName<MavenPublication>(Constants.PUBLICATION_NAME) {
	from(components["java"])

	pom {
		name = Constants.PROJECT_NAME + ": " + project.name.capitalized()
	}
}
