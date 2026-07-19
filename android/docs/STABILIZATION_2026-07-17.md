# Stabilization pass — 2026-07-17

## Scope

This pass re-opened the source-level release candidate and validated its claims independently instead
of relying only on the existing review documents.

## Fixed defect

### Room 2 -> 3 FTS migration did not recreate synchronization triggers

Room removes external-content FTS synchronization triggers before migrations. The existing `2 -> 3`
migration created and rebuilt `ayahs_fts`, but assumed Room would recreate the four triggers after the
migration. That assumption could leave migrated installations with a search index that stopped
tracking inserted, updated, or deleted ayah rows.

The migration now explicitly recreates these triggers:

- `room_fts_content_sync_ayahs_fts_AFTER_INSERT`
- `room_fts_content_sync_ayahs_fts_BEFORE_DELETE`
- `room_fts_content_sync_ayahs_fts_BEFORE_UPDATE`
- `room_fts_content_sync_ayahs_fts_AFTER_UPDATE`

## Regression protection

`tools/verify_final.py` now:

1. Requires the `2 -> 3` migration to invoke the FTS trigger creation helper.
2. Extracts the four trigger statements from the Kotlin migration source.
3. Applies them to a temporary SQLite database.
4. Verifies FTS synchronization for insert, update, stale-term removal, and delete operations.

The previous verifier rule that prohibited triggers inside migrations was removed because it encoded
the incorrect assumption that caused the defect.

## Build usability

The Unix and Windows Gradle launcher scripts now stop with a clear instruction when
`gradle-wrapper.jar` has not yet been bootstrapped, rather than failing with an opaque Java
`ClassNotFoundException`.

## Verification completed

- `python3 tools/verify_final.py`: passed.
- Migration Kotlin file type-checked with JVM 17-compatible local stubs and warnings-as-errors: passed.
- Unix shell syntax for `gradlew` and `bootstrap-gradle-wrapper.sh`: passed.
- Prepackaged Quran database integrity, corpus fidelity, FTS benchmark, resources, architecture and
  Android-free Kotlin compilation: passed through the final verifier.

## Remaining external gate

A real AGP build, Android Lint, KSP/Hilt/Room generated-code compilation, schema export and device
instrumentation still require the Gradle Wrapper distribution, JDK 17 toolchain and Android SDK
Platform 37. Those dependencies are not available in the current isolated execution environment, so
no Android APK/AAB build is claimed by this pass.
