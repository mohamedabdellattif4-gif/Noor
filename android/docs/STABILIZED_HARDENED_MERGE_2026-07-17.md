# Stabilized + Production Hardened merge — 2026-07-17

This release uses the Production Hardened repository as the base and incorporates the independently
validated stabilization fixes without removing the hardening, CI, benchmark, testing, signing, or
release-gate work.

## Merged fixes

1. `MIGRATION_2_3` explicitly recreates the four Room external-content FTS synchronization triggers.
2. `tools/verify_final.py` executes a temporary SQLite regression test for insert, update, stale-term
   removal, and delete synchronization after the migration trigger statements are applied.
3. Unix and Windows Gradle launchers fail with a clear bootstrap instruction when
   `gradle-wrapper.jar` is missing.
4. All Production Hardened capabilities remain present, including ViewModel tests, the audio
   abstraction, Macrobenchmark/Baseline Profile sources, GitHub Actions security and build workflows,
   release preflight, signature verification, locale configuration, and dependency automation.

## Honest release status

The merged source-level verification gates can run in the isolated environment. A real Android Gradle
build, KSP/Hilt/Room code generation, Android Lint, emulator/device tests, R8, and signed AAB generation
still require JDK 17, Android SDK Platform 37, and the Gradle Wrapper distribution.
