# Release procedure

## Prerequisites

- JDK 17.
- Android SDK Platform 37 and compatible build tools.
- Gradle 9.4.1 wrapper bootstrapped from the checksum-pinned official distribution.
- Emulator/device coverage at minSdk and a recent API.
- A private Play upload key stored outside the repository.
- A unique production application ID and completed publisher/provider/legal requirements.

## Release environment

Export an increasing positive version code and semantic version name:

```bash
export NOOR_VERSION_CODE=2
export NOOR_VERSION_NAME=1.0.1
```

Configure all signing values together. Never place the keystore in the repository:

```bash
export NOOR_KEYSTORE_PATH=/secure/noor-upload.jks
export NOOR_KEYSTORE_PASSWORD='...'
export NOOR_KEY_ALIAS='...'
export NOOR_KEY_PASSWORD='...'
```

Validate the workstation and secrets:

```bash
python3 tools/release_preflight.py --require-signing --require-version
```

## Quality gate

```bash
python3 tools/verify_build_foundation.py --allow-missing-wrapper-jar
python3 tools/verify_final.py
python3 tools/verify_hardening.py
python3 tools/verify_phase2_premium_home.py
python3 tools/verify_phase3_premium_reader.py
python3 tools/verify_phase4_premium_search.py
python3 tools/verify_phase5_premium_bookmarks.py
python3 tools/verify_phase6_premium_audio.py
python3 tools/verify_phase7_premium_tafsir.py
python3 tools/verify_phase8_premium_adhkar.py
python3 tools/verify_flagship_review.py
./bootstrap-gradle-wrapper.sh
python3 tools/verify_build_foundation.py --require-wrapper-jar
./gradlew clean \
  :core:common:test \
  :core:media:test \
  :domain:test \
  :data:testDebugUnitTest \
  :app:testDebugUnitTest \
  :data:assembleAndroidTest \
  :app:assembleDebug \
  :app:assembleBenchmark \
  :app:assembleNonMinifiedRelease \
  :baseline-profile:assembleBenchmark \
  :baseline-profile:assembleNonMinifiedRelease \
  :app:lintDebug \
  :app:lintRelease \
  --warning-mode=all
./gradlew :data:pixel6Api35DebugAndroidTest :app:pixel6Api35DebugAndroidTest
```

Commit the current Room schema JSON emitted into `data/schemas`. Recover historical schemas only
from authoritative tagged source/build artifacts, run `NoorDatabaseMigrationTest`, and add
`MigrationTestHelper` coverage for every supported historical upgrade once those files are available.
Rerun all gates from a clean checkout.

## Baseline Profile and Macrobenchmark

Generate profile rules on an AOSP managed device:

```bash
./gradlew :baseline-profile:pixel6Api35NonMinifiedReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
python3 tools/update_baseline_profile.py
python3 tools/verify_final.py
```

Run `StartupBenchmark` and `ReaderScrollBenchmark` on representative physical devices and retain the
JSON output and Perfetto traces. Do not treat emulator timings as release measurements.

## Signed release bundle

The canonical release command is:

```bash
tools/release_gate.sh
```

It runs the source gate, preflight, Gradle tests/build/Lint, creates `app-release.aab`, and invokes:

```bash
python3 tools/verify_android_artifact.py \
  app/build/outputs/bundle/release/app-release.aab \
  --require-signature
```

Verify the merged manifest, R8 mapping/missing-class warnings, version, certificate, Baseline Profile,
database/licenses, download size, locales, device compatibility, offline first launch, and every
external-provider failure state. Inspect transitive `.so` libraries and prove 16 KB page support.

## Play rollout

1. Upload to Internal testing and inspect App Bundle Explorer.
2. Resolve Play pre-launch, policy, accessibility, crash, ANR, and foreground-service findings.
3. Run Closed testing and validate upgrades from the previous database/app version.
4. Start a staged production rollout.
5. Monitor Android Vitals, startup, rendering, battery, audio failures, provider availability, and
   reviews; halt or roll back when release thresholds are exceeded.
