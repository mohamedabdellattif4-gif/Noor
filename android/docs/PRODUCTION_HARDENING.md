# Production hardening pass

**Date:** 2026-07-17
**Result:** source-level verification passed; Android toolchain and device gates remain external.

## Goals

This pass strengthens repeatability, performance measurement, release safety, localization metadata,
and supply-chain visibility without changing Noor's product scope or architecture.

## Performance tooling

- Added the `:baseline-profile` `com.android.test` module.
- Added a release-equivalent, minified, non-debuggable `benchmark` app build type for performance
  measurement and a separate non-debuggable `nonMinifiedRelease` build type for profile generation.
  Both use the local debug key and match Release dependencies.
- Kept shell profiling restricted to Benchmark and non-minified profile-generation source-set
  manifests; production Release is not profileable.
- Added `BaselineProfileGenerator` covering startup, opening Al-Fatiha, and reader scrolling. It runs
  against the non-minified variant so generated source rules do not contain R8-obfuscated names.
- Added cold-start comparisons with no compilation versus the packaged Baseline Profile.
- Added reader `FrameTimingMetric` scrolling measurement.
- Extracted shared critical-user journeys so profile generation and benchmarks exercise the same path
  and fail if the expected UI never appears.
- Connected Compose `ReportDrawnWhen` to completion of the Home loading state so startup metrics can
  include time to full display.
- Added `tools/update_baseline_profile.py` to locate, validate, and install generated profile rules.
- Kept the stable Benchmark 1.4.1 libraries and plugin, while deliberately avoiding the stable
  Baseline Profile Gradle plugin on AGP 9.2 because that consumer plugin requires the legacy AGP DSL.
  The checked-in `app/src/main/baseline-prof.txt` is compiled by AGP and installed locally through
  ProfileInstaller.

Managed-device profile generation is automated for repeatability, but performance numbers from an
emulator are not release evidence. Startup and frame results must be measured on representative
physical devices.

## Release safety

- Version code can be supplied with `NOOR_VERSION_CODE` and must be positive.
- Version name can be supplied with `NOOR_VERSION_NAME` and must use semantic versioning.
- Release signing accepts only the complete set of `NOOR_KEYSTORE_*` variables.
- The keystore must exist and must be outside the source repository.
- `tools/release_preflight.py` checks JDK 17, Android SDK Platform 37, version inputs, signing inputs,
  and keystore location before a release.
- `tools/verify_android_artifact.py` verifies required Quran assets, license notices, DEX payload,
  Baseline Profile packaging and size, accidental key material, native libraries, checksum, and—when
  requested—the APK/AAB signature.
- `tools/release_gate.sh` now requires explicit versioning and signing and rejects an unsigned final
  artifact.

## CI and supply chain

- Android CI runs source verification, unit tests, Android-test compilation, Debug and Benchmark app
  assembly, Benchmark-module assembly, strict Lint, release bundle generation, and artifact checks.
- A manual managed-device workflow generates and validates Baseline Profile rules.
- Dependency Review rejects pull requests with moderate-or-higher known dependency vulnerabilities.
- Dependabot watches Gradle and GitHub Actions dependencies weekly.
- Gradle dependency submission populates GitHub's dependency graph.
- CodeQL analyzes Java and Kotlin on pushes, pull requests, and a weekly schedule.
- GitHub Actions use the current supported major releases and Gradle Wrapper validation.

## Testability and regression coverage

- Added the `QuranAudioPlayer` boundary so Reader presentation can be tested without Android Context,
  MediaController, or ExoPlayer. Hilt binds the production Media3 implementation as one singleton.
- Added coroutine unit tests for Home flow recovery, Bookmarks flow recovery/removal, Reader playback
  start position, last-read persistence, and bookmark updates.
- Release-key checks now compare canonical paths and the hardening suite proves that an external
  symlink resolving to a repository-contained key is rejected.

## Platform and UX metadata

- Added Android per-app language metadata for Arabic and English through `android:localeConfig`.
- Added a benchmark-only `profileable` manifest overlay; Release keeps the smaller security surface.
- Kept `targetSdk 36`, `compileSdk 37`, R8 optimization, resource shrinking, strict Lint, HTTPS-only
  networking, disabled backup, and minimal permissions.

## Remaining external gates

1. Run the Gradle/AGP build with JDK 17 and Android SDK Platform 37.
2. Run unit, Lint, generated-code, Room migration, and instrumentation tests.
3. Generate a fresh Baseline Profile and measure startup/scroll performance on physical devices.
4. Build a signed AAB and pass the artifact verifier with `--require-signature`.
5. Inspect any transitive native libraries for 16 KB page compatibility.
6. Complete accessibility, process-death, background-audio, network-failure, battery, and Android
   Vitals testing.
7. Complete publisher, provider-rights, privacy URL, store listing, Play Console, and staged rollout
   requirements.
