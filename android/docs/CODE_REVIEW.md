# Code review

## Findings and remediation

| Finding | Severity | Resolution |
|---|---:|---|
| Duplicate/stale design-system declarations | High | Remove duplicates and retain one public component surface |
| Fixed strings in Kotlin weakened localization | Medium | Move all user-facing text to matching English/Arabic resources |
| Broad coroutine catches could swallow cancellation | High | Rethrow `CancellationException` at search, tafsir, observation and worker boundaries |
| Loading indicator forced full-screen layout | Medium | Make it caller-sized and retain a separate full-screen wrapper |
| Dense action rows could overflow in Arabic or on narrow screens | Medium | Use full-width vertical actions for home and reader controls |
| Text-size slider wrote on every pointer movement | Medium | Preview locally and persist once on drag completion |
| Audio failure had no visible state | Medium | Add idle, buffering, playing, paused and error states |
| Launcher icon coverage was incomplete | Medium | Add legacy, adaptive, round and monochrome resources |
| FTS reserved words could be interpreted as operators | High | Quote every normalized token before appending the prefix wildcard |
| Search input could produce unbounded query complexity | Medium | Cap query length, token count, token length and result count |
| Search UI could show “no results” before a normalized search completed | Medium | Track `hasSearched` separately from raw input length |
| Continue-reading used a list index that included a non-ayah header | High | Track the first visible item whose stable key is an ayah ID and apply the header offset only when initially scrolling |
| Home/reader/bookmarks failures could look like indefinite loading or empty content | Medium | Add explicit Loading/Content/Empty/Error states and retry actions |
| Repeated tafsir retries could leave concurrent requests running | Medium | Cancel the previous load job before retrying |
| Large-screen text and lists could become excessively wide | Medium | Add a shared centered content container capped at 840 dp |
| Media notification used enum identifiers | Low | Localize ayah and reciter metadata in English and Arabic |
| Tafsir response cap measured characters instead of transport bytes | High | Read through a bounded byte stream with a strict 1 MiB cap |
| Source formatting degraded after automated merges | Medium | Rewrite affected Compose routes with consistent Kotlin formatting and imports |
| Benchmark journeys could silently skip reader navigation | Medium | Share one strict critical-user-journey helper and fail when Al-Fatiha never becomes visible |
| Release tooling had no executable regression suite | Medium | Add `verify_hardening.py` for profile installation, signing, artifact, preflight, and workflow checks |
| Reader presentation depended on the concrete Media3 controller | Medium | Introduce `QuranAudioPlayer`, bind it through Hilt, and test ReaderViewModel with a fake player |
| Home, Reader, and Bookmarks retry/state regressions lacked unit coverage | Medium | Add deterministic coroutine tests for loading, recovery, playback start position, last-read persistence, and bookmark changes |
| Keystore path validation could be bypassed by path aliases or symlinks | High | Compare canonical paths in Gradle and resolve paths in preflight; add a symlink-regression test |

## Automated gates

- Kotlin/Kotlin DSL syntax parsing.
- XML and Version Catalog parsing.
- No wildcard imports, production `runBlocking`, `GlobalScope`, direct console output, unresolved
  TODO/FIXME, hardcoded Compose text, cleartext Kotlin URLs or obvious hard-coded secrets.
- English/Arabic resource-key parity in app, design-system and media modules.
- Complete logical database regeneration comparison.
- Reader stable-key position checks, search completion-state checks, tafsir cancellation checks,
  adaptive content-container checks, audio-boundary checks, and Home/Reader/Bookmarks state and
  retry tests.
- Android-free Kotlin compilation for JVM 17 with warnings treated as errors when `kotlinc` is present.
- Android Lint is configured to abort on errors and treat warnings as errors when the Android toolchain runs.

## Re-review result

Four source-review passes found no remaining source-level high or medium issue. Compiler-generated
code, Android Lint output, Gradle unit tests, instrumentation and device UI results remain external
because AGP cannot run in the current environment.
