plugins {
	`maven-publish`
	signing
}

group = Constants.GROUP
version = Constants.VERSION

val moduleExtension = project.extensions.create<ModuleExtension>("module")

repositories {
	mavenCentral()
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			pom {
				url = Constants.PROJECT_URL

				organization {
					name = Constants.ORG_NAME
					url = Constants.ORG_URL
				}

				licenses {
					license {
						name = Constants.LICENSE_NAME
						url = Constants.LICENSE_URL
					}
				}

				scm {
					url = Constants.GIT_URL
					connection = Constants.GIT_CONNECTION
					developerConnection = Constants.GIT_DEV_CONNECTION
				}
			}

			afterEvaluate {
				artifactId = base.archivesName.get()

				pom {
					description = moduleExtension.description.get()
				}
			}
		}
	}
}

signing {
	val signingKeyId: String? by rootProject
	val signingKey: String? by rootProject
	val signingPassword: String? by rootProject
	isRequired = signingKeyId != null && signingKey != null && signingPassword != null
	useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)

	sign(publishing.publications["maven"])

	afterEvaluate {
		tasks["signMavenPublication"].group = "publishing"
	}
}
