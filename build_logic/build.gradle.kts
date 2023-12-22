import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
}

val javaVersion = 17

repositories {
	gradlePluginPortal()
	maven {
		name = "Fabric"
		url = uri("https://maven.fabricmc.net/")
	}
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
