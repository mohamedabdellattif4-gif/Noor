plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val noorVersionCode = providers.gradleProperty("noor.versionCode")
    .orElse(providers.environmentVariable("NOOR_VERSION_CODE"))
    .orElse("1")
    .get()
    .toInt()
val noorVersionName = providers.gradleProperty("noor.versionName")
    .orElse(providers.environmentVariable("NOOR_VERSION_NAME"))
    .orElse("1.0.0")
    .get()

require(noorVersionCode > 0) { "Version code must be a positive integer." }
require(Regex("""\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.-]+)?""").matches(noorVersionName)) {
    "Version name must use semantic versioning, for example 1.2.3 or 1.2.3-rc.1."
}

val signingValues = mapOf(
    "storeFile" to providers.environmentVariable("NOOR_KEYSTORE_PATH").orNull,
    "storePassword" to providers.environmentVariable("NOOR_KEYSTORE_PASSWORD").orNull,
    "keyAlias" to providers.environmentVariable("NOOR_KEY_ALIAS").orNull,
    "keyPassword" to providers.environmentVariable("NOOR_KEY_PASSWORD").orNull,
)
val suppliedSigningValues = signingValues.values.count { it != null }
if (suppliedSigningValues !in setOf(0, signingValues.size)) {
    throw GradleException(
        "Release signing is partially configured. Provide all NOOR_KEYSTORE_* variables or none.",
    )
}
val hasReleaseSigning = suppliedSigningValues == signingValues.size
if (hasReleaseSigning) {
    val configuredStore = file(requireNotNull(signingValues["storeFile"])).canonicalFile
    val repositoryRoot = rootDir.canonicalFile.toPath()
    require(configuredStore.isFile) { "Configured release keystore does not exist." }
    require(!configuredStore.toPath().startsWith(repositoryRoot)) {
        "Release keystore must be stored outside the source repository."
    }
}

android {
    namespace = "com.noor.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.noor.app"
        minSdk = 23
        targetSdk = 37
        versionCode = noorVersionCode
        versionName = noorVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(requireNotNull(signingValues["storeFile"]))
                storePassword = requireNotNull(signingValues["storePassword"])
                keyAlias = requireNotNull(signingValues["keyAlias"])
                keyPassword = requireNotNull(signingValues["keyPassword"])
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        create("nonMinifiedRelease") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        animationsDisabled = true
        // Managed virtual devices (AVD-based instrumented tests) are not used on Replit.
        // The pixel6Api35 managed device is defined in the baseline-profile module for
        // Macrobenchmark runs. AGP 9.2.1 changed the managedDevices.devices container API;
        // re-add this block when running device tests on a machine with an Android emulator.
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = true
        // AndroidGradlePluginVersion / NewerVersionAvailable: versions are deliberately
        // pinned; upgrading AGP or Kotlin is a separate release-gate decision.
        disable += setOf(
            "AndroidGradlePluginVersion", // versions are deliberately pinned
            "NewerVersionAvailable",      // versions are deliberately pinned
            "NotShrinkingResources",      // nonMinifiedRelease disables shrinking intentionally for profiling
        )
    }
}

hilt { enableAggregatingTask = true }


dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:media"))
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.profileinstaller)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
