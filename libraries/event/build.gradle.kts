plugins {
	id("yumi-commons-library")
	id("me.champeau.jmh")
}

module {
	description = "An event framework designed to be flexible and easy to use."

	require("collections")
}

dependencies {
	testImplementation(libs.slf4j.simple)
}

jmh {
	fork.set(2)
	threads.set(4)
}

sourceSets.jmh.configure {
	compileClasspath += sourceSets.test.get().output
}
