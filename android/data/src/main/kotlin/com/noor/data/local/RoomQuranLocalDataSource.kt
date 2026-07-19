package com.noor.data.local

import androidx.room.withTransaction
import com.noor.data.local.dao.AyahDao
import com.noor.data.local.dao.SurahDao
import com.noor.data.local.database.NoorDatabase
import com.noor.data.local.entity.AyahEntity
import com.noor.data.local.entity.SurahEntity
import com.noor.data.local.model.AyahWithSurahName
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class RoomQuranLocalDataSource @Inject constructor(
    private val database: NoorDatabase,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
) : QuranLocalDataSource {
    override fun observeSurahs(): Flow<List<SurahEntity>> = surahDao.observeAll()
    override fun observeSurah(number: Int): Flow<SurahEntity?> = surahDao.observeByNumber(number)
    override fun observeAyahsForSurah(surahNumber: Int): Flow<List<AyahEntity>> =
        ayahDao.observeForSurah(surahNumber)
    override fun observeAyahsForPage(pageNumber: Int): Flow<List<AyahEntity>> =
        ayahDao.observeForPage(pageNumber)
    override fun observeAyahsForJuz(juzNumber: Int): Flow<List<AyahEntity>> =
        ayahDao.observeForJuz(juzNumber)
    override suspend fun getAyah(id: Int): AyahEntity? = ayahDao.getById(id)
    override suspend fun searchAyahs(matchQuery: String, limit: Int): List<AyahWithSurahName> =
        ayahDao.search(matchQuery, limit)

    override suspend fun isPopulated(): Boolean = database.withTransaction {
        val expectedAyahCount = surahDao.totalAyahCount() ?: return@withTransaction false
        surahDao.count() == 114 && expectedAyahCount == 6236 && ayahDao.count() == expectedAyahCount
    }

    override suspend fun replaceCorpus(surahs: List<SurahEntity>, ayahs: List<AyahEntity>) {
        database.withTransaction {
            // Canonical IDs are stable. Updating rows in place preserves bookmarks and tafsir cache.
            surahDao.upsertAll(surahs)
            ayahDao.upsertAll(ayahs)
        }
    }
}
