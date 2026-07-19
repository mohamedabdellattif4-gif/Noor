# Final verification

**Final repository review date:** 2026-07-17
**Repository verdict:** production-hardened source-level release candidate; Android build/device gate remains external.

## Review passes completed

1. Architecture and feature-completeness pass.
2. Code, security, performance, and Play-readiness remediation pass.
3. Independent regression re-review after functional fixes.
4. Production-hardening pass for benchmarks, Baseline Profiles, CI, supply-chain scanning, release
   preflight, version/signing controls, artifact verification, and per-app language metadata.

## Completed environment-independent gates

- Architecture/module dependency review, including isolation of the test-only Benchmark module.
- Source-level code, security, performance, and Play-readiness regression reviews.
- Version Catalog, Gradle script policy, XML, manifest, resources, and Arabic/English parity checks.
- Exact Quran corpus checks: 114 surahs, 6,236 ayahs, pages 1..604, juz 1..30, and rub 1..240.
- SQLite integrity/foreign-key/FTS row and trigger checks, query safety, performance, and regeneration.
- Exact Tanzil display-text fidelity and normalized search-field comparison.
- Reader-position, search-state, cancellation, retry, adaptive-width, network-boundary, and audio URL checks.
- Android-free Kotlin compilation for JVM 17 with warnings treated as errors.
- Baseline Profile source validation and strict critical-user-journey checks.
- Release version/signing/preflight policy checks.
- Synthetic unsigned/signed AAB verification, including required assets, licenses, DEX, profile,
  checksum, accidental key material, and JAR signature.
- CI, CodeQL, dependency review/submission, Dependabot, and workflow-YAML checks.
- Shell syntax, Python bytecode compilation, text hygiene, license, privacy, and release documentation.

## Latest local result

```text
FINAL VERIFICATION: PASSED
HARDENING VERIFICATION: PASSED
SQLite FTS host benchmark p95: 0.460 ms (gate: < 50 ms)
Pure Kotlin JVM 17 files compiled with warnings as errors: 18
Kotlin/Kotlin DSL files in repository: 107
XML files: 28
Test/benchmark/profile classes: 12
GitHub Actions workflows: 5
Prepackaged database: 3,252,224 bytes
Database SHA-256: a77c6155204cbe13133ab09a253d030622d4cc2ac0c5aa2514cda007d24bfda1
```

The optional Tree-sitter Kotlin parser is unavailable in this sandbox. Android-free Kotlin is
compiled with `kotlinc`; Android/Kotlin DSL compilation remains part of the external Gradle gate.

## Android build attempt

The checksum-verified Wrapper bootstrap was retried and stopped before project configuration:

```text
curl: (6) Could not resolve host: services.gradle.org
```

The environment provides JDK 21 rather than JDK 17 and has no Android SDK or `gradle-wrapper.jar`.
Consequently none of the following is claimed as executed:

- AGP configuration and resolved dependency graph.
- Compose/Hilt/KSP/Room generated-code compilation or generated Room schema JSON.
- Gradle unit tests, Benchmark variant compilation, Android Lint, R8, or resource shrinking.
- Instrumentation/device tests, background audio/process-death tests, or accessibility scans.
- Managed-device Baseline Profile generation or physical-device Macrobenchmark measurements.
- Signed production AAB, 16 KB native-library inspection, or Play pre-launch report.

Run `tools/release_gate.sh` and the physical-device commands in `RELEASE.md` on a configured
workstation. Publisher identity, provider rights, public policy/support details, and Play Console
operations also remain external.
