#!/usr/bin/env python3
"""Generate Noor's deterministic, pre-populated Room database from vendored Quran data."""
from __future__ import annotations

import argparse
import json
import re
import sqlite3
import subprocess
import unicodedata
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
QURAN_META = ROOT / "tools/vendor/ayah_metadata.json"
TANZIL_XML = ROOT / "tools/vendor/tanzil-quran-uthmani.xml"
OUTPUT = ROOT / "data/src/main/assets/database/noor.db"
EXPECTED_AYAHS = 6236
EXPECTED_SURAHS = 114


def normalize_arabic(text: str) -> str:
    text = unicodedata.normalize("NFKD", text)
    chars: list[str] = []
    replacements = str.maketrans({
        "أ": "ا", "إ": "ا", "آ": "ا", "ٱ": "ا",
        "ى": "ي", "ؤ": "و", "ئ": "ي", "ة": "ه",
        "ـ": " ",
    })
    text = text.translate(replacements)
    for char in text:
        category = unicodedata.category(char)
        if category.startswith("M"):
            continue
        if char.isalnum() or char.isspace():
            chars.append(char)
        else:
            chars.append(" ")
    return re.sub(r"\s+", " ", "".join(chars)).strip()


def generate_metadata() -> None:
    package = Path("/mnt/data/pypi/quran-meta/package")
    script = ROOT / "tools/vendor/generate_metadata.mjs"
    script.write_text(
        """import { getAyahMeta, getSurahMeta, meta } from '/mnt/data/pypi/quran-meta/package/dist/hafs.js';
const out = { meta, ayahs: [], surahs: [] };
for (let id = 1; id <= meta.numAyahs; id++) out.ayahs.push({ id, ...getAyahMeta(id) });
for (let number = 1; number <= meta.numSurahs; number++) out.surahs.push(getSurahMeta(number));
process.stdout.write(JSON.stringify(out));
""",
        encoding="utf-8",
    )
    if not package.exists():
        raise SystemExit("quran-meta package is missing; run the documented vendor preparation first")
    result = subprocess.run(["node", str(script)], check=True, capture_output=True, text=True)
    QURAN_META.write_text(result.stdout, encoding="utf-8")


def create_database(output: Path) -> None:
    if not QURAN_META.exists():
        generate_metadata()

    metadata = json.loads(QURAN_META.read_text(encoding="utf-8"))
    chapters = metadata["surahs"]
    tanzil_root = ET.parse(TANZIL_XML).getroot()
    tanzil_surahs = list(tanzil_root.findall("sura"))

    if len(chapters) != EXPECTED_SURAHS or len(tanzil_surahs) != EXPECTED_SURAHS:
        raise ValueError("Every source must contain exactly 114 surahs")

    ayah_meta = {int(item["id"]): item for item in metadata["ayahs"]}
    if len(ayah_meta) != EXPECTED_AYAHS:
        raise ValueError(f"Expected {EXPECTED_AYAHS} metadata rows, got {len(ayah_meta)}")

    output.parent.mkdir(parents=True, exist_ok=True)
    output.unlink(missing_ok=True)
    connection = sqlite3.connect(output)
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
            text_simple TEXT NOT NULL,
            juz_number INTEGER NOT NULL,
            hizb_quarter INTEGER NOT NULL,
            page_number INTEGER NOT NULL,
            FOREIGN KEY(surah_number) REFERENCES surahs(number) ON UPDATE NO ACTION ON DELETE CASCADE
        );
        CREATE UNIQUE INDEX index_ayahs_surah_number_number_in_surah ON ayahs(surah_number, number_in_surah);
        CREATE INDEX index_ayahs_surah_number ON ayahs(surah_number);
        CREATE INDEX index_ayahs_page_number ON ayahs(page_number);
        CREATE INDEX index_ayahs_juz_number ON ayahs(juz_number);
        CREATE VIRTUAL TABLE ayahs_fts USING FTS4(text_simple, content='ayahs');
        CREATE TRIGGER room_fts_content_sync_ayahs_fts_AFTER_INSERT AFTER INSERT ON ayahs BEGIN
            INSERT INTO ayahs_fts(docid, text_simple) VALUES (new.id, new.text_simple);
        END;
        CREATE TRIGGER room_fts_content_sync_ayahs_fts_BEFORE_DELETE BEFORE DELETE ON ayahs BEGIN
            DELETE FROM ayahs_fts WHERE docid = old.id;
        END;
        CREATE TRIGGER room_fts_content_sync_ayahs_fts_BEFORE_UPDATE BEFORE UPDATE ON ayahs BEGIN
            DELETE FROM ayahs_fts WHERE docid = old.id;
        END;
        CREATE TRIGGER room_fts_content_sync_ayahs_fts_AFTER_UPDATE AFTER UPDATE ON ayahs BEGIN
            INSERT INTO ayahs_fts(docid, text_simple) VALUES (new.id, new.text_simple);
        END;
        CREATE TABLE bookmarks (
            ayah_id INTEGER NOT NULL PRIMARY KEY,
            created_at INTEGER NOT NULL,
            FOREIGN KEY(ayah_id) REFERENCES ayahs(id) ON UPDATE NO ACTION ON DELETE CASCADE
        );
        CREATE INDEX index_bookmarks_created_at ON bookmarks(created_at);
        CREATE TABLE tafsirs (
            ayah_id INTEGER NOT NULL PRIMARY KEY,
            text TEXT NOT NULL,
            footnotes TEXT,
            source_key TEXT NOT NULL,
            source_name TEXT NOT NULL,
            fetched_at INTEGER NOT NULL,
            FOREIGN KEY(ayah_id) REFERENCES ayahs(id) ON UPDATE NO ACTION ON DELETE CASCADE
        );
        CREATE INDEX index_tafsirs_fetched_at ON tafsirs(fetched_at);
        CREATE TABLE app_metadata (
            key TEXT NOT NULL PRIMARY KEY,
            value TEXT NOT NULL
        );
        """
    )

    global_ayah_id = 0
    for chapter, tanzil_surah in zip(chapters, tanzil_surahs, strict=True):
        number = int(chapter["surahNum"])
        if int(tanzil_surah.attrib["index"]) != number:
            raise ValueError(f"Tanzil chapter mismatch at surah {number}")

        tanzil_ayahs = list(tanzil_surah.findall("aya"))
        expected_count = int(chapter["ayahCount"])
        if len(tanzil_ayahs) != expected_count:
            raise ValueError(f"Ayah count mismatch in surah {number}")

        first_meta = ayah_meta[global_ayah_id + 1]
        connection.execute(
            "INSERT INTO surahs VALUES (?, ?, ?, ?, ?, ?, ?)",
            (
                number,
                chapter["name"],
                None,
                None,
                "MECCAN" if chapter["isMeccan"] else "MEDINAN",
                expected_count,
                int(first_meta["page"]),
            ),
        )

        for number_in_surah, tanzil_ayah in enumerate(tanzil_ayahs, start=1):
            global_ayah_id += 1
            meta = ayah_meta[global_ayah_id]
            if int(meta["surah"]) != number or int(meta["ayah"]) != number_in_surah:
                raise ValueError(f"Metadata mismatch at global ayah {global_ayah_id}")
            if int(tanzil_ayah.attrib["index"]) != number_in_surah:
                raise ValueError(f"Tanzil ayah mismatch at {number}:{number_in_surah}")
            text = tanzil_ayah.attrib["text"]
            connection.execute(
                "INSERT INTO ayahs VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    global_ayah_id,
                    number,
                    number_in_surah,
                    text,
                    normalize_arabic(text),
                    int(meta["juz"]),
                    int(meta["rubAlHizbId"]),
                    int(meta["page"]),
                ),
            )

    if global_ayah_id != EXPECTED_AYAHS:
        raise ValueError(f"Expected {EXPECTED_AYAHS} ayahs, generated {global_ayah_id}")

    metadata_rows = {
        "corpus_source": "Tanzil Uthmani Quran Text 1.0.2 (CC BY 3.0; verbatim)",
        "chapter_metadata_source": "quran-meta 6.0.17 (Hafs; MIT)",
        "pagination_source": "quran-meta 6.0.17 (Hafs; MIT)",
        "corpus_ayah_count": str(EXPECTED_AYAHS),
        "corpus_surah_count": str(EXPECTED_SURAHS),
        "database_schema_version": "4",
    }
    connection.executemany("INSERT INTO app_metadata VALUES (?, ?)", metadata_rows.items())
    connection.execute("PRAGMA user_version = 4")
    connection.commit()

    result = connection.execute(
        "SELECT (SELECT COUNT(*) FROM surahs), (SELECT COUNT(*) FROM ayahs), "
        "(SELECT COUNT(*) FROM ayahs WHERE text_simple = ''), "
        "(SELECT MIN(page_number) FROM ayahs), (SELECT MAX(page_number) FROM ayahs), "
        "(SELECT COUNT(*) FROM ayahs_fts)"
    ).fetchone()
    integrity = connection.execute("PRAGMA integrity_check").fetchone()[0]
    foreign_key_violations = connection.execute("PRAGMA foreign_key_check").fetchall()
    connection.close()

    if result != (EXPECTED_SURAHS, EXPECTED_AYAHS, 0, 1, 604, EXPECTED_AYAHS):
        raise ValueError(f"Database integrity result is unexpected: {result}")
    if integrity != "ok" or foreign_key_violations:
        raise ValueError(f"SQLite validation failed: {integrity}, {foreign_key_violations}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=OUTPUT)
    args = parser.parse_args()
    create_database(args.output)
    print(args.output)


if __name__ == "__main__":
    main()
