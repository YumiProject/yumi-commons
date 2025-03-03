plugins {
	id("yumi-commons-library")
}

module {
	description = "A library of collections-related utilities."
}

dependencies {
	api(project(":libraries:core"))
	api(libs.slf4j.api)
	testImplementation(libs.slf4j.simple)
}
