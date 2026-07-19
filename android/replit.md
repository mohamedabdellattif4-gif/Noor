# Noor — Android Quran App

## Project overview

Noor is a production-oriented, offline-first Android Quran application built with:

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room (prepackaged Quran corpus — 114 surahs, 6,236 ayahs)
- **Async**: Coroutines + Flow
- **Media**: Media3 (ExoPlayer + MediaSessionService)
- **Preferences**: DataStore
- **Background work**: WorkManager
- **Navigation**: Navigation Compose (typed routes via `:core:navigation`)

## Modules

```
:app                 Compose UI, navigation, feature ViewModels
:core:common         Android-free result/text utilities
:core:designsystem   Noor Material 3 theme, tokens, shared components
:core:navigation     Typed route construction constants
:core:media          Media3 controller, session service, playback state
:domain              Android-free models and repository contracts
:data                Room, DataStore, remote tafsir, repositories, Hilt, WorkManager
```

## Building on Replit

### Prerequisites (already set up in this repl)

| Tool | Location |
|------|----------|
| JDK 17 | Installed via Nix (`jdk17`) |
| Gradle 9.4.1 binary | `~/gradle-installs/gradle-9.4.1/` |
| Gradle wrapper JAR | `gradle/wrapper/gradle-wrapper.jar` (downloaded from GitHub) |
| Android SDK | `~/android-sdk/` (platform 37.0, build-tools 35.0.0) |
| `local.properties` | `sdk.dir=/home/runner/android-sdk` |

### Environment setup (required in every shell session)

```bash
export ANDROID_HOME=$HOME/android-sdk
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$HOME/gradle-installs/gradle-9.4.1/bin:$HOME/android-sdk/cmdline-tools/latest/bin:$HOME/android-sdk/platform-tools:$PATH
```

### Build commands

```bash
# Debug APK
./gradlew assembleDebug --no-daemon

# Release AAB (unsigned — no keystore configured)
./gradlew bundleRelease --no-daemon

# Unit tests
./gradlew testDebugUnitTest --no-daemon

# Lint
./gradlew lintDebug --no-daemon

# Full production check
./gradlew assembleDebug bundleRelease testDebugUnitTest lintDebug --no-daemon
```

### Outputs

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`
- Lint report: `app/build/intermediates/lint_intermediate_text_report/debug/lintReportDebug/lint-results-debug.txt`

### Signed release

To build a signed release APK/AAB, set these environment variables before building:

```
NOOR_VERSION_CODE=<int>
NOOR_VERSION_NAME=<semver>
NOOR_KEYSTORE_PATH=<path outside repo>
NOOR_KEYSTORE_PASSWORD=<password>
NOOR_KEY_ALIAS=<alias>
NOOR_KEY_PASSWORD=<password>
```

### Known exclusions

- **`:baseline-profile`** — excluded from `settings.gradle.kts`. This Macrobenchmark module requires a connected physical/virtual device and a different AGP plugin combination. Re-enable on a machine with an emulator.
- **Managed virtual devices** — removed from `testOptions.managedDevices` in `app`, `data`, and `baseline-profile` build files (AGP 9.2.1 API changed). Device tests via AVDs must be run on a machine with an emulator.
- **Instrumented tests** — cannot run on Replit (no Android emulator); unit tests run fully.

## User preferences

- Preserve Clean Architecture + MVVM + Hilt + Room + Compose structure.
- Fix only real compile/runtime issues; do not restructure or migrate.
- Do not remove or rewrite existing features.
