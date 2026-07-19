#!/usr/bin/env python3
"""Static, domain, and SQLite integrity checks for Noor Phase 3."""

from __future__ import annotations

import re
import sqlite3
import subprocess
import sys
import tempfile
import tomllib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ERRORS: list[str] = []


def require(condition: bool, message: str) -> None:
    if not condition:
        ERRORS.append(message)


def read(relative_path: str) -> str:
    path = ROOT / relative_path
    require(path.is_file(), f"Missing required file: {relative_path}")
    return path.read_text(encoding="utf-8") if path.is_file() else ""


phase2 = subprocess.run(
    [sys.executable, str(ROOT / "tools/verify_phase2.py")],
    cwd=ROOT,
    capture_output=True,
    text=True,
    check=False,
)
require(
    phase2.returncode == 0,
    "Phase 2 regression verification failed:\n" + (phase2.stdout + phase2.stderr).strip(),
)

required_files = {
    "domain/src/main/kotlin/com/noor/domain/model/RevelationType.kt",
    "domain/src/main/kotlin/com/noor/domain/model/Surah.kt",
    "domain/src/main/kotlin/com/noor/domain/model/Ayah.kt",
    "domain/src/main/kotlin/com/noor/domain/model/QuranCorpus.kt",
    "domain/src/main/kotlin/com/noor/domain/repository/QuranRepository.kt",
    "data/src/main/kotlin/com/noor/data/local/entity/SurahEntity.kt",
    "data/src/main/kotlin/com/noor/data/local/entity/AyahEntity.kt",
    "data/src/main/kotlin/com/noor/data/local/dao/SurahDao.kt",
    "data/src/main/kotlin/com/noor/data/local/dao/AyahDao.kt",
    "data/src/main/kotlin/com/noor/data/local/database/NoorDatabase.kt",
    "data/src/main/kotlin/com/noor/data/local/database/NoorDatabaseMigrations.kt",
    "data/src/main/kotlin/com/noor/data/local/QuranLocalDataSource.kt",
    "data/src/main/kotlin/com/noor/data/local/RoomQuranLocalDataSource.kt",
    "data/src/main/kotlin/com/noor/data/mapper/QuranMappers.kt",
    "data/src/main/kotlin/com/noor/data/repository/DefaultQuranRepository.kt",
    "data/src/main/kotlin/com/noor/data/di/DatabaseModule.kt",
    "data/src/main/kotlin/com/noor/data/di/DataBindingsModule.kt",
    "data/schemas/README.md",
    "data/src/androidTest/kotlin/com/noor/data/local/database/NoorDatabaseTest.kt",
}
for required_file in required_files:
    require((ROOT / required_file).is_file(), f"Missing Phase 3 file: {required_file}")

with (ROOT / "gradle/libs.versions.toml").open("rb") as catalog_file:
    catalog = tomllib.load(catalog_file)
require(catalog.get("versions", {}).get("room") == "2.8.4", "Room must be pinned to 2.8.4")
for library in (
    "androidx-room-runtime",
    "androidx-room-ktx",
    "androidx-room-compiler",
    "kotlinx-coroutines-core",
):
    require(library in catalog.get("libraries", {}), f"Missing Version Catalog library: {library}")
require("room" in catalog.get("plugins", {}), "Room Gradle plugin is missing")

data_gradle = read("data/build.gradle.kts")
for expected in (
    "alias(libs.plugins.android.library)",
    "alias(libs.plugins.ksp)",
    "alias(libs.plugins.hilt)",
    "alias(libs.plugins.room)",
    'schemaDirectory("$projectDir/schemas")',
    "implementation(libs.androidx.room.runtime)",
    "implementation(libs.androidx.room.ktx)",
    "ksp(libs.androidx.room.compiler)",
    "testInstrumentationRunner = \"androidx.test.runner.AndroidJUnitRunner\"",
    "androidTestImplementation(libs.androidx.test.core.ktx)",
    "androidTestImplementation(libs.androidx.test.ext.junit.ktx)",
    "androidTestImplementation(libs.androidx.test.runner)",
):
    require(expected in data_gradle, f"data/build.gradle.kts is missing: {expected}")

for module in ("app", "core/common", "core/designsystem", "core/navigation", "domain"):
    gradle_file = ROOT / module / "build.gradle.kts"
    if gradle_file.is_file():
        content = gradle_file.read_text(encoding="utf-8").lower()
        require("androidx.room" not in content, f"Room leaked into {module}")
        require("libs.plugins.room" not in content, f"Room plugin leaked into {module}")

database = read("data/src/main/kotlin/com/noor/data/local/database/NoorDatabase.kt")
for expected in (
    "@Database(",
    "SurahEntity::class",
    "AyahEntity::class",
    "version = 1",
    "exportSchema = true",
    "abstract fun surahDao()",
    "abstract fun ayahDao()",
):
    require(expected in database, f"NoorDatabase is missing: {expected}")

surah_entity = read("data/src/main/kotlin/com/noor/data/local/entity/SurahEntity.kt")
ayah_entity = read("data/src/main/kotlin/com/noor/data/local/entity/AyahEntity.kt")
require('@Entity(tableName = "surahs")' in surah_entity, "Surah table name is incorrect")
require('tableName = "ayahs"' in ayah_entity, "Ayah table name is incorrect")
require("ForeignKey.CASCADE" in ayah_entity, "Ayah foreign key must cascade on surah deletion")
require('Index(value = ["surah_number", "number_in_surah"], unique = true)' in ayah_entity,
        "Ayah position uniqueness index is missing")
for indexed_column in ("page_number", "juz_number"):
    require(f'Index(value = ["{indexed_column}"])' in ayah_entity,
            f"Missing ayah navigation index: {indexed_column}")

surah_dao = read("data/src/main/kotlin/com/noor/data/local/dao/SurahDao.kt")
ayah_dao = read("data/src/main/kotlin/com/noor/data/local/dao/AyahDao.kt")
for expected in ("Flow<List<SurahEntity>>", "suspend fun count()", "suspend fun totalAyahCount()", "@Upsert", 'DELETE FROM surahs'):
    require(expected in surah_dao, f"SurahDao is missing: {expected}")
for expected in (
    "Flow<List<AyahEntity>>",
    "observeForSurah",
    "observeForPage",
    "observeForJuz",
    "suspend fun getById",
    "suspend fun count()",
    "@Upsert",
    "DELETE FROM ayahs",
):
    require(expected in ayah_dao, f"AyahDao is missing: {expected}")

repository_contract = read("domain/src/main/kotlin/com/noor/domain/repository/QuranRepository.kt")
repository_impl = read("data/src/main/kotlin/com/noor/data/repository/DefaultQuranRepository.kt")
for expected in (
    "interface QuranRepository",
    "Flow<List<Surah>>",
    "Flow<List<Ayah>>",
    "suspend fun replaceCorpus(corpus: QuranCorpus)",
):
    require(expected in repository_contract, f"QuranRepository is missing: {expected}")
require(": QuranRepository" in repository_impl, "DefaultQuranRepository does not implement its contract")
require("localDataSource" in repository_impl, "Repository must use the local data source")

local_source = read("data/src/main/kotlin/com/noor/data/local/RoomQuranLocalDataSource.kt")
require("database.withTransaction" in local_source, "Corpus replacement must be transactional")
delete_ayah_position = local_source.find("ayahDao.deleteAll()")
delete_surah_position = local_source.find("surahDao.deleteAll()")
insert_surah_position = local_source.find("surahDao.upsertAll(surahs)")
insert_ayah_position = local_source.find("ayahDao.upsertAll(ayahs)")
require(
    -1 not in (delete_ayah_position, delete_surah_position, insert_surah_position, insert_ayah_position)
    and delete_ayah_position < delete_surah_position < insert_surah_position < insert_ayah_position,
    "Corpus replacement order must respect foreign keys",
)


database_test = read("data/src/androidTest/kotlin/com/noor/data/local/database/NoorDatabaseTest.kt")
for expected in (
    "Room.inMemoryDatabaseBuilder",
    "ayahsAreOrderedWithinTheirSurah",
    "failedCorpusReplacementRollsBackPreviousData",
    "RoomQuranLocalDataSource",
):
    require(expected in database_test, f"Database instrumentation test is missing: {expected}")

all_source = "\n".join(path.read_text(encoding="utf-8") for path in ROOT.rglob("*.kt"))
all_gradle = "\n".join(path.read_text(encoding="utf-8") for path in ROOT.rglob("*.gradle.kts"))
require("fallbackToDestructiveMigration" not in all_source, "Destructive migration fallback is forbidden")
for forbidden in ("BookmarkEntity", "TafsirEntity", "AudioEntity", "SettingEntity"):
    require(forbidden not in all_source, f"Out-of-scope Phase 3 entity found: {forbidden}")
for forbidden_dependency in ("androidx.datastore", "androidx.media3", "androidx.work"):
    require(forbidden_dependency not in all_gradle.lower(),
            f"Out-of-scope dependency detected: {forbidden_dependency}")

migrations = read("data/src/main/kotlin/com/noor/data/local/database/NoorDatabaseMigrations.kt")
require("Array<Migration> = emptyArray()" in migrations, "Version 1 migration registry must be explicit")
require("Destructive fallback is intentionally forbidden" in migrations,
        "Migration safety policy must be documented")

# Validate the logical schema and its transaction semantics with SQLite itself.
connection = sqlite3.connect(":memory:")
connection.execute("PRAGMA foreign_keys = ON")
connection.executescript(
    """
    CREATE TABLE surahs (
        number INTEGER NOT NULL PRIMARY KEY,
        name_arabic TEXT NOT NULL,
        name_transliterated TEXT,
        name_translated TEXT,
        revelation_type TEXT NOT NULL,
        ayah_count INTEGER NOT NULL,
        start_page INTEGER NOT NULL
    );
    CREATE TABLE ayahs (
        id INTEGER NOT NULL PRIMARY KEY,
        surah_number INTEGER NOT NULL,
        number_in_surah INTEGER NOT NULL,
        text_uthmani TEXT NOT NULL,
        juz_number INTEGER NOT NULL,
        hizb_quarter INTEGER NOT NULL,
        page_number INTEGER NOT NULL,
        FOREIGN KEY(surah_number) REFERENCES surahs(number) ON DELETE CASCADE
    );
    CREATE INDEX index_ayahs_surah_number ON ayahs(surah_number);
    CREATE UNIQUE INDEX index_ayahs_surah_number_number_in_surah
        ON ayahs(surah_number, number_in_surah);
    CREATE INDEX index_ayahs_page_number ON ayahs(page_number);
    CREATE INDEX index_ayahs_juz_number ON ayahs(juz_number);
    """
)
connection.execute(
    "INSERT INTO surahs VALUES (?, ?, ?, ?, ?, ?, ?)",
    (1, "الفاتحة", "Al-Fatihah", "The Opening", "MECCAN", 2, 1),
)
connection.executemany(
    "INSERT INTO ayahs VALUES (?, ?, ?, ?, ?, ?, ?)",
    [
        (1, 1, 1, "نص تجريبي", 1, 1, 1),
        (2, 1, 2, "نص تجريبي آخر", 1, 1, 1),
    ],
)
connection.commit()
require(connection.execute("SELECT COUNT(*) FROM ayahs").fetchone()[0] == 2,
        "SQLite schema failed to store ayahs")
try:
    connection.execute(
        "INSERT INTO ayahs VALUES (?, ?, ?, ?, ?, ?, ?)",
        (3, 1, 2, "duplicate", 1, 1, 1),
    )
    ERRORS.append("Unique ayah position constraint did not reject a duplicate")
except sqlite3.IntegrityError:
    pass
connection.rollback()

try:
    with connection:
        connection.execute("DELETE FROM ayahs")
        connection.execute("DELETE FROM surahs")
        connection.execute(
            "INSERT INTO ayahs VALUES (?, ?, ?, ?, ?, ?, ?)",
            (3, 99, 1, "orphan", 1, 1, 1),
        )
except sqlite3.IntegrityError:
    pass
require(connection.execute("SELECT COUNT(*) FROM surahs").fetchone()[0] == 1,
        "Failed corpus replacement did not roll back surahs")
require(connection.execute("SELECT COUNT(*) FROM ayahs").fetchone()[0] == 2,
        "Failed corpus replacement did not roll back ayahs")
connection.close()

# Compile and execute framework-independent model invariants with warnings as errors.
model_files = [
    ROOT / "domain/src/main/kotlin/com/noor/domain/model/RevelationType.kt",
    ROOT / "domain/src/main/kotlin/com/noor/domain/model/Surah.kt",
    ROOT / "domain/src/main/kotlin/com/noor/domain/model/Ayah.kt",
    ROOT / "domain/src/main/kotlin/com/noor/domain/model/QuranCorpus.kt",
]
with tempfile.TemporaryDirectory() as temporary_directory:
    temporary = Path(temporary_directory)
    test_source = temporary / "Phase3DomainCheck.kt"
    test_source.write_text(
        """
        import com.noor.domain.model.Ayah
        import com.noor.domain.model.QuranCorpus
        import com.noor.domain.model.RevelationType
        import com.noor.domain.model.Surah

        fun main() {
            val surahs = (1..114).map { number ->
                Surah(number, "سورة $number", null, null, RevelationType.MECCAN, 1, number)
            }
            val ayahs = (1..114).map { number ->
                Ayah(number, number, 1, "نص", 1, 1, number)
            }
            val mutableAyahs = ayahs.toMutableList()
            val corpus = QuranCorpus(surahs, mutableAyahs)
            mutableAyahs.clear()
            check(corpus.ayahs.size == 114)
            check(runCatching { QuranCorpus(surahs.dropLast(1), ayahs.dropLast(1)) }.isFailure)
            check(runCatching { QuranCorpus(surahs, ayahs + ayahs.first()) }.isFailure)
            check(runCatching { Ayah(0, 1, 1, "نص", 1, 1, 1) }.isFailure)
            check(Surah(1, "الفاتحة", null, null, RevelationType.MECCAN, 7, 1).nameTranslated == null)
        }
        """.strip(),
        encoding="utf-8",
    )
    output_jar = temporary / "phase3-domain-check.jar"
    compilation = subprocess.run(
        [
            "kotlinc",
            *[str(path) for path in model_files],
            str(test_source),
            "-Werror",
            "-jvm-target",
            "17",
            "-include-runtime",
            "-d",
            str(output_jar),
        ],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    require(
        compilation.returncode == 0,
        "Domain model compilation failed:\n" + (compilation.stdout + compilation.stderr).strip(),
    )
    if compilation.returncode == 0:
        execution = subprocess.run(
            ["java", "-jar", str(output_jar)],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )
        require(
            execution.returncode == 0,
            "Domain invariant execution failed:\n" + (execution.stdout + execution.stderr).strip(),
        )

if ERRORS:
    print("Phase 3 verification failed:")
    for error in ERRORS:
        print(f"- {error}")
    sys.exit(1)

print("Phase 3 static and database verification passed.")
print("Validated Room boundaries, schema constraints, transactional rollback, and domain invariants.")
print(f"Checked {len(list(ROOT.rglob('*.kt')))} Kotlin files.")
