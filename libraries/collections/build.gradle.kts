plugins {
	id("yumi-commons-library")
}

module {
	description = "A library of collections-related utilities."

	require("core")
}

dependencies {
	api(libs.slf4j.api)
	testImplementation(libs.slf4j.simple)
}
