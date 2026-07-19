package com.noor.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.noor.core.common.text.ArabicNormalizer

object NoorDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE ayahs_new (
                    id INTEGER NOT NULL PRIMARY KEY,
                    surah_number INTEGER NOT NULL,
                    number_in_surah INTEGER NOT NULL,
                    text_uthmani TEXT NOT NULL,
                    text_simple TEXT NOT NULL,
                    juz_number INTEGER NOT NULL,
                    hizb_quarter INTEGER NOT NULL,
                    page_number INTEGER NOT NULL,
                    FOREIGN KEY(surah_number) REFERENCES surahs(number) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO ayahs_new (
                    id, surah_number, number_in_surah, text_uthmani, text_simple,
                    juz_number, hizb_quarter, page_number
                )
                SELECT id, surah_number, number_in_surah, text_uthmani, text_uthmani,
                    juz_number, hizb_quarter, page_number
                FROM ayahs
                """.trimIndent(),
            )
            database.execSQL("DROP TABLE ayahs")
            database.execSQL("ALTER TABLE ayahs_new RENAME TO ayahs")
            database.execSQL("CREATE INDEX index_ayahs_surah_number ON ayahs(surah_number)")
            database.execSQL(
                "CREATE UNIQUE INDEX index_ayahs_surah_number_number_in_surah " +
                    "ON ayahs(surah_number, number_in_surah)",
            )
            database.execSQL("CREATE INDEX index_ayahs_page_number ON ayahs(page_number)")
            database.execSQL("CREATE INDEX index_ayahs_juz_number ON ayahs(juz_number)")
            database.query("SELECT id, text_uthmani FROM ayahs").use { cursor ->
                val update = database.compileStatement("UPDATE ayahs SET text_simple = ? WHERE id = ?")
                while (cursor.moveToNext()) {
                    update.clearBindings()
                    update.bindString(1, ArabicNormalizer.normalize(cursor.getString(1)))
                    update.bindLong(2, cursor.getLong(0))
                    update.executeUpdateDelete()
                }
            }
            database.execSQL("CREATE INDEX index_ayahs_text_simple ON ayahs(text_simple)")
            database.execSQL(
                """
                CREATE TABLE bookmarks (
                    ayah_id INTEGER NOT NULL PRIMARY KEY,
                    created_at INTEGER NOT NULL,
                    FOREIGN KEY(ayah_id) REFERENCES ayahs(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            database.execSQL("CREATE INDEX index_bookmarks_created_at ON bookmarks(created_at)")
            database.execSQL(
                """
                CREATE TABLE tafsirs (
                    ayah_id INTEGER NOT NULL PRIMARY KEY,
                    text TEXT NOT NULL,
                    source_key TEXT NOT NULL,
                    source_name TEXT NOT NULL,
                    fetched_at INTEGER NOT NULL,
                    FOREIGN KEY(ayah_id) REFERENCES ayahs(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            database.execSQL("CREATE INDEX index_tafsirs_fetched_at ON tafsirs(fetched_at)")
            database.execSQL(
                "CREATE TABLE app_metadata (`key` TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)",
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP INDEX IF EXISTS index_ayahs_text_simple")
            database.execSQL(
                "CREATE VIRTUAL TABLE ayahs_fts USING FTS4(text_simple, content='ayahs')",
            )
            database.execSQL("INSERT INTO ayahs_fts(ayahs_fts) VALUES('rebuild')")
            createAyahFtsSyncTriggers(database)
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE tafsirs ADD COLUMN footnotes TEXT")
        }
    }

    private fun createAyahFtsSyncTriggers(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_ayahs_fts_AFTER_INSERT
            AFTER INSERT ON ayahs BEGIN
                INSERT INTO ayahs_fts(docid, text_simple) VALUES (new.id, new.text_simple);
            END
            """.trimIndent(),
        )
        database.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_ayahs_fts_BEFORE_DELETE
            BEFORE DELETE ON ayahs BEGIN
                DELETE FROM ayahs_fts WHERE docid = old.id;
            END
            """.trimIndent(),
        )
        database.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_ayahs_fts_BEFORE_UPDATE
            BEFORE UPDATE ON ayahs BEGIN
                DELETE FROM ayahs_fts WHERE docid = old.id;
            END
            """.trimIndent(),
        )
        database.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_ayahs_fts_AFTER_UPDATE
            AFTER UPDATE ON ayahs BEGIN
                INSERT INTO ayahs_fts(docid, text_simple) VALUES (new.id, new.text_simple);
            END
            """.trimIndent(),
        )
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}
