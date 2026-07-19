package com.noor.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.noor.data.local.RoomQuranLocalDataSource
import com.noor.data.local.entity.AyahEntity
import com.noor.data.local.entity.SurahEntity
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoorDatabaseTest {
    private lateinit var database: NoorDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NoorDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun ayahsAreOrderedWithinTheirSurah() = runBlocking {
        database.surahDao().upsertAll(listOf(testSurah()))
        database.ayahDao().upsertAll(
            listOf(
                testAyah(id = 2, numberInSurah = 2),
                testAyah(id = 1, numberInSurah = 1),
            ),
        )

        val ayahs = database.ayahDao().observeForSurah(surahNumber = 1).first()

        assertEquals(listOf(1, 2), ayahs.map(AyahEntity::numberInSurah))
    }

    @Test
    fun searchUsesNormalizedFtsPrefixQuery() = runBlocking {
        database.surahDao().upsertAll(listOf(testSurah()))
        database.ayahDao().upsertAll(
            listOf(testAyah(id = 1, numberInSurah = 1, textSimple = "الرحمن الرحيم")),
        )

        val results = database.ayahDao().search("\"الرحمن*\" AND \"الرحيم*\"", 10)

        assertEquals(listOf(1), results.map { it.ayah.id })
    }

    @Test
    fun prepackagedDatabaseContainsCompleteQuran() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "noor-prepackaged-test.db"
        context.deleteDatabase(name)
        val packaged = Room.databaseBuilder(context, NoorDatabase::class.java, name)
            .createFromAsset(NoorDatabase.ASSET_PATH)
            .build()
        try {
            assertEquals(114, packaged.surahDao().count())
            assertEquals(6236, packaged.ayahDao().count())
        } finally {
            packaged.close()
            context.deleteDatabase(name)
        }
    }


    @Test
    fun tafsirCachePreservesSourceFootnotes() = runBlocking {
        database.surahDao().upsertAll(listOf(testSurah()))
        database.ayahDao().upsertAll(listOf(testAyah(id = 1, numberInSurah = 1)))
        val expected = com.noor.data.local.entity.TafsirEntity(
            ayahId = 1,
            text = "تفسير محفوظ",
            footnotes = "حاشية المصدر",
            sourceKey = "arabic_moyassar",
            sourceName = "التفسير الميسر — QuranEnc.com",
            fetchedAtEpochMillis = 1L,
        )

        database.tafsirDao().upsert(expected)

        assertEquals(expected, database.tafsirDao().getByAyahId(1))
    }

    @Test
    fun corpusRefreshPreservesBookmarks() = runBlocking {
        val localDataSource = RoomQuranLocalDataSource(
            database = database,
            surahDao = database.surahDao(),
            ayahDao = database.ayahDao(),
        )
        localDataSource.replaceCorpus(
            surahs = listOf(testSurah()),
            ayahs = listOf(testAyah(id = 1, numberInSurah = 1)),
        )
        database.bookmarkDao().upsert(
            com.noor.data.local.entity.BookmarkEntity(ayahId = 1, createdAtEpochMillis = 1L),
        )

        localDataSource.replaceCorpus(
            surahs = listOf(testSurah()),
            ayahs = listOf(testAyah(id = 1, numberInSurah = 1, textSimple = "نص محدث")),
        )

        assertEquals(listOf(1), database.bookmarkDao().observeAyahIds().first())
    }

    @Test
    fun failedCorpusReplacementRollsBackPreviousData() = runBlocking {
        val localDataSource = RoomQuranLocalDataSource(
            database = database,
            surahDao = database.surahDao(),
            ayahDao = database.ayahDao(),
        )
        localDataSource.replaceCorpus(
            surahs = listOf(testSurah()),
            ayahs = listOf(testAyah(id = 1, numberInSurah = 1)),
        )

        val replacementFailed = runCatching {
            localDataSource.replaceCorpus(
                surahs = listOf(testSurah(number = 2)),
                ayahs = listOf(testAyah(id = 2, surahNumber = 99, numberInSurah = 1)),
            )
        }.isFailure

        assertTrue(replacementFailed)
        assertEquals(1, database.surahDao().count())
        assertEquals(1, database.ayahDao().count())
        assertEquals(1, database.ayahDao().getById(id = 1)?.surahNumber)
    }

    private fun testSurah(number: Int = 1): SurahEntity = SurahEntity(
        number = number,
        nameArabic = "اسم تجريبي",
        nameTransliterated = "Test",
        nameTranslated = "Test",
        revelationType = "MECCAN",
        ayahCount = 1,
        startPage = 1,
    )

    private fun testAyah(
        id: Int,
        surahNumber: Int = 1,
        numberInSurah: Int,
        textSimple: String = "نص تجريبي",
    ): AyahEntity = AyahEntity(
        id = id,
        surahNumber = surahNumber,
        numberInSurah = numberInSurah,
        textUthmani = "نص تجريبي",
        textSimple = textSimple,
        juzNumber = 1,
        hizbQuarter = 1,
        pageNumber = 1,
    )
}
