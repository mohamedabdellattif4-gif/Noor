# Noor final project report

## Delivery status

Noor has been implemented through all thirteen planned stages and reviewed through three engineering
passes plus a fourth production-hardening pass. The repository is a source-level
**Release Candidate**. It is not represented as a signed or
publishable Play artifact until the external build, device, signing, provider-rights and policy gates
are completed.

## Delivered product

- Complete offline Quran corpus: 114 surahs and 6,236 ayahs with page/juz/rub metadata.
- Home screen, surah list and resilient continue-reading state.
- Lazy Quran reader with scalable typography, stable visible-ayah tracking and adaptive width.
- Arabic-normalized Room FTS4 search with debounce, cancellation and bounded safe prefix queries.
- Persistent bookmarks with Loading/Content/Empty/Error states.
- Media3 background ayah/surah recitation, localized metadata and selectable reciters.
- On-demand Arabic tafsir with hardened HTTPS transport, Room cache and WorkManager cleanup.
- DataStore settings for theme, dynamic color, Quran text scale, reciter and last read position.
- Material 3 design system, dark mode, Arabic/English resources, RTL and large-screen containment.
- Tests for normalization, domain invariants, audio URL safety, Home/Reader/Bookmarks/Search
  ViewModels, database/FTS, corpus refresh/rollback, and Compose theme smoke rendering.
- Privacy, attribution, architecture, database, release and five review reports.
- Production-hardening infrastructure: separate Macrobenchmark/profile-generation variants, TTFD reporting,
  release preflight/signature checks, CI, CodeQL, dependency graph/review, and per-app locales.

## Architecture

```text
app -> domain contracts + core modules + data implementations
core:media -> QuranAudioPlayer contract + domain models + Media3 implementation
core:designsystem -> Compose/Material 3
core:navigation -> route contracts
data -> domain + core:common + Room/DataStore/WorkManager/HTTPS
domain/core:common -> Android-free Kotlin
```

Room is the structured local source of truth. DataStore owns compact preferences. Network content is
optional and cached; reading, navigation, search and bookmarks work from the packaged database.

## Review outcome

### Architecture review

Resolved persistence/media leakage, concrete Media3 coupling, unstable IDs, partial corpus writes,
FTS trigger ownership, missing error-state boundaries and large-screen line length.

### Code review

Resolved duplicate APIs, localization gaps, cancellation swallowing, narrow-screen overflow,
incorrect visible-ayah persistence, premature empty states, concurrent tafsir retries, formatting
regressions and unbounded transport/query behavior.

### Security review

Enforced HTTPS-only traffic, fixed hosts, timeout/redirect/content-type/byte-size controls, minimal
permissions, disabled backup, safe FTS construction, explicit migrations and secret scanning. The
Media3 service is exported only as required for system media controllers and exposes no custom
command or media-library surface.

### Performance review

Added FTS4, a compact prebuilt database, lazy reader lists, stable keys, debounced/cancelled work,
Room tafsir cache, constrained cleanup, adaptive content width, R8/resource shrinking, executable
Baseline Profile generation, cold-start and frame benchmarks, and full-display reporting.

### Google Play readiness review

Prepared target/compile SDK settings, strict Lint configuration, release shrinking, icon variants,
foreground media service, per-app locales, privacy/source notices, validated version/signing inputs,
artifact signature inspection, CI/security workflows and a complete external release checklist.

## Verification outcome

`tools/verify_final.py` and `tools/verify_hardening.py` pass after the fourth
production-hardening review. They verify architecture, resources, security
policy, Quran data integrity/fidelity, FTS correctness/performance, generator reproducibility,
release assets, Kotlin syntax and Android-free Kotlin compilation.

## Residual items that cannot be completed without external requirements

1. Run AGP/Gradle build, Lint, KSP/Hilt/Room compilation and resolved dependency scans with JDK 17 and
   Android SDK Platform 37.
2. Generate/commit Room schema JSON and run migration/instrumentation tests.
3. Test min/recent API devices, RTL, dark mode, offline use, accessibility, configuration changes,
   audio background/process death, startup, frame timing and battery.
4. Inspect the built artifact for transitive native libraries and verify 16 KB page-size compatibility.
5. Run the implemented Baseline Profile generator and Macrobenchmarks on physical devices and retain results.
6. Choose a unique production application ID if `com.noor.app` is unavailable.
7. Create and secure the Play upload/signing key and build a signed AAB.
8. Insert publisher legal name/support contact, host the privacy policy and create store assets/forms.
9. Obtain current written distribution/streaming confirmation for EveryAyah and QuranEnc.
10. Upload to Play internal testing, inspect App Bundle Explorer/pre-launch reports and stage rollout.

No source-level high or medium finding remains after the fourth review pass. The residual items require
publisher identity, credentials, provider confirmation, an Android toolchain/device lab or Play
Console access and cannot be honestly fabricated inside this repository.

## 2026-07-17 stabilized-hardening merge

The Production Hardened source was retained as the release base. The Room 2 -> 3 FTS migration now
explicitly recreates its synchronization triggers, and the final verifier exercises insert, update,
stale-term removal, and delete behavior in temporary SQLite. Gradle launchers also give an actionable
message when the Wrapper JAR has not yet been bootstrapped. See
`docs/STABILIZED_HARDENED_MERGE_2026-07-17.md`.
