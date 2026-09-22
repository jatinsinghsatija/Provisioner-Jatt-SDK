pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri(rootDir.resolve("repo")) }
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "provisioner_jatt_android"
include(":provisioner-jatt")
include(":example")
include(":example-java")
