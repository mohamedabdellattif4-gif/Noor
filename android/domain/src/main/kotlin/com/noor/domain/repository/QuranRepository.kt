package com.noor.domain.repository

import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult
import com.noor.domain.model.QuranCorpus
import com.noor.domain.model.Surah
import kotlinx.coroutines.flow.Flow

/** Public data contract consumed by Quran features without exposing Room implementation details. */
interface QuranRepository {
    fun observeSurahs(): Flow<List<Surah>>
    fun observeSurah(number: Int): Flow<Surah?>
    fun observeAyahsForSurah(surahNumber: Int): Flow<List<Ayah>>
    fun observeAyahsForPage(pageNumber: Int): Flow<List<Ayah>>
    fun observeAyahsForJuz(juzNumber: Int): Flow<List<Ayah>>
    suspend fun getAyah(id: Int): Ayah?
    suspend fun searchAyahs(normalizedQuery: String, limit: Int = 100): List<AyahSearchResult>
    suspend fun isPopulated(): Boolean
    suspend fun replaceCorpus(corpus: QuranCorpus)
}
