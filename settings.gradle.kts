pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
    id("com.gradle.enterprise") version "3.14"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "1.11.1"
}

gradleEnterprise {
    server = "https://ge.solutions-team.gradle.com"
    buildScan {
        publishAlways()
        isUploadInBackground = System.getenv("CI") == null
        tag("onboardingProject")
        capture {
            isTaskInputFiles = true
        }
    }
    buildCache {
        local {
            directory = rootProject.projectDir.resolve("build-cache")
        }
    }
}

rootProject.name = "uptodateReproducer"
