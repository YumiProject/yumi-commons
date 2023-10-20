dependencies {
	api(project(":libraries:collections"))
	api(libs.asm)

	testImplementation(libs.slf4j.simple)
}
