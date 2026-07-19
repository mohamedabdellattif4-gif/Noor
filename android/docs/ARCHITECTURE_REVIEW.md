# Architecture review

## Review scope

The review covered module ownership, dependency direction, UI state flow, persistence boundaries,
background work, media lifetime, corpus replacement, search indexing, migration behavior and
adaptive presentation.

## Findings and remediation

| Finding | Severity | Resolution |
|---|---:|---|
| A single app module would mix persistence, media and presentation | High | Retain moderate boundaries for domain, data, design, navigation, common and media |
| Media3 details could leak into feature/ViewModel code | Medium | Isolate player/session/service/controller in `:core:media` and inject the `QuranAudioPlayer` contract into presentation |
| Search used an index incompatible with prefix matching | High | Use normalized external-content FTS4 and bounded prefix queries |
| Quran source IDs were not globally stable | High | Generate and validate canonical global IDs 1..6,236 |
| User settings and corpus data could be mixed | Medium | Room owns structured/cache data; DataStore owns compact preferences and reading position |
| Full-corpus writes could leave partial data | High | Validate aggregate invariants and write inside one Room transaction |
| Corpus refresh could delete bookmarks/tafsir through cascades | High | Upsert stable canonical IDs in place and retain a regression test |
| Custom FTS migration triggers could duplicate Room-managed triggers | High | Let Room own external-content synchronization triggers |
| ViewModels lacked explicit error/retry state for persistent flows | Medium | Model Home, Reader and Bookmarks as immutable state with cancellable observation jobs |
| Feature-per-module extraction would add cost without current reuse | Low | Keep compact presentation in `:app`; document extraction criteria |
| Wide tablets could harm long-form reading | Medium | Centralize a maximum content width in the design system |
| Performance tooling could contaminate Release, capture obfuscated rules, or require legacy AGP DSL | Medium | Use a test-only module, separate minified measurement and non-minified generation variants, variant-only profileable overlays, and no incompatible consumer plugin |

## Dependency verdict

- `:domain` and `:core:common` remain Android-free.
- UI depends on repository contracts, not Room, DataStore or network implementations.
- `:data` owns Room, DataStore, WorkManager and HTTPS implementation.
- `:core:media` owns Media3 and depends only on domain models; presentation consumes its
  framework-neutral `QuranAudioPlayer` surface rather than the concrete controller.
- Room is the structured local source of truth; DataStore owns small preferences.
- Background maintenance is unique, constrained and idempotent.
- No destructive Room fallback exists.
- `:baseline-profile` targets only `app:benchmark`; no production module depends on it.

## Re-review result

Four architecture passes found no repository-fixable high or medium defect. A real Gradle build is
still required to validate Hilt/KSP/Room generated code, manifest merging, Compose compilation and
the compiler-generated Room schema JSON.
