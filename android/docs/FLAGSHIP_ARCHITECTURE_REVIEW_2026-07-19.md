# Noor Flagship Architecture Review

**Review date:** 2026-07-19
**Baseline:** Phase 8 Premium Adhkar archive
**Baseline Git commit:** `bf8a8ecc7309a9cc51895363ed75f13519e401ef`
**Result:** Source-level verified; Android build and device verification remain external release gates.

## Executive Summary

This review preserved Noor's existing MVVM and Clean Architecture boundaries. No feature was removed, no placeholder implementation was introduced, and no production `TODO` or `FIXME` remains. The work focused on reproducible defects and release risks rather than speculative rewrites.

The review corrected repeated notification navigation, persistence failure handling, accessibility semantics, Room migration coverage, CI coverage, release tooling reliability, and stale security/release documentation. It also added focused regression tests and a dedicated flagship verification gate.

The prepackaged Quran database remains unchanged at 114 surahs and 6,236 ayahs, with SQLite `integrity_check = ok` and Room schema version 4.

---

## Task 1 — Repeated notification navigation

### Problem

The activity stored the notification deep-link request as a persistent Boolean. After the first warm notification intent set the value to `true`, later notification taps could produce the same Compose state and therefore fail to trigger navigation again.

### Impact

A user could tap an Adhkar reminder while Noor was already open and see no navigation response after the first successful tap. This was a real state-event modeling defect.

### Solution

Introduced an immutable `AppLaunchNavigation` model. The activity now keeps a fixed cold-start destination and a monotonically changing one-shot request ID for every warm Adhkar intent. `NoorApp` reacts to the request ID and performs single-top navigation.

### Files Modified

- `app/src/main/kotlin/com/noor/app/AppLaunchNavigation.kt`
- `app/src/main/kotlin/com/noor/app/MainActivity.kt`
- `app/src/main/kotlin/com/noor/app/ui/NoorApp.kt`
- `app/src/test/kotlin/com/noor/app/AppLaunchNavigationTest.kt`
- `tools/verify_phase8_premium_adhkar.py`
- `tools/verify_flagship_review.py`

### Reason

Navigation requests are events, not persistent Boolean state. A changing request identifier preserves repeated user intent without introducing a global event bus or changing navigation architecture.

### Verification

- Four focused JVM tests cover cold start, repeated warm requests, non-Adhkar start, and counter rollover.
- Phase 8 verification passed.
- Flagship verification passed.

---

## Task 2 — Settings persistence reliability and accessibility

### Problem

Settings writes were launched without failure handling. A DataStore or storage exception could become an uncaught coroutine failure with no user feedback. Radio-button and switch rows delegated click behavior to small child controls instead of exposing the full row as one accessible control. Preference-file corruption also had no recovery handler.

### Impact

Settings changes could fail silently or destabilize the screen. TalkBack focus and touch targets were less predictable, and a corrupted preferences file could prevent settings collection.

### Solution

- Added buffered one-shot settings events and cancellation-safe exception handling.
- Added a Snackbar for persistence failures.
- Split the route from the stateless screen to improve testability.
- Applied row-level `selectable`, `selectableGroup`, and `toggleable` semantics with appropriate roles.
- Enforced minimum 48 dp targets.
- Added `ReplaceFileCorruptionHandler { emptyPreferences() }` to the shared DataStore delegate.
- Added stable UI test tags and bilingual failure text.

### Files Modified

- `app/src/main/kotlin/com/noor/app/ui/settings/SettingsRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/settings/SettingsViewModel.kt`
- `data/src/main/kotlin/com/noor/data/settings/NoorPreferencesDataStore.kt`
- `app/src/test/kotlin/com/noor/app/ui/settings/SettingsViewModelTest.kt`
- `app/src/androidTest/kotlin/com/noor/app/ui/settings/SettingsPremiumUiTest.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ar/strings.xml`

### Reason

Persistence is an I/O boundary and must be treated as fallible. Row-level semantics match Material and Compose accessibility patterns while keeping the existing Material 3 design.

### Verification

- ViewModel tests cover repository failures and successful writes.
- Compose tests cover radio and switch semantics.
- English and Arabic resource keys are in parity.
- DataStore corruption recovery is enforced by flagship verification.

---

## Task 3 — Quran Reader persistence safety

### Problem

Bookmark, text-size, and reading-progress writes could throw from `viewModelScope`. Repeated bookmark taps could enqueue conflicting writes before state propagation completed.

### Impact

A transient storage failure could produce an unhandled coroutine exception. Rapid taps could produce duplicate or contradictory bookmark operations. Users received no explanation when a write failed.

### Solution

- Added per-ayah pending bookmark state.
- Ignored duplicate bookmark taps while the same ayah update is pending.
- Calculated the desired bookmark state once from the current snapshot.
- Added cancellation-safe exception handling for bookmarks, text scale, and reading progress.
- Added one-shot reader events and localized Snackbar messages.
- Disabled and visually de-emphasized the bookmark action while pending.
- Prevented repeated reading-progress error messages until a later successful write.

### Files Modified

- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderUiState.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderViewModel.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderComponents.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderPreview.kt`
- `app/src/test/kotlin/com/noor/app/ui/reader/ReaderViewModelTest.kt`
- `app/src/androidTest/kotlin/com/noor/app/ui/reader/ReaderPremiumUiTest.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ar/strings.xml`

### Reason

The reader is a flagship path. User actions must remain deterministic even when storage is slow or unavailable, without changing repository contracts or business rules.

### Verification

- Tests cover duplicate taps, bookmark failure, text-scale failure, and reading-progress failure.
- Phase 3 Premium Reader verification passed.
- No Quran content or reader navigation contract changed.

---

## Task 4 — Adhkar reminder consistency

### Problem

A WorkManager scheduling exception inside the reminder settings flow could terminate the state pipeline. The UI could continue showing an enabled setting even though scheduling had failed, with no distinct explanation.

### Impact

Reminder configuration could become misleading and the screen could stop receiving future settings updates.

### Solution

- Caught scheduler failures inside `onEach` without terminating the settings flow.
- Preserved coroutine cancellation.
- Added a distinct `SchedulingFailed` event and localized user message.
- Kept repository-write failures separate from WorkManager scheduling failures.
- Made entire reminder rows accessible switches with minimum touch targets.

### Files Modified

- `app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersViewModel.kt`
- `app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersRoute.kt`
- `app/src/test/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersViewModelTest.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ar/strings.xml`

### Reason

Persisting a preference and synchronizing platform work are separate failure domains. Reporting them separately makes the state honest and keeps the Flow alive.

### Verification

- Tests prove scheduler failure does not terminate settings observation.
- Tests cover repository update failure.
- Phase 8 and flagship verification passed.

---

## Task 5 — Adhkar, Tasbih, and Compose accessibility/performance

### Problem

The Adhkar favorite action did not expose checked state, the Tasbih counter had a rigid fixed size that was vulnerable to large-font layouts, and several static LazyColumn entries did not declare content types.

### Impact

TalkBack users received weaker state feedback. Large fonts could constrain the counter. Compose had less information for item reuse in heterogeneous lazy lists.

### Solution

- Replaced the favorite icon action with `IconToggleButton` semantics and separate add/remove descriptions.
- Added explicit Tasbih button role, state description, click label, completion-disabled semantics, and merged descendants.
- Replaced the rigid counter size with `sizeIn` minimum dimensions.
- Added stable test tags.
- Added content types to static items in Home, Adhkar, Tasbih, and reminder lazy lists.
- Added automated contrast checks for fixed Noor palette pairs.

### Files Modified

- `app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/adhkar/TasbihRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/adhkar/AdhkarRemindersRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/home/HomeRoute.kt`
- `app/src/androidTest/kotlin/com/noor/app/ui/adhkar/AdhkarPremiumUiTest.kt`
- `app/src/androidTest/kotlin/com/noor/app/ui/adhkar/TasbihPremiumUiTest.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ar/strings.xml`
- `tools/verify_flagship_review.py`

### Reason

These changes improve semantics and lazy-list reuse without changing visual identity, navigation, or business behavior.

### Verification

- Compose tests assert favorite on/off state.
- Compose tests assert Tasbih click availability and completed disabled state.
- Every application LazyColumn now has stable key and content-type coverage by source verification.
- Flagship palette contrast checks passed.

---

## Task 6 — Room migration confidence

### Problem

The repository documented migration support but did not contain a direct instrumentation test that opened a real version-1 database and migrated it through `1 → 2 → 3 → 4`. Historical Room schema JSON files were also absent while some documentation implied stronger schema coverage than existed.

### Impact

A production upgrade defect could remain undetected. Hand-authoring missing schema JSON would create false confidence and could invalidate migration tests.

### Solution

- Added an instrumentation migration test that creates the documented version-1 schema, inserts Quran data, opens it with the current Room database and all migrations, and validates content normalization and FTS search after migration.
- Corrected schema documentation: current builds can generate schema 4, while historical schemas 1–3 must be recovered from authoritative tagged builds and must not be fabricated.
- Updated release and Google Play documentation to match Room version 4.

### Files Modified

- `data/src/androidTest/kotlin/com/noor/data/local/database/NoorDatabaseMigrationTest.kt`
- `data/build.gradle.kts`
- `data/schemas/README.md`
- `docs/RELEASE.md`
- `docs/GOOGLE_PLAY_READINESS.md`

### Reason

A direct migration test provides meaningful upgrade coverage without inventing historical artifacts. Accurate documentation is safer than an invalid `MigrationTestHelper` setup.

### Verification

- Flagship source verification confirms all migrations and the direct migration test are present.
- Prepackaged database integrity passed.
- The Android migration test is configured in the managed-device CI job but was not executed in this sandbox.

---

## Task 7 — CI, release, security, and Google Play gates

### Problem

CI did not run Phase 7 or Phase 8 verification, omitted data-module unit tests, omitted release lint, and had no device job for Room and Compose instrumentation tests. Release tooling could also hang indefinitely while invoking external tools. Several security and release documents described stale Room/component facts.

### Impact

Regressions in recent features, migrations, accessibility, or release-only lint could reach a merge. A stalled external command could block CI indefinitely. Stale documentation could mislead a release reviewer.

### Solution

- Expanded source verification through Phase 8 and the flagship gate.
- Added data and app unit-test tasks, release lint, benchmark/non-minified builds, and artifact verification.
- Added a Gradle Managed Device (`Pixel 6`, API 35) for app and data instrumentation tests.
- Added explicit timeouts to release-preflight, hardening, Phase 7 compilation, and artifact-signature subprocesses.
- Corrected security, privacy, release, and Google Play documents.
- Preserved strict release settings: R8 enabled, resource shrinking enabled, release not debuggable, signing sourced only from complete environment configuration, keystore required outside the repository, cleartext disabled, backup disabled, and minimum permissions retained.

### Files Modified

- `.github/workflows/android-ci.yml`
- `app/build.gradle.kts`
- `data/build.gradle.kts`
- `tools/release_gate.sh`
- `tools/release_preflight.py`
- `tools/verify_android_artifact.py`
- `tools/verify_hardening.py`
- `tools/verify_phase2_premium_home.py`
- `tools/verify_phase7_premium_tafsir.py`
- `tools/verify_phase8_premium_adhkar.py`
- `tools/verify_flagship_review.py`
- `docs/RELEASE.md`
- `docs/SECURITY_REVIEW.md`
- `docs/GOOGLE_PLAY_READINESS.md`
- `docs/PRIVACY_POLICY.md`
- `docs/PHASE_8_PREMIUM_ADHKAR_REVIEW_2026-07-19.md`

### Reason

Production readiness must be enforced by repeatable gates, not prose. Time-bounded tools and device tests reduce false-green and permanently-running CI states.

### Verification

- Foundation verification passed.
- Final verification passed.
- Hardening verification passed.
- Phase 2 Premium Home through Phase 8 Premium Adhkar verification passed.
- Flagship verification passed with 254 checks.
- Git diff whitespace check, shell syntax, Python compilation, XML parsing, YAML parsing, and SQLite integrity all passed.

---

## Task 8 — Verification tooling integrity

### Problem

Two legacy verification assumptions had drifted from production code: the Phase 2 verifier counted an obsolete method form, and the Phase 8 report contained trailing whitespace that caused final verification to fail. Some subprocess-based verifiers had no timeout.

### Impact

The project could report a false failure or hang, reducing trust in its own quality gates.

### Solution

Updated the verifiers to assert current canonical behavior, corrected the document whitespace, expanded Phase 8 test discovery, and added subprocess limits.

### Files Modified

- `tools/verify_phase2_premium_home.py`
- `tools/verify_phase7_premium_tafsir.py`
- `tools/verify_phase8_premium_adhkar.py`
- `tools/verify_hardening.py`
- `tools/release_preflight.py`
- `tools/verify_android_artifact.py`
- `docs/PHASE_8_PREMIUM_ADHKAR_REVIEW_2026-07-19.md`

### Reason

A quality gate must test the intended invariant and terminate deterministically.

### Verification

All updated verification tools completed successfully in the working tree.

---

## Documented External Release Gates

The following items are not represented as completed because this sandbox cannot prove them:

1. **Gradle Wrapper JAR:** `gradle-wrapper.jar` is absent from the supplied archive. The checksum-pinned bootstrap remains available. The official Gradle 9.4.1 wrapper checksum is recorded, but the sandbox blocked binary transfer and DNS access, so no unverified binary was inserted.
2. **Android build:** Android SDK is absent and the host provides JDK 21 while the project build is pinned to JDK 17. No Gradle Android task was executed locally.
3. **Device verification:** Managed-device Room, parser, Compose, TalkBack, RTL, large-font, dark-mode, audio-background, notification, and process-recreation tests are configured for CI but were not run here.
4. **Release artifact:** No signed production AAB/APK was generated or uploaded to Play Console.
5. **Room historical schemas:** Authoritative JSON schemas 1–3 must be recovered from trusted historical builds before adding `MigrationTestHelper` coverage.
6. **Publishing decisions:** The final globally unique application ID, publisher identity, signing key custody, store listing, Data Safety answers, privacy-policy hosting, content/provider rights, and closed-testing approval remain owner/release-manager decisions.
7. **Performance evidence:** Baseline Profile infrastructure exists, but macrobenchmark results must be captured on a controlled Android device before making startup-performance claims.

## Final Status

**Source-level architecture, security, accessibility, persistence, and release-gate review: PASSED.**

**Android build/device/Play release status: NOT YET VERIFIED.**
