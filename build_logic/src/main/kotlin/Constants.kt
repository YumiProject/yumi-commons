data class Developer(val name: String, val email: String)

object Constants {
	const val GROUP = "dev.yumi.commons"
	const val VERSION = "1.0.0-alpha.1"
	const val JAVA_VERSION = 17

	const val PROJECT_NAME = "Yumi Commons"
	const val PROJECT_URL = "https://github.com/YumiProject/yumi-commons"

	const val ORG_NAME = "Yumi Project"
	const val ORG_URL = "https://yumi.dev/"

	val DEVELOPERS = listOf(
			Developer("$ORG_NAME Development Team", "infra@yumi.dev")
	)

	const val LICENSE_NAME = "Mozilla Public License Version 2.0"
	const val LICENSE_URL = "https://www.mozilla.org/en-US/MPL/2.0/"

	private const val GIT_REPO = "github.com/YumiProject/yumi-commons"
	const val GIT_URL = "https://$GIT_REPO"
	const val GIT_CONNECTION = "scm:git:git://$GIT_REPO"
	val GIT_DEV_CONNECTION = "scm:git:ssh://" + GIT_REPO.replaceFirst('/', ':')

	const val PUBLICATION_NAME = "mavenJava"
}
