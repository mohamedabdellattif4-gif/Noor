# Contributing to Noor

## Required environment

- JDK 17
- Android SDK Platform 37
- Python 3.11 or newer for repository verification

## Before opening a change

```bash
python3 tools/verify_final.py
./bootstrap-gradle-wrapper.sh
./gradlew clean \
  :core:common:test \
  :core:media:test \
  :domain:test \
  :app:testDebugUnitTest \
  :data:assembleAndroidTest \
  :app:assembleDebug \
  :app:assembleBenchmark \
  :app:assembleNonMinifiedRelease \
  :baseline-profile:assembleBenchmark \
  :baseline-profile:assembleNonMinifiedRelease \
  :app:lintDebug \
  --warning-mode=all
```

Changes to the Room schema must include an explicit migration, a migration test, and the generated
schema JSON. Changes to Quran text or metadata must preserve the source attribution, generator
reproducibility, and all corpus integrity checks.

Keep user-facing strings in both English and Arabic resources. Do not add analytics, advertising,
permissions, network hosts, or dependencies without documenting the product and security reason.

Performance changes must keep critical user journeys in `NoorJourneys.kt`, rerun the Baseline Profile
generator, and compare physical-device Macrobenchmark JSON/traces. Release tooling changes must keep
`tools/release_preflight.py`, `tools/release_gate.sh`, and artifact signature verification passing.
