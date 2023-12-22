import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
}

val javaVersion = 17

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation(libs.licenser)
}

java {
	sourceCompatibility = JavaVersion.toVersion(javaVersion)
	targetCompatibility = JavaVersion.toVersion(javaVersion)
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.fromTarget(javaVersion.toString())
	}
}
