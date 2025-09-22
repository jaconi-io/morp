plugins {
	id("com.gradle.develocity") version "4.2"
}

rootProject.name = "morp"

develocity {
	buildScan {
		termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
		termsOfUseAgree = "yes"

		capture {
			buildLogging = true
			testLogging = true
		}
	}
}
