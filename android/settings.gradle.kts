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
        google()
        mavenCentral()
    }
}

rootProject.name = "Noor"

include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:navigation")
include(":core:media")
include(":domain")
include(":data")

// :baseline-profile is a Macrobenchmark module for CI/physical-device profiling only.
// It is excluded here because the androidx.benchmark plugin requires com.android.library
// but this module uses com.android.test. Enable it on a machine with a connected device.
// include(":baseline-profile")
