# Noor Phase 3 — Premium Quran Reader Review

Date: 2026-07-18

## Scope

This phase implements only the premium Quran reader visual direction approved after the premium Home screen. It does not redesign Search, Bookmarks, Tafsir, Settings, or the underlying architecture.

## Existing implementation review

### 1. Fragmented reading surface

The previous reader rendered every ayah inside a generic card with three full-width text buttons. This was functionally correct but visually fragmented the Quran into unrelated application cards, reduced the amount of Quran text visible on screen, and multiplied composables and interaction controls for long surahs.

**Root cause:** the initial functional reader reused general-purpose card and button components instead of a dedicated long-form Quran reading surface.

### 2. Actions were detached from the visible ayah

Audio controls were placed in a separate card above the list. Bookmark, tafsir, and verse audio controls were repeated inside every ayah card. The top app bar displayed only the surah name and did not reflect the currently visible page or juz.

**Root cause:** the reader had no presentation-level concept of the first visible ayah, although the existing `LazyListState` already exposed the information needed to derive it safely.

### 3. Font size could not be adjusted in context

The persisted Quran text scale existed in DataStore and Settings, but the reader did not provide the approved bottom action for changing it while reading.

**Root cause:** the setting was exposed only through `SettingsViewModel`; `ReaderViewModel` consumed the value but did not expose a bounded persistence action.

### 4. Buffering audio could not be paused reliably

`QuranAudioController.togglePause()` used `Player.isPlaying`. Media3 reports `isPlaying == false` while buffering even if playback is scheduled through `playWhenReady`. Tapping pause during buffering therefore called `play()` again instead of pausing.

**Root cause:** `isPlaying` describes active playback, not playback intent. `playWhenReady` is the appropriate state for a pause/resume toggle across READY and BUFFERING.

### 5. Basmala presentation needed explicit corpus-aware rules

Noor's corpus stores the basmala as ayah 1 of Al-Fatihah. At-Tawbah has no opening basmala. A visual header applied indiscriminately would either duplicate Quran text or add text where it does not belong.

**Root cause:** the visual reference did not encode the Quran corpus rules required for production data integrity.

## Production fixes applied

- Replaced the card-per-ayah presentation with one warm ivory reading surface.
- Preserved `LazyColumn`, stable ayah IDs, target-ayah scrolling, and the debounced last-read observer.
- Added restrained Islamic corner ornaments and decorative dividers drawn locally in Compose.
- Added decorative ayah-number medallions without modifying Quran text.
- Rendered `Ayah.textUthmani` directly from the verified prebuilt database.
- Added a dynamic emerald top bar showing the current surah, juz, and page based on the first visible ayah.
- Added a fixed premium bottom action bar for bookmark, tafsir, listening, and text size.
- Kept full-surah playback and stop controls available.
- Added an in-reader text-size dialog with bounded values persisted through `SettingsRepository`.
- Changed Media3 pause/resume logic to use `playWhenReady`.
- Added explicit and tested basmala rules for Al-Fatihah, At-Tawbah, and all other surahs.
- Added Arabic and English previews.
- Added unit, ViewModel, pure-behavior, and Compose interaction tests.
- Added the phase verifier to Android CI.

## Backward compatibility

- No Room entity, DAO, database version, migration, or prebuilt database changes.
- No navigation route or argument changes.
- No DataStore key changes.
- Existing bookmarks, last-read positions, text scale, reciter choice, tafsir navigation, and audio service remain compatible.
- No architecture or framework replacement.

## Performance decisions

- Long surahs remain lazy and use stable ayah IDs.
- The current ayah is derived from `LazyListState` rather than copied into persistent UI state.
- Only the visible playback row receives a subtle highlight.
- Ornamentation uses lightweight Canvas drawing and no bitmap background.
- No new dependency was introduced.
- Repeated per-ayah action buttons were removed from the lazy list.

## Accessibility decisions

- Bottom actions have at least 48 dp interactive width and 64 dp height.
- Actions expose button roles and localized labels.
- Bookmark selection state is exposed through semantics.
- Text size is user adjustable and persisted.
- Quran content is presented in RTL independently of the surrounding UI locale.
- Test tags support deterministic Compose interaction tests without relying on localized labels.

## Verification

The following source-level gates passed:

```text
BUILD FOUNDATION VERIFICATION: PASSED
FINAL VERIFICATION: PASSED
HARDENING VERIFICATION: PASSED
PHASE 2 PREMIUM HOME VERIFICATION: PASSED
PHASE 3 PREMIUM READER VERIFICATION: PASSED
```

The phase verifier also compiles and executes a pure Kotlin harness for playback decisions and basmala presentation rules.

## Remaining external validation

The current environment does not include the Android SDK or `gradle-wrapper.jar`, so the following must still run in Android CI or Android Studio:

- `:app:testDebugUnitTest`
- `:app:assembleDebug`
- `:app:lintDebug`
- `:app:assembleBenchmark`
- `:app:connectedDebugAndroidTest`
- TalkBack traversal on a physical device
- Arabic shaping comparison across supported API levels and OEM fonts
- 200% font-scale and small-screen validation
- Macrobenchmark reader-scroll validation

## Production readiness

The premium reader is source-level complete and backward compatible. It is ready for Android compilation and device validation, but it must not be called release-verified until the Android build, Lint, Compose instrumentation tests, accessibility checks, and physical-device rendering checks pass.
