# Phase 5 — Premium Bookmarks Review

Date: 2026-07-18

## Scope

This phase reviews and implements only the Bookmarks feature. The locked Kotlin, Compose, Material 3,
MVVM, Clean Architecture, Repository, Hilt, Room, DataStore, Media3, Navigation Compose, single-activity,
offline-first, prebuilt SQLite, and FTS4 architecture remains unchanged.

## Existing implementation review

The previous screen used generic Material cards and did not follow Noor's approved emerald, ivory, and
muted-gold visual system. Loading, empty, error, and content states existed, but destructive bookmark
operations had two production defects:

1. `BookmarksViewModel.remove()` launched the repository mutation without handling non-cancellation
   exceptions. A Room or storage failure could escape the `viewModelScope` child and reach the main
   coroutine exception handler instead of producing recoverable UI feedback.
2. Repeated taps could dispatch duplicate deletion requests for the same ayah while the first request
   was still running.

The screen also gave no visible progress during deletion and no message when deletion failed.

## Minimum safe production fix

- Kept `BookmarkRepository`, `BookmarkDao`, Room entities, schema, navigation routes, and persisted data
  unchanged.
- Added per-ayah pending-removal state to prevent duplicate requests.
- Preserved `CancellationException` semantics and caught only operational failures.
- Added a buffered one-shot `BookmarksEvent.RemovalFailed` event and a Material 3 snackbar in the UI.
- Kept Room as the source of truth; bookmarks are removed from the list only after the observed Room
  query emits the updated data.
- Added loading feedback and disabled the destructive control while removal is pending.

## Premium UI implementation

The Bookmarks screen is now native Compose using the approved visual direction:

- deep emerald patterned background;
- premium top app bar with gold accents;
- warm ivory cards and restrained gold borders;
- Quran ayah medallions;
- Uthmani text from the stored database without transformation;
- juz and page metadata;
- explicit open and remove actions;
- polished loading, error, and empty states;
- newest-first count header;
- Arabic RTL and dark-theme previews.

No screenshot or raster mockup is used in the application UI.

## Files created

- `app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksComponents.kt`
- `app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksEvent.kt`
- `app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksPreview.kt`
- `app/src/androidTest/kotlin/com/noor/app/ui/bookmarks/BookmarksPremiumUiTest.kt`
- `tools/verify_phase5_premium_bookmarks.py`
- `docs/PHASE_5_PREMIUM_BOOKMARKS_REVIEW_2026-07-18.md`

## Files modified

- `.github/workflows/android-ci.yml`
- `README.md`
- `app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksRoute.kt`
- `app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksUiState.kt`
- `app/src/main/kotlin/com/noor/app/ui/bookmarks/BookmarksViewModel.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-ar/strings.xml`
- `app/src/test/kotlin/com/noor/app/ui/bookmarks/BookmarksViewModelTest.kt`
- `core/designsystem/src/main/kotlin/com/noor/core/designsystem/component/NoorLineIcon.kt`

## Regression coverage

ViewModel tests cover:

- observation failure and retry recovery;
- successful removal;
- duplicate-removal suppression;
- failure-event delivery and pending-state cleanup;
- invalid ayah ID rejection.

Compose tests cover:

- opening the selected bookmark;
- removing the correct bookmark without opening it;
- disabling removal while the operation is pending;
- the empty-state browse action.

The phase verifier also locks:

- stable LazyColumn keys and content types;
- direct Uthmani text rendering;
- newest-first Room ordering;
- Room/repository persistence contracts;
- Arabic/English resource parity;
- absence of placeholders.

## Verification results

- Build foundation verification: passed.
- Final repository verification: passed.
- Hardening verification: passed.
- Premium Home verification: passed.
- Premium Reader verification: passed.
- Premium Search verification: passed.
- Premium Bookmarks verification: passed.
- Prebuilt Quran database SHA-256 remained unchanged:
  `a77c6155204cbe13133ab09a253d030622d4cc2ac0c5aa2514cda007d24bfda1`.
- Deleted files: 0.

## Remaining external gate

A real Android Gradle build was not available in this execution environment. The verified Wrapper JAR
is absent and the environment cannot resolve `services.gradle.org`; Android SDK compilation, Compose
compiler validation, KSP/Hilt/Room processing, Android Lint, emulator tests, and device accessibility
checks must still run in the configured Android CI or Android Studio workstation.

## Production readiness

The Bookmarks feature is source-level production ready and backward compatible. Final release readiness
still depends on the repository-wide external Android build, lint, instrumentation, device, signing, and
Play Console gates documented in `docs/RELEASE.md`.
