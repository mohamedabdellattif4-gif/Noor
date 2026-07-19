# Phase 6 — Premium Audio Review

Date: 2026-07-19

## Scope

This phase reviews and implements only Noor's listening and background-audio experience. The locked
Kotlin, Compose, Material 3, MVVM, Clean Architecture, Repository, Hilt, Room, DataStore, Media3,
MediaSessionService, Navigation Compose, single-activity, offline-first, prebuilt SQLite, and FTS4
architecture remains unchanged.

## Existing implementation review

The prior implementation could play an ayah or surah from the Reader and continue through a
`MediaSessionService`, but it did not provide a dedicated listening destination. The Home listening
shortcut opened the Reader, and presentation code received only a coarse playing/buffering/paused state.
That created the following production issues:

1. The app had no first-class listening workflow for choosing a surah, selecting a reciter, seeking,
   stopping, or moving through the queue without entering the Reader.
2. The coarse playback contract did not expose the current media identity, queue position, duration,
   seek position, or previous/next availability.
3. The Reader could resume an unrelated paused or buffering ayah because paused/buffering states did
   not retain media identity in the UI decision path.
4. Screen-off playback did not explicitly configure ExoPlayer's local wake mode or declare the
   corresponding wake-lock permission, creating a risk of interrupted remote audio on some devices.
5. Continuous progress polling would have run even with no playback UI observing it, wasting main-thread
   work and battery during background playback.
6. The first retry implementation placed `catch` outside the retry-generation flow. Once an upstream
   observation failed, the recovered flow completed and later retry requests could not restart it.
7. A buffering queue belonging to another selected surah could incorrectly show a spinner on the
   selected-surah play button.

## Root causes

- Playback identity and timeline data were held only inside Media3 and were not represented by the
  framework-neutral `QuranAudioPlayer` boundary.
- The Home navigation model had no stable Audio route.
- The Reader's pause/resume decision relied on state type alone instead of state plus current media ID.
- ExoPlayer had audio focus and noisy-device handling but no explicit wake strategy.
- Position polling was tied to player state rather than active UI collectors.
- Retry recovery was attached to the outer flow, so recovery emitted once and terminated the stream.

## Minimum safe production fix

- Added an additive `QuranPlaybackInfo` contract with sanitized media ID, title, artist, queue index,
  queue size, position, duration, progress, and transport capabilities.
- Extended `QuranAudioPlayer` with backward-compatible default playback-info and transport methods so
  existing test doubles and alternative implementations remain source compatible.
- Kept all Media3 implementation types inside `:core:media`.
- Added a stable `audio` Navigation Compose destination and routed Home listening actions to it.
- Updated Reader playback resolution to resume only when the visible ayah matches the current media ID.
- Added `C.WAKE_MODE_LOCAL`, the `WAKE_LOCK` permission, and retained automatic audio focus plus
  headphone-disconnect protection.
- Polls timeline position only while playback is active and `QuranPlaybackInfo` has collectors.
- Moved operational recovery inside the retry generation, preserving `CancellationException` and making
  retry restart repository/settings observation.
- Scoped buffering feedback to the currently selected queue.
- Kept Room schemas, migrations, DataStore keys, Quran data, repository contracts, and existing routes
  backward compatible.

## Premium UI implementation

The dedicated Audio screen is native Compose and follows Noor's approved visual direction:

- deep emerald patterned background;
- warm ivory and muted-gold surfaces;
- premium now-playing card and restrained waveform visualization;
- current surah, reciter, ayah, queue index, elapsed time, and duration;
- seek slider when duration is available;
- previous, play/pause, next, and stop actions;
- reciter selector backed by DataStore;
- all 114 surahs in a stable-key lazy list;
- selected-surah feedback and an open-in-reader action;
- loading, error, retry, and reciter-update feedback;
- Arabic RTL and dark-theme previews;
- minimum touch targets, semantic selection state, localized descriptions, and stable test tags.

No screenshot or raster mockup is used in the application UI.

## Files created

- `app/src/main/kotlin/com/noor/app/ui/audio/AudioComponents.kt`
- `app/src/main/kotlin/com/noor/app/ui/audio/AudioEvent.kt`
- `app/src/main/kotlin/com/noor/app/ui/audio/AudioPreview.kt`
- `app/src/main/kotlin/com/noor/app/ui/audio/AudioRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/audio/AudioUiState.kt`
- `app/src/main/kotlin/com/noor/app/ui/audio/AudioViewModel.kt`
- `app/src/test/kotlin/com/noor/app/ui/audio/AudioViewModelTest.kt`
- `app/src/androidTest/kotlin/com/noor/app/ui/audio/AudioPremiumUiTest.kt`
- `core/media/src/main/kotlin/com/noor/core/media/QuranPlaybackInfo.kt`
- `core/media/src/test/kotlin/com/noor/core/media/QuranPlaybackInfoTest.kt`
- `tools/verify_phase6_premium_audio.py`
- `docs/PHASE_6_PREMIUM_AUDIO_REVIEW_2026-07-19.md`

## Files modified

- `.github/workflows/android-ci.yml`
- `README.md`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/kotlin/com/noor/app/navigation/NoorNavHost.kt`
- `app/src/main/kotlin/com/noor/app/ui/home/HomePreview.kt`
- `app/src/main/kotlin/com/noor/app/ui/home/HomeRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderComponents.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderPlaybackAction.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderUiState.kt`
- `app/src/main/kotlin/com/noor/app/ui/reader/ReaderViewModel.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ar/strings.xml`
- `app/src/test/kotlin/com/noor/app/ui/reader/ReaderPlaybackActionTest.kt`
- `core/designsystem/src/main/kotlin/com/noor/core/designsystem/component/NoorLineIcon.kt`
- `core/media/build.gradle.kts`
- `core/media/src/main/kotlin/com/noor/core/media/QuranAudioController.kt`
- `core/media/src/main/kotlin/com/noor/core/media/QuranAudioPlayer.kt`
- `core/media/src/main/kotlin/com/noor/core/media/QuranPlaybackService.kt`
- `core/navigation/src/main/kotlin/com/noor/core/navigation/NoorRoute.kt`
- `tools/verify_final.py`
- `tools/verify_phase3_premium_reader.py`

## Regression coverage

ViewModel tests cover:

- initial selection from the persisted last-read surah;
- queue creation with the persisted reciter;
- resume of the selected queue versus replacement of a different queue;
- previous, next, seek, and stop delegation;
- reciter-persistence failure feedback and loading-state cleanup;
- retry after a real repository observation failure.

Media contract tests cover:

- strict Noor ayah-media ID parsing;
- unavailable, proportional, and bounded progress values.

Reader regression tests cover identity-safe resume behavior for paused and buffering playback.

Compose tests cover:

- enabled and disabled transport states;
- play and next actions;
- accessible reciter selection;
- exact surah selection;
- transition from the player to the Reader.

The phase verifier also locks:

- the dedicated route and Home navigation;
- MediaSessionService declarations and foreground-service type;
- local wake mode and wake-lock permission;
- headphone-disconnect and audio-focus handling;
- subscriber-aware position polling;
- stable LazyColumn keys and content types;
- Arabic/English resource parity;
- absence of placeholders;
- pure Kotlin playback-info compilation and behavior.

## Verification results

- Build foundation verification: passed.
- Final repository verification: passed.
- Hardening verification: passed.
- Premium Home verification: passed.
- Premium Reader verification: passed.
- Premium Search verification: passed.
- Premium Bookmarks verification: passed.
- Premium Audio verification: passed.
- Audio ViewModel framework-stub compilation with warnings as errors: passed.
- Prebuilt Quran database SHA-256 remained unchanged:
  `a77c6155204cbe13133ab09a253d030622d4cc2ac0c5aa2514cda007d24bfda1`.
- Deleted files: 0.

## Remaining external gate

A real Android Gradle build was not available in this execution environment. The verified Wrapper JAR
is absent, Android SDK compilation is unavailable, and the environment cannot currently complete the
network bootstrap. Compose compiler validation, Media3/Android API compilation, KSP/Hilt/Room
processing, Android Lint, emulator instrumentation, notification/media-button behavior, Bluetooth,
headphone-disconnect, Doze, lock-screen controls, and physical-device battery tests must still run in
the configured Android CI or Android Studio workstation.

## Production readiness

The Audio feature is source-level production ready and backward compatible. Final release readiness
still depends on the repository-wide external Android build, lint, instrumentation, device, signing,
provider-rights, and Play Console gates documented in `docs/RELEASE.md`.
