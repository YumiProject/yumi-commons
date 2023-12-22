plugins {
	id("yumi-commons-library")
}

configure<ModuleExtension> {
	description = "A event framework designed to be flexible and easy to use."
}

dependencies {
	api(project(":libraries:collections"))
	api(libs.asm)

	testImplementation(libs.slf4j.simple)
}
