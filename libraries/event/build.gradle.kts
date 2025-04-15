plugins {
	id("yumi-commons-library")
}

module {
	description = "An event framework designed to be flexible and easy to use."

	require("collections")
}

dependencies {
	api(libs.asm)

	testImplementation(libs.slf4j.simple)
}
