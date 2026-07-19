---
name: Android build environment on Replit
description: How to set up JDK, Gradle, and Android SDK for building Android projects on Replit, and what to watch for.
---

# Android Build Environment on Replit

## Required exports (each shell session)
```bash
export ANDROID_HOME=$HOME/android-sdk
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$HOME/gradle-installs/gradle-9.4.1/bin:$HOME/android-sdk/cmdline-tools/latest/bin:$HOME/android-sdk/platform-tools:$PATH
```

## Setup steps (already done for Noor)
1. `installSystemDependencies({ packages: ["jdk17", "curl", "wget", "which"] })` via the package-management skill
2. Download Gradle binary zip to /tmp, extract to `~/gradle-installs/` (NOT /opt — read-only)
3. Download Android cmdline-tools zip to /tmp, unzip to `~/android-sdk/cmdline-tools/latest/`
4. Accept SDK licenses: `yes | sdkmanager --licenses`
5. Install required platform + build-tools: `sdkmanager "platforms;android-37.0" "build-tools;35.0.0" "platform-tools"`
6. Symlink platform folder if needed: `ln -sf ~/android-sdk/platforms/android-37.0 ~/android-sdk/platforms/android-37`
7. Download Gradle wrapper JAR from GitHub (services.gradle.org returned 404): `https://github.com/gradle/gradle/raw/v<VERSION>/gradle/wrapper/gradle-wrapper.jar`
8. Create `local.properties`: `sdk.dir=/home/runner/android-sdk`

**Why:** /opt is read-only on Replit; the Gradle wrapper JAR is not committed and the services.gradle.org URL returns 404; Android SDK platform 37 is listed as "android-37.0" not "android-37" in sdkmanager.
