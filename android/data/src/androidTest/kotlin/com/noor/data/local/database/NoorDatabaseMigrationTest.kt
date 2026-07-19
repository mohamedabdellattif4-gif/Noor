package com.noor.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.noor.core.common.text.ArabicNormalizer
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoorDatabaseMigrationTest {
    private lateinit var context: Context
    private lateinit var databaseFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(DATABASE_NAME)
        databaseFile = context.getDatabasePath(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun migrationFromVersion1To4PreservesAyahAndBuildsSearchInfrastructure() = runBlocking {
        createVersion1Database()

        val room = Room.databaseBuilder(context, NoorDatabase::class.java, DATABASE_NAME)
            .addMigrations(*NoorDatabaseMigrations.ALL)
            .allowMainThreadQueries()
            .build()
        try {
            // Force Room to open, run all migrations, and validate the final schema.
            assertNotNull(room.openHelper.writableDatabase)

            val ayah = room.ayahDao().getById(1)
            assertNotNull(ayah)
            assertEquals(SOURCE_UTHMANI, ayah?.textUthmani)
            assertEquals(ArabicNormalizer.normalize(SOURCE_UTHMANI), ayah?.textSimple)

            val results = room.ayahDao().search(
                matchQuery = ArabicNormalizer.toFtsPrefixQuery("الله"),
                limit = 10,
            )
            assertEquals(listOf(1), results.map { it.ayah.id })

            // The nullable column is introduced by 3→4 and validated by Room on open.
            assertNull(room.tafsirDao().getByAyahId(1))
        } finally {
            room.close()
        }
    }

    private fun createVersion1Database() {
        databaseFile.parentFile?.mkdirs()
        val database = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null)
        try {
            database.execSQL("PRAGMA foreign_keys = ON")
            database.execSQL(
                """
                CREATE TABLE surahs (
                    number INTEGER NOT NULL PRIMARY KEY,
                    name_arabic TEXT NOT NULL,
                    name_transliterated TEXT,
                    name_translated TEXT,
                    revelation_type TEXT NOT NULL,
                    ayah_count INTEGER NOT NULL,
                    start_page INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE ayahs (
                    id INTEGER NOT NULL PRIMARY KEY,
                    surah_number INTEGER NOT NULL,
                    number_in_surah INTEGER NOT NULL,
                    text_uthmani TEXT NOT NULL,
                    juz_number INTEGER NOT NULL,
                    hizb_quarter INTEGER NOT NULL,
                    page_number INTEGER NOT NULL,
                    FOREIGN KEY(surah_number) REFERENCES surahs(number)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO surahs (
                    number, name_arabic, name_transliterated, name_translated,
                    revelation_type, ayah_count, start_page
                ) VALUES (1, 'الفاتحة', 'Al-Fatihah', 'The Opening', 'MECCAN', 7, 1)
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO ayahs (
                    id, surah_number, number_in_surah, text_uthmani,
                    juz_number, hizb_quarter, page_number
                ) VALUES (1, 1, 1, ?, 1, 1, 1)
                """.trimIndent(),
                arrayOf(SOURCE_UTHMANI),
            )
            database.version = 1
        } finally {
            database.close()
        }
    }

    private companion object {
        const val DATABASE_NAME = "noor-migration-test.db"
        const val SOURCE_UTHMANI = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
    }
}
