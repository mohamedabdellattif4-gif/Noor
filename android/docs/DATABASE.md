# Noor local database

## Scope

Phase 3 introduces only the Quran corpus storage required by the home, surah list, reader, and
future navigation features. User-owned bookmarks, tafsir content, audio state, and settings remain
owned by their later phases.

Room is the local source of truth. UI and use-case code consume the `QuranRepository` interface in
`:domain`; they never depend on Room, DAOs, or entities directly.

## Schema version 1

### `surahs`

| Column | Type | Notes |
|---|---|---|
| `number` | INTEGER | Primary key, 1–114 |
| `name_arabic` | TEXT | Required Arabic name |
| `name_transliterated` | TEXT nullable | Optional source-provided transliteration |
| `name_translated` | TEXT nullable | Optional source-provided translated name |
| `revelation_type` | TEXT | Canonical `MECCAN` or `MEDINAN` value |
| `ayah_count` | INTEGER | Expected number of ayahs in the surah |
| `start_page` | INTEGER | Positive page number from the approved corpus source |

### `ayahs`

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | Stable primary key supplied by the corpus importer |
| `surah_number` | INTEGER | Foreign key to `surahs.number` |
| `number_in_surah` | INTEGER | Position within the surah |
| `text_uthmani` | TEXT | Required Uthmani text |
| `juz_number` | INTEGER | 1–30 |
| `hizb_quarter` | INTEGER | 1–240 |
| `page_number` | INTEGER | Positive source page number |

The combination of `surah_number` and `number_in_surah` is unique. Indices support surah, page,
and juz reads. Deleting a surah cascades to its ayahs, while corpus replacement deletes ayahs first
so the operation remains explicit and portable.

## Import rules

`QuranCorpus` validates data before Room is touched:

- Exactly 114 surahs must be present.
- Every surah number from 1 through 114 must occur exactly once.
- Ayah IDs and `(surah, numberInSurah)` positions must be unique.
- Every ayah must reference a supplied surah.
- Each surah's metadata count must match the supplied ayahs.
- Caller collections are defensively copied after validation.

`replaceCorpus` then executes the complete replacement inside one Room transaction. A constraint or
write failure rolls the entire transaction back, preserving the previous valid corpus.

No Quran text is bundled in Phase 3. A licensed and verified source must be approved before corpus
assets or an importer are added.

## Migrations

The database starts at version 1, so there is no legitimate predecessor migration. The production
builder always calls `addMigrations` with the central registry, and it deliberately does not call
`fallbackToDestructiveMigration`.

The Room Gradle plugin exports schemas into `data/schemas`. Every generated JSON schema must be
committed. The first real schema change must:

1. Increment the database version.
2. Add an explicit automatic or manual migration.
3. Register the migration in `NoorDatabaseMigrations.ALL`.
4. Add a migration test from every supported prior version.
5. Preserve user-owned data once later phases introduce it.

## Tests

`NoorDatabaseTest` uses an in-memory Room database and verifies:

- Ayahs are returned in verse order even when inserted out of order.
- A failed corpus replacement rolls back and preserves the previous rows.

The repository also includes environment-independent checks that recreate the logical schema with
SQLite, test foreign keys and unique indices, and execute domain-model invariants on JVM 17.
