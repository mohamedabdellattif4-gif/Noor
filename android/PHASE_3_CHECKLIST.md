# Phase 3 checklist

## Implementation

- [x] Room 2.8.4 configured through the Version Catalog
- [x] Room Gradle plugin and schema export directory
- [x] Framework-independent `Surah`, `Ayah`, `RevelationType`, and `QuranCorpus` models
- [x] Validated full-corpus import contract
- [x] `surahs` and `ayahs` Room entities
- [x] Foreign key from ayahs to surahs with cascade delete
- [x] Unique surah/ayah-position constraint
- [x] Page, juz, and surah lookup indices
- [x] Reactive DAOs using `Flow`
- [x] One-shot DAO operations using suspend functions
- [x] Atomic corpus replacement transaction
- [x] Domain-owned `QuranRepository` contract
- [x] Room-backed repository implementation and mappers
- [x] Hilt database, DAO, data-source, and repository bindings
- [x] Explicit migration registry with no destructive fallback
- [x] Versioned Room schema export policy
- [x] Android instrumentation tests for ordering and rollback
- [x] No Quran text corpus bundled without an approved source
- [x] No bookmark, tafsir, audio, settings, DataStore, Media3, or WorkManager scope leakage

## Verification

- [x] Phase 1 regression checks
- [x] Phase 2 regression checks
- [x] Phase 3 static integrity checks
- [x] SQLite foreign-key, uniqueness, indexing, and rollback checks
- [x] Domain invariant compilation and execution on JVM 17
- [x] Main Phase 3 Kotlin boundary compilation with warnings as errors
- [ ] Android Gradle build, KSP, Room SQL validation, and lint in an Android SDK environment
- [ ] Instrumentation tests on an emulator or device
- [ ] Generated Room version 1 schema committed after the first successful Android build
