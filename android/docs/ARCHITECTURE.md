# Noor architecture

## Architectural style

Noor uses a moderate multi-module Clean Architecture with MVVM and unidirectional UI state. It
separates framework-independent business contracts from Android persistence, media, and Compose
presentation while avoiding premature one-module-per-screen fragmentation.

## Module ownership

- `:app`: application entry point, root navigation, Compose screens, ViewModels, and release manifest.
- `:core:common`: Arabic search normalization and framework-independent shared primitives.
- `:core:designsystem`: light/dark Material 3 schemes, optional dynamic color, typography, shapes,
  spacing, accessibility labels, previews, and shared components.
- `:core:navigation`: route contracts and navigation-controller creation.
- `:core:media`: Media3 session service, application-scoped playback implementation, the testable
  `QuranAudioPlayer` contract, URL construction, and playback state. Presentation never sees
  ExoPlayer or `MediaController`.
- `:domain`: immutable Quran, bookmark, tafsir, reciter, and settings models plus repository contracts.
  This module has no Android dependency.
- `:data`: Room entities/DAOs/database/migrations, prepackaged corpus, DataStore settings, tafsir
  network boundary/cache, WorkManager maintenance, mappers, and repository implementations.
- `:baseline-profile`: test-only `com.android.test` module. It targets a minified Benchmark variant
  for measurements and a separate non-minified Release-like variant for profile generation. No
  production module depends on it.

## Dependency direction

```text
                         ┌──────────────► core:designsystem
                         │
app ─────────────────────┼──────────────► core:navigation
 │                       │
 ├───────────────────────┼──────────────► core:media ─────► domain
 │                       │
 ├───────────────────────┴──────────────► data ───────────► domain ───► core:common
 └──────────────────────────────────────► domain

baseline-profile ──test target only──► app:benchmark
```

Rules enforced by `tools/verify_final.py`:

1. `domain` and `core:common` remain Android-free.
2. `data` implements contracts owned by `domain`; `domain` never references `data`.
3. Compose/ViewModels do not import Room entities, DAOs, DataStore, or concrete repositories.
4. Media3 is isolated in `core:media`; presentation depends on `QuranAudioPlayer`, and data and
   presentation do not construct ExoPlayer or MediaController.
5. Room is the local source of truth for Quran, bookmarks, and cached tafsir.
6. DataStore is the source of truth for small user preferences and reading position.
7. Network sources are cache-backed and accessed through repository contracts.
8. Background maintenance is unique, constrained, and idempotent.
9. No destructive Room migration fallback is allowed.
10. The Benchmark/Profile module is test-only and does not enter the production dependency graph.

## Data flow

```text
Compose screen
    │ events / lifecycle-aware StateFlow
    ▼
ViewModel
    │ domain repository interface
    ▼
Repository implementation
    ├── Room DAO / prepackaged database
    ├── Preferences DataStore
    ├── HTTPS tafsir source + Room cache
    └── QuranAudioPlayer boundary → Media3 implementation (audio path)
```

## State and concurrency

- ViewModels expose immutable screen state through `StateFlow`.
- Compose collects state with lifecycle awareness.
- Database reads use Room `Flow`; one-shot operations are `suspend`.
- Search cancels prior requests, waits 300 ms before querying, and distinguishes input from a completed empty search.
- Broad coroutine error boundaries explicitly rethrow `CancellationException`; persistent UI flows expose loading/error/retry state.
- Full-corpus replacement uses one Room transaction.
- WorkManager performs non-urgent weekly cache cleanup with a battery-not-low constraint.
- Primary screens use a shared 840 dp maximum content width for readable large-screen layouts.

## Modularity decision

Presentation features are intentionally retained inside `:app`. The application currently has a
single product surface, one navigation graph, no parallel feature teams, and no reusable feature
SDK. Extracting seven small screen modules would increase Gradle configuration, resource wiring,
and navigation indirection without improving domain isolation. The boundary can be revisited when a
feature is independently owned, reused, or contributes materially to build times.
