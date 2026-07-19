# Phase 3 verification

## Completed scope

Phase 3 adds Room-based Quran corpus persistence, domain models, DAOs, a local data source,
repository implementation, Hilt bindings, schema export configuration, migration policy, and
focused database tests.

No Quran corpus asset was added because its text, numbering, license, and provenance require an
approved source. Bookmarks, tafsir, audio, settings, DataStore, Media3, and WorkManager remain out of
scope.

## Review findings fixed

- Relaxed page validation from a hard-coded 604-page ceiling to a positive source page, avoiding a
  database contract tied to one Mushaf layout.
- Made translated and transliterated surah names nullable so the storage contract does not require a
  specific multilingual corpus provider.
- Required `QuranCorpus` to contain every surah number exactly once.
- Added defensive copies to prevent post-validation mutation of corpus lists.
- Made the populated-state check verify 114 surahs and an ayah total matching surah metadata.
- Wrapped the populated-state check in a transaction for one consistent database snapshot.
- Fixed the SQLite rollback test by committing its baseline data before starting the failure case.
- Kept the migration registry explicit but empty instead of inventing an artificial version change.
- Prohibited destructive migration fallback.

## Checks that passed

- Phase 1 and Phase 2 regression verification.
- Version Catalog parsing and Room 2.8.4 pinning.
- Room plugin, KSP, Hilt, and schema-directory configuration checks.
- Room isolation to the `:data` module.
- Entity, foreign-key, unique-index, and navigation-index checks.
- DAO Flow and suspend API checks.
- Domain repository contract and data implementation checks.
- Hilt provider and binding checks.
- Transaction ordering and no-destructive-fallback checks.
- SQLite insertion, ordering prerequisites, foreign-key rejection, uniqueness rejection, and
  rollback preservation.
- JVM 17 compilation and execution of domain invariants with warnings treated as errors.
- Synthetic Kotlin boundary compilation of all Phase 3 main sources against minimal framework
  stubs, with warnings treated as errors.
- Scope checks proving no later-phase entities or dependencies were introduced.

Run the environment-independent checks with:

```bash
python3 tools/verify_phase1.py
python3 tools/verify_phase2.py
python3 tools/verify_phase3.py
python3 tools/verify_phase3_kotlin.py
```

The synthetic boundary compiler catches Kotlin syntax and type-shape regressions. It does not
replace Android Gradle, Room KSP SQL verification, Hilt code generation, or Android Lint.

## Android build gate

The current execution environment has no Android SDK, Gradle installation, or wrapper JAR, so it
cannot execute the definitive Android gate.

On a workstation with JDK 17 and Android SDK Platform 37, run:

```bash
./bootstrap-gradle-wrapper.sh
./gradlew clean \
  :data:assembleDebug \
  :data:assembleAndroidTest \
  :app:assembleDebug \
  lintDebug \
  --warning-mode=all
```

Windows PowerShell:

```powershell
./bootstrap-gradle-wrapper.ps1
./gradlew.bat clean `
  :data:assembleDebug `
  :data:assembleAndroidTest `
  :app:assembleDebug `
  lintDebug `
  --warning-mode=all
```

After the build, confirm that Room generated and committed the version 1 JSON schema under
`data/schemas`.

Run the instrumentation tests on an emulator or connected device:

```bash
./gradlew :data:connectedDebugAndroidTest
```

Phase 3 is fully closed only after these commands pass without unresolved warnings and the generated
schema is committed.
