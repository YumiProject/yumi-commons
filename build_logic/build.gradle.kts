import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
}

val javaVersion = 17

repositories {
	mavenLocal()
	gradlePluginPortal()
}

dependencies {
	implementation(libs.licenser)
	implementation(libs.gradle.jmh)
	implementation(libs.nexus.publish)
	implementation(libs.gson)
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
