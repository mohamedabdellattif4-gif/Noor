# Noor

Noor is a production-oriented, offline-first Android Quran application implemented with Kotlin,
Jetpack Compose, Material 3, Clean Architecture, MVVM, Hilt, Room, Navigation Compose, Coroutines,
Flow, DataStore, Media3, WorkManager, and a Gradle Version Catalog.

## Product capabilities

- Complete offline Quran corpus: 114 surahs and 6,236 ayahs.
- Home screen with surah list and continue-reading position.
- Reader with scalable Quran typography, bookmarks, tafsir, and ayah/surah audio controls.
- Arabic-normalized full-text prefix search backed by Room FTS4.
- Bookmark management.
- Background audio through Media3 `MediaSessionService` with three reciters.
- On-demand Arabic tafsir with bounded HTTPS transport and local cache.
- Curated offline adhkar and duas with categories, normalized search, local favorites, repeat counters, and a standalone tasbih.
- Optional local morning/evening reminders through WorkManager and notification permission.
- Theme, dark mode, dynamic-color opt-in, reciter, and Quran text-size settings in DataStore.
- Periodic stale-tafsir cleanup through Hilt-integrated WorkManager.
- Arabic/English resources, RTL support, privacy notice, and source/license attribution.

## Modules

```text
:app                 Compose UI, navigation, feature ViewModels and application startup
:core:common         Android-free result/text utilities
:core:designsystem   Noor Material 3 theme, tokens, and shared components
:core:navigation     Typed route construction constants
:core:media          Media3 controller, session service, playback state and URL boundary
:domain              Android-free models and repository contracts
:data                Room, DataStore, remote tafsir, repositories, Hilt and WorkManager
:baseline-profile    Baseline Profile generation and Macrobenchmark startup/reader measurements
```

The presentation layer depends on domain contracts rather than Room, DataStore, or network classes.
Media3 is isolated from feature screens by the `QuranAudioPlayer` contract.

## Data and licensing

The prepackaged Room database contains Tanzil Uthmani Quran text and quran-meta chapter/page metadata.
Displayed Tanzil text is retained byte-for-byte; a separate normalized field is generated only for
search. See `docs/DATA_ATTRIBUTIONS.md` and the bundled notices under
`app/src/main/assets/licenses/` before distribution.

Audio is streamed from EveryAyah and tafsir is requested from QuranEnc. Publisher confirmation of
current provider terms is a release-operation gate.

## Toolchain

- JDK 17
- Android Gradle Plugin 9.2.1
- Gradle 9.4.1
- Android SDK Platform 37
- minSdk 23, targetSdk 36, compileSdk 37
- Kotlin/Compose compiler plugin 2.3.10 (aligned with AGP 9.2 built-in Kotlin)

## Repository verification

The environment-independent review gate validates structure, architecture boundaries, Gradle/TOML,
XML/resources, security policy, complete Quran data integrity, exact Tanzil source fidelity, FTS
behavior/performance, generator reproducibility, release artifacts, Kotlin syntax, and Android-free
Kotlin compilation when a local compiler is available.

```bash
python3 tools/verify_build_foundation.py --allow-missing-wrapper-jar
python3 tools/verify_final.py
python3 tools/verify_hardening.py
python3 tools/verify_phase2_premium_home.py
python3 tools/verify_phase3_premium_reader.py
python3 tools/verify_phase4_premium_search.py
python3 tools/verify_phase5_premium_bookmarks.py
python3 tools/verify_phase6_premium_audio.py
python3 tools/verify_phase7_premium_tafsir.py
python3 tools/verify_phase8_premium_adhkar.py
```

## Android build and test gate

Gradle requires the Wrapper JAR to be committed for a fully self-contained checkout. The current
archive cannot retain that binary in this execution environment, so the project provides a bounded,
checksum-verified recovery bootstrap. Run it once before Gradle when the JAR is absent:

```bash
./bootstrap-gradle-wrapper.sh
./gradlew clean \
  :core:common:test \
  :core:media:test \
  :domain:test \
  :app:testDebugUnitTest \
  :data:assembleAndroidTest \
  :app:assembleDebug \
  :app:assembleBenchmark \
  :app:assembleNonMinifiedRelease \
  :baseline-profile:assembleBenchmark \
  :baseline-profile:assembleNonMinifiedRelease \
  :app:lintDebug \
  --warning-mode=all
./gradlew :data:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```

Windows equivalents are available through `bootstrap-gradle-wrapper.ps1` and `gradlew.bat`.
Commit the Room-generated schema JSON under `data/schemas` after the first successful Android build.


## Performance profiles

The app includes a conservative checked-in `app/src/main/baseline-prof.txt` and a separate
`:baseline-profile` Macrobenchmark module. Generate refreshed rules with the manual GitHub workflow
or, on a configured machine, run the managed-device benchmark task and install its output:

```bash
./gradlew :baseline-profile:pixel6Api35NonMinifiedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
python3 tools/update_baseline_profile.py
```

Use a physical device for release performance measurements. Emulator output is suitable for
repeatable profile generation, not for claiming startup or frame-time improvements.

## Signed release gate

Set `NOOR_VERSION_CODE`, `NOOR_VERSION_NAME`, and all four `NOOR_KEYSTORE_*` variables, with the
keystore stored outside the repository, then run:

```bash
tools/release_gate.sh
```

The gate validates the workstation, builds and tests the project, creates the signed AAB, and checks
its signature, Quran database, licenses, DEX payload, Baseline Profile, size, and checksum.

## Release status

The repository is a **release candidate**, not a signed/publishable Play artifact. The remaining
external gates are Android SDK/Gradle compilation and lint, generated-code/schema validation,
device tests, physical-device Macrobenchmark results, signed artifact creation, public privacy-policy/
support details, provider rights confirmation, and Play Console review. CI, CodeQL, dependency
submission/review, release preflight, and artifact verification are configured in the repository. See
`docs/FINAL_PROJECT_REPORT.md`, `docs/FINAL_VERIFICATION.md`, and `docs/RELEASE.md`.

## Premium UI status

The approved emerald, ivory, and muted-gold visual direction is implemented in the Home, Quran Reader, offline Search, Bookmarks, dedicated Audio, and rebuilt Tafsir screens. The memorial note is integrated inside the application UI, reading progress is backed by DataStore, the Reader preserves lazy Quran rendering and identity-safe Media3 controls, Search remains backed by the prebuilt Room FTS4 index with stale-result protection, bookmark removals are guarded against duplicate requests and surfaced failures, the Audio screen provides a full surah queue, seek/transport controls, reciter selection, screen-off playback wake handling, and subscriber-aware progress polling; and Tafsir now resolves canonical ayah identity, validates provider coordinates, preserves footnotes, deduplicates requests, and reports cache/network origin. Phase 8 adds a curated offline adhkar catalog, Arabic-normalized search, favorites, per-item counters, a standalone tasbih, and optional local morning/evening reminders. See:

- `docs/PHASE_2_PREMIUM_HOME_REVIEW_2026-07-18.md`
- `docs/PHASE_3_PREMIUM_READER_REVIEW_2026-07-18.md`
- `docs/PHASE_4_PREMIUM_SEARCH_REVIEW_2026-07-18.md`
- `docs/PHASE_5_PREMIUM_BOOKMARKS_REVIEW_2026-07-18.md`
- `docs/PHASE_6_PREMIUM_AUDIO_REVIEW_2026-07-19.md`
- `docs/PHASE_7_PREMIUM_TAFSIR_REVIEW_2026-07-19.md`
- `docs/PHASE_8_PREMIUM_ADHKAR_REVIEW_2026-07-19.md`

