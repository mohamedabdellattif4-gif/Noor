# Noor Phase 4 — Premium Offline Quran Search Review

Date: 2026-07-18

## Scope

This phase implements only the premium Quran search experience after the approved Home and Reader phases. It preserves Room FTS4, the prebuilt database, Arabic normalization, navigation, and the locked architecture. Bookmarks, Tafsir, and Settings are not redesigned in this phase.

## Existing implementation review

### 1. Completed results remained visible while a new query was pending

The previous ViewModel updated the text query and started a debounced search without clearing the prior result list. During the 300 ms debounce and database call, users could see results for the old query underneath a loading indicator.

**Root cause:** result data was not associated with the active query generation, and the loading transition copied the previous result list forward.

### 2. Cancellation alone did not guarantee stale-result isolation

The previous implementation canceled the prior coroutine job. Room suspend queries are cancellation-aware, but the repository contract does not guarantee that every future data source will stop immediately. A data source that completes after cancellation could still attempt to publish an older result.

**Root cause:** search correctness relied only on cooperative coroutine cancellation and did not validate that a completed response still belonged to the latest visible query.

### 3. Search errors had no recovery action

The UI showed a localized error message but exposed no retry control. The user had to modify the query to trigger another request.

**Root cause:** `SearchViewModel` had no explicit retry operation and the generic search screen had no state-specific action panel.

### 4. The generic UI did not match the approved Noor visual system

The previous screen used the shared top app bar, a default outlined field, generic cards, and plain state messages. It did not use the approved emerald, ivory, muted-gold, and Islamic geometric presentation.

**Root cause:** the functional search phase predated the approved premium visual direction.

### 5. Keyboard and large-list behavior were incomplete

The text field did not expose a Search IME action, the screen did not account for the software keyboard with `imePadding`, and result items did not provide a `contentType` for lazy-list reuse.

**Root cause:** the initial implementation focused on FTS correctness rather than production interaction and rendering details.

## Production fixes applied

- Added a premium emerald search header and warm ivory search surface.
- Added a clear-query action with a local vector-style Canvas icon.
- Added Search IME configuration and keyboard dismissal.
- Added keyboard-safe `imePadding`.
- Added dedicated initial, loading, empty, and error panels.
- Added an explicit retry action that repeats the current query without another debounce.
- Clear prior results immediately when a different valid query begins.
- Added a monotonic request generation guard so a late response cannot overwrite a newer query.
- Preserved the 300 ms debounce, 200-character input bound, token bounds, and 100-result cap.
- Added premium result cards with surah, ayah, juz, page, Uthmani text, and decorative ayah medallions.
- Preserved direct rendering of `Ayah.textUthmani` from the verified prebuilt database.
- Added stable item keys and a result `contentType` to the `LazyColumn`.
- Added Arabic and dark-theme previews.
- Added ViewModel race, retry, clear, and stale-result regression tests.
- Added Compose tests for clear, retry, and result navigation.
- Added a phase-specific source verifier and wired it into Android CI.

## Backward compatibility

- No Room entity, DAO, database version, migration, or prebuilt database changes.
- No FTS schema or query ordering changes.
- No repository contract changes.
- No navigation route or argument changes.
- No DataStore key changes.
- Existing bookmarks, reader position, audio, tafsir, and settings remain compatible.
- No new dependency or framework was introduced.

## Search integrity

The search path remains fully offline:

```text
Visible query
  → bounded Arabic normalization
  → safe FTS4 prefix query
  → Room DAO MATCH query
  → canonical ayah-order results
  → reader navigation using stable ayah ID
```

The displayed Quran text is never normalized or modified. Normalization is applied only to the separate search field and query string.

## Performance decisions

- Search remains backed by the prebuilt FTS4 table.
- A bounded 300 ms debounce prevents a database query on every keystroke.
- Prior jobs are canceled and late responses are rejected by generation ID.
- Result count remains capped at 100.
- Results remain in a lazy list with stable ayah IDs and homogeneous content type.
- Decorative elements are drawn with lightweight Compose Canvas operations.
- No bitmap mockup or new runtime dependency was added.

## Accessibility decisions

- Clear and back controls expose localized descriptions or click labels.
- Retry has a minimum 48 dp touch target.
- Result cards expose their readable text and a dedicated open-ayah description.
- Search uses an appropriate IME action.
- Arabic and English resources remain in parity.
- Test tags provide deterministic interaction tests without depending on localized text.

## Verification

The following source-level gates passed:

```text
BUILD FOUNDATION VERIFICATION: PASSED
FINAL VERIFICATION: PASSED
HARDENING VERIFICATION: PASSED
PHASE 2 PREMIUM HOME VERIFICATION: PASSED
PHASE 3 PREMIUM READER VERIFICATION: PASSED
PHASE 4 PREMIUM SEARCH VERIFICATION: PASSED
```

The SQLite host benchmark remained sub-millisecond in the available environment. The Quran database was not modified.

## Remaining external validation

The current environment does not include the Android SDK or `gradle-wrapper.jar`, so the following must still run in Android CI or Android Studio:

- `:app:testDebugUnitTest`
- `:app:assembleDebug`
- `:app:lintDebug`
- `:app:connectedDebugAndroidTest`
- Compose text-field behavior with multiple Arabic keyboards
- TalkBack traversal and result announcements
- 200% font-scale and small-screen validation
- Physical-device search latency and frame timing

## Production readiness

The premium search phase is source-level complete and backward compatible. It is ready for Android compilation and device validation, but it must not be called release-verified until Android build, Lint, instrumentation, accessibility, and device tests pass.
