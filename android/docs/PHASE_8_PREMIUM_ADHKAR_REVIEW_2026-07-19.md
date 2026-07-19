# Phase 8 — Premium Adhkar and Duas review

Date: July 19, 2026
Status: **Implemented and source-level verified**

## Scope delivered

Phase 8 adds an offline-first adhkar area without changing the Quran database or the Phase 7 tafsir
schema. The implementation includes:

- A curated bundled catalog of 21 entries across morning, evening, after-prayer, sleep, waking, and
  general remembrance categories.
- Arabic-normalized local search that composes with category and favorites filters.
- Favorites persisted in the existing encrypted-by-platform private DataStore scope; no account or
  remote sync is introduced.
- A bounded per-item repetition counter whose value cannot exceed the card's configured count.
- A standalone tasbih with local count, reset, phrase selection, 33/100 user-selectable targets, and
  haptic feedback.
- Optional morning and evening reminders using unique periodic WorkManager jobs.
- Android 13+ notification permission requested only when the user enables a reminder.
- Notification deep-link handling for both cold start and `onNewIntent` while the activity exists.
- Updated Arabic and English resources, privacy text, data attribution, README, navigation, and Home
  quick actions.

## Architecture

The feature follows the existing module boundaries:

- `:domain` owns `Dhikr`, `AdhkarCategory`, reminder settings, repository contracts, and the scheduler
  boundary.
- `:data` owns the bundled catalog and DataStore-backed favorites/reminder preferences.
- `:app` owns Compose screens, ViewModels, WorkManager scheduling, notification delivery, permission
  handling, and navigation.
- The Room database remains at version 4. Phase 8 adds no table and no migration.

## Content safeguards

- Every bundled card has a stable ID, category, Arabic text, reference label, and positive count.
- The starter catalog excludes an entry whose grading was not sufficiently unambiguous for this
  release review.
- General remembrance cards do not prescribe an unsupported fixed repetition count.
- User-selectable tasbih targets are presented as counting controls, not religious rulings.
- A qualified Arabic proofread and independent source review remain a publication gate. This source
  review is not represented as a scholarly certification.

## Reliability and privacy

- Adhkar reading, search, favorites, counters, and tasbih are offline.
- Reminder choices remain in the same private local DataStore used by other Noor preferences.
- Notifications contain no sensitive content and transmit no preference or usage data.
- WorkManager delivery is intentionally described as approximate because device battery policies may
  defer periodic work.
- The Worker checks notification permission before posting and uses immutable pending intents.

## Verification performed

The repository-specific Phase 8 gate confirms:

- 21 unique bundled entries and all six categories.
- Arabic/English string-key parity and valid XML.
- Navigation, Home integration, ViewModel filtering/counter behavior, DataStore keys, WorkManager,
  permission handling, notification deep-linking, and Hilt bindings.
- 12 focused unit/instrumentation test cases are present in source.
- Android-free Phase 8 models and the bundled catalog compile with `kotlinc`.
- The prepackaged database passes `PRAGMA integrity_check` and remains 114 surahs / 6,236 ayahs.
- Foundation, final, hardening, and Phase 2 through Phase 8 static/source gates all exit successfully.

See `PHASE_8_VERIFICATION_OUTPUT.txt` and `PHASE_8_BUILD_FACTS.json` for machine-readable facts and
captured output.

## Verification not performed in this environment

A real Android Gradle build, lint run, generated Hilt/Compose compilation, unit-test execution through
Gradle, emulator/device instrumentation, notification delivery, process recreation, RTL visual QA,
font scaling, and battery-optimization behavior were not executed here. The supplied archive does not
contain `gradle/wrapper/gradle-wrapper.jar`, and this environment does not provide an Android SDK.

On a configured Android workstation, run:

```bash
./bootstrap-gradle-wrapper.sh
./gradlew clean \
  :domain:test \
  :app:testDebugUnitTest \
  :app:assembleDebug \
  :app:lintDebug \
  --warning-mode=all
./gradlew :app:connectedDebugAndroidTest
```

Phase 8 must not be called device-verified until those commands and manual device scenarios pass.
