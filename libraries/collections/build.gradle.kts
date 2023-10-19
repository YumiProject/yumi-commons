dependencies {
	api(project(":libraries:core"))
	api(libs.slf4j.api)
	testImplementation(libs.slf4j.simple)
}
