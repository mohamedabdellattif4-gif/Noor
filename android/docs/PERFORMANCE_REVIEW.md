# Performance review

## Findings and remediation

| Finding | Severity | Resolution |
|---|---:|---|
| SQL text scanning could touch all 6,236 ayahs | High | Use normalized external-content FTS4 prefix search |
| Search could run on every keystroke | Medium | Debounce 300 ms and cancel the previous job |
| Search MATCH expression could grow without bound | Medium | Cap characters, tokens, token length and results |
| Text-size slider generated repeated DataStore writes | Medium | Persist only when dragging finishes while preview stays immediate |
| Continue-reading could write during every scroll frame | Medium | Persist the settled visible ayah after a 750 ms `collectLatest` debounce |
| Quran startup depended on parsing/importing a corpus | High | Ship a validated prepopulated Room database |
| Reader could compose all ayahs eagerly | High | Use `LazyColumn` with stable ayah IDs |
| Tafsir was repeatedly downloaded | Medium | Cache by ayah in Room and clean stale entries through constrained WorkManager |
| Wide layouts increased reading-line length | Medium | Cap primary content width at 840 dp |
| Release carried avoidable code/resources | Medium | Enable R8 optimization and resource shrinking |
| Playback lifetime could retain ExoPlayer | High | Release player and media session in service `onDestroy` |

## Measurements in the generation environment

`tools/verify_final.py` repeats SQLite FTS searches and requires host p95 below 50 ms. It also keeps
the prepackaged database below 10 MiB, verifies exactly 6,236 indexed rows and regenerates/compares
the complete logical corpus. This is a regression gate, not an Android UI/device benchmark.

The final database is approximately 3.1 MiB. The final measured p95 is recorded in
`FINAL_VERIFICATION.md`; minor variation between hosts is expected.

## Baseline Profile and Macrobenchmark strategy

The repository now contains a dedicated `:baseline-profile` `com.android.test` module with:

- a Baseline Profile generator covering startup, Al-Fatiha navigation, and reader scrolling;
- cold-start comparison with no compilation and with the packaged Baseline Profile;
- reader `FrameTimingMetric` measurement;
- shared critical-user journeys that fail when expected UI is unavailable;
- a release-equivalent minified Benchmark variant for measurement;
- a separate non-debuggable, non-minified profile-generation variant so source rules are not based on
  R8-obfuscated names;
- shell profiling only in those two test variants, leaving production Release non-profileable;
- `ReportDrawnWhen` tied to Home data readiness for time-to-full-display;
- a managed-device generation workflow and a validated profile-install script.

The checked-in profile is still a starter until the generator runs with the Android toolchain. Managed
emulators are useful for deterministic rule generation; final startup/frame claims must come from
representative physical devices and retained JSON/Perfetto traces.

## Re-review result

Four source-level performance passes found no obvious algorithmic or allocation hotspot. The fourth
pass added executable benchmark/profile infrastructure and repeatable release gates. Startup, frame
timing, memory, battery, audio buffering, network behavior, 16 KB device behavior and Android Vitals
still require built-artifact/device measurements.
