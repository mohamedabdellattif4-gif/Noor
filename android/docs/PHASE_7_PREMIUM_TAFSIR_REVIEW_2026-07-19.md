# Phase 7 — Premium Tafsir Rebuild Review

Date: 2026-07-19

## Scope

Phase 7 was rebuilt from the Phase 6 Premium Audio archive rather than accepted from the earlier
partial tafsir implementation. The locked Kotlin, Jetpack Compose, Material 3, MVVM, Clean
Architecture, Repository, Hilt, Room, Navigation Compose, Coroutines, offline-first, prebuilt SQLite,
and single-activity architecture remains unchanged.

## Baseline review findings

The Phase 6 archive contained an initial tafsir path, but it was not safe to certify as a completed
phase. The review identified these risks:

1. Navigation passed `ayahId`, surah number, and ayah number independently. A malformed or stale route
   could therefore pair the displayed Quran text with the wrong remote tafsir coordinates.
2. The remote response was not required to return the same surah and ayah that were requested.
3. QuranEnc footnotes were discarded instead of preserved byte-for-byte as provider content.
4. Concurrent cache misses or manual refreshes could create duplicate network calls for the same ayah.
5. The old screen did not distinguish cached content from newly fetched content and had incomplete
   loading, refresh, failure-feedback, preview, and Compose-test coverage.
6. The Room schema had no column for provider footnotes.

## Rebuilt architecture

### Canonical identity

- The tafsir route now contains only `ayahId`.
- `TafsirViewModel` validates that ID and loads the canonical `Ayah` from `QuranRepository`.
- The canonical `Ayah` object is the only coordinate source passed to `TafsirRepository` and the
  remote data source.
- Invalid or missing ayah IDs fail safely without contacting the tafsir provider.

### Remote boundary

- Uses the fixed QuranEnc HTTPS host and the per-ayah Arabic Muyassar endpoint.
- Runs blocking transport on `Dispatchers.IO`.
- Disables redirects and URL caching.
- Applies connection/read timeouts and a one-MiB response cap.
- Requires HTTP 200 and a JSON content type.
- Rejects blank or malformed responses.
- Validates returned `sura` and `aya` against the canonical requested ayah.
- Preserves `translation` and optional `footnotes` without editing provider content.
- Stores clear source identity for the UI: `التفسير الميسر — QuranEnc.com`.

### Offline-first repository

- Returns Room cache immediately for normal reads.
- Fetches and persists only on a cache miss or explicit refresh.
- Uses a per-ayah mutex to deduplicate concurrent cache misses.
- Uses fetch generations to deduplicate concurrent forced refreshes that began from the same state.
- Rejects a remote result whose `ayahId` does not match the canonical ayah before writing to Room.
- Keeps existing cached content intact when refresh fails.
- Marks domain content as `CACHE` or `NETWORK` for honest UI status.

### Database migration

- Room schema version increased from 3 to 4.
- `MIGRATION_3_4` adds nullable `footnotes TEXT` to `tafsirs`.
- The prepackaged database generator and generated `noor.db` now use schema/user version 4.
- Database integrity, foreign keys, 114-surah count, and 6,236-ayah count remain valid.
- New prepackaged database SHA-256:
  `59ae8dac5af0cdc9bb9bfcb767aef0222d59cf97ee21c477627d17cef64c4960`.

## Premium UI

The rebuilt screen is native Compose and follows Noor's emerald, ivory, and muted-gold design system:

- canonical Uthmani ayah card;
- dedicated explanation card;
- optional provider-footnotes card;
- clear source and cache/network status card;
- loading and retry states;
- manual refresh with duplicate-refresh protection;
- snackbar feedback while preserving already loaded content after refresh failure;
- Arabic RTL and dark-theme previews;
- stable semantic test tags and resource-backed text.

## Test coverage

Phase-specific source tests contain 23 tests:

- 7 ViewModel tests;
- 6 repository/concurrency tests;
- 4 QuranEnc parser tests;
- 4 Compose UI tests;
- 2 domain-invariant tests.

The existing Room instrumentation suite also adds a tafsir cache/footnotes persistence test, bringing
the directly relevant Phase 7 test inventory to 24 tests. Repository-wide phase verification detects
29 tafsir/database-related test methods in the selected suites.

Coverage includes:

- canonical ayah resolution and invalid navigation arguments;
- initial failure and retry;
- refresh success, refresh failure, retained content, and one-shot feedback;
- duplicate refresh suppression;
- cache-first behavior and persistence;
- concurrent miss and concurrent refresh deduplication;
- mismatched domain identity rejection;
- provider-coordinate mismatch rejection;
- malformed/empty provider content;
- optional footnotes persistence and display;
- loaded, refreshing, and error Compose states.

## Verification results

Passed in this environment:

- `python3 tools/verify_final.py`
- `python3 tools/verify_phase4_premium_search.py`
- `python3 tools/verify_phase5_premium_bookmarks.py`
- `python3 tools/verify_phase6_premium_audio.py`
- `python3 tools/verify_phase7_premium_tafsir.py`
- all repository XML parsing;
- SQLite `integrity_check` and schema/content assertions;
- pure Kotlin tafsir model compilation and behavior with warnings treated as errors;
- final verifier's Android-free Kotlin compilation and FTS host benchmark.

No files were deleted.

## External Android gate

A complete Android Gradle build was not executable in this sandbox because the uploaded archive does
not contain `gradle-wrapper.jar`, the environment has no configured Android SDK, and its network cannot
complete the wrapper bootstrap. Consequently, Compose compiler validation, KSP/Hilt/Room generated
code, Android Lint, unit-test execution through AGP, emulator instrumentation, and physical-device
network/offline/RTL/font-scaling checks remain mandatory on Android Studio or configured CI.

Use:

```bash
./bootstrap-gradle-wrapper.sh
./gradlew clean \
  :domain:test \
  :data:testDebugUnitTest \
  :app:testDebugUnitTest \
  :data:connectedDebugAndroidTest \
  :app:connectedDebugAndroidTest \
  :app:assembleDebug \
  :app:lintDebug \
  --warning-mode=all
```

After the first successful Android build, commit Room's generated schema 4 JSON under
`data/schemas` and run the migration instrumentation test on at least API 23 and the release target
API.

## Status

Phase 7 is rebuilt and source-level verified. It is not labeled device-verified or release-complete
until the external Android build, lint, generated-code, instrumentation, provider-version, and
physical-device gates pass.
