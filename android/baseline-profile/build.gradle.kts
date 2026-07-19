plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.benchmark)
}

android {
    namespace = "com.noor.baselineprofile"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    buildTypes {
        create("benchmark") {
            matchingFallbacks += listOf("release")
            isDebuggable = true
        }
        create("nonMinifiedRelease") {
            matchingFallbacks += listOf("release")
            isDebuggable = true
        }
    }

    testOptions {
        animationsDisabled = true
        // Managed virtual devices removed — see app/build.gradle.kts comment.
    }
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.ext.junit.ktx)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
}
