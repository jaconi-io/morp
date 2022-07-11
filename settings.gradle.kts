pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://repo.spring.io/release")
    }
}

plugins {
    id("com.gradle.enterprise") version("3.10.2")
}

rootProject.name = "morp"

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        capture {
            isBuildLogging = true
            isTestLogging = true
        }
    }
}