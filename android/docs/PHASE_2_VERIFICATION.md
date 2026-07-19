# Phase 2 verification

## Scope completed

Phase 2 implements the shared Material 3 design system only. Room, DataStore, Media3,
WorkManager, Quran data, and feature screens remain outside this phase.

## Review findings fixed

- Removed a stale `Type.kt` file that duplicated the `NoorTypography` declaration.
- Replaced a preview reference to the removed `NoorButton` API with `NoorFilledButton`.
- Made `NoorLoadingIndicator` caller-sized instead of always full-screen.
- Added a localized accessibility label to the loading indicator.
- Added API 27-qualified navigation-bar appearance resources.
- Restored light/LTR and dark/RTL theme previews.
- Added a standard `Modifier` parameter to the shared top app bar.
- Centralized the platform launch-window background in light/night color resources.

## Completed checks

- Re-ran every Phase 1 static integrity check.
- Parsed the Gradle Version Catalog.
- Parsed all 15 XML files.
- Inspected all 20 Kotlin files for package declarations, unresolved markers, stale APIs,
  physical left/right padding, and misplaced hardcoded Compose colors.
- Verified exactly one `NoorTypography` declaration.
- Verified light, dark, optional dynamic-color, typography, shape, and spacing integration.
- Verified matching default and Arabic resource keys.
- Verified RTL manifest configuration and Arabic/dark previews.
- Verified 18 critical foreground/background pairs at a contrast ratio of at least 4.5:1.
- Verified that no external font binaries were bundled.
- Verified that Phase 3+ dependencies remain absent.
- Checked shell-script syntax and project text-file hygiene.
- Compiled `:core:common` independently for JVM 17 with warnings treated as errors.

Run the repository checks locally:

```bash
python3 tools/verify_phase1.py
python3 tools/verify_phase2.py
```

## Android build gate

The generation environment provides JDK 21, but it has no Android SDK and no Gradle
wrapper JAR. Shell processes also cannot retrieve the required Gradle binary. Running
`./gradlew --version` therefore stops at `GradleWrapperMain` before project configuration.

The definitive Android gate must run on a workstation with JDK 17 and Android SDK
Platform 37:

```bash
./bootstrap-gradle-wrapper.sh
./gradlew clean :core:designsystem:assembleDebug :app:assembleDebug lintDebug --warning-mode=all
```

Windows:

```powershell
./bootstrap-gradle-wrapper.ps1
./gradlew.bat clean :core:designsystem:assembleDebug :app:assembleDebug lintDebug --warning-mode=all
```

Phase 3 must not begin until this command succeeds without unresolved warnings.
