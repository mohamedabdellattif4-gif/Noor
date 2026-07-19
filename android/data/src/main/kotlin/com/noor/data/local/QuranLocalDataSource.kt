package com.noor.data.local

import com.noor.data.local.entity.AyahEntity
import com.noor.data.local.entity.SurahEntity
import com.noor.data.local.model.AyahWithSurahName
import kotlinx.coroutines.flow.Flow

internal interface QuranLocalDataSource {
    fun observeSurahs(): Flow<List<SurahEntity>>
    fun observeSurah(number: Int): Flow<SurahEntity?>
    fun observeAyahsForSurah(surahNumber: Int): Flow<List<AyahEntity>>
    fun observeAyahsForPage(pageNumber: Int): Flow<List<AyahEntity>>
    fun observeAyahsForJuz(juzNumber: Int): Flow<List<AyahEntity>>
    suspend fun getAyah(id: Int): AyahEntity?
    suspend fun searchAyahs(matchQuery: String, limit: Int): List<AyahWithSurahName>
    suspend fun isPopulated(): Boolean
    suspend fun replaceCorpus(surahs: List<SurahEntity>, ayahs: List<AyahEntity>)
}
