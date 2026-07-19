package com.noor.data.repository

import com.noor.core.common.text.ArabicNormalizer
import com.noor.data.local.QuranLocalDataSource
import com.noor.data.mapper.asDomain
import com.noor.data.mapper.asEntity
import com.noor.domain.model.Ayah
import com.noor.domain.model.AyahSearchResult
import com.noor.domain.model.QuranCorpus
import com.noor.domain.model.Surah
import com.noor.domain.repository.QuranRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DefaultQuranRepository @Inject constructor(
    private val localDataSource: QuranLocalDataSource,
) : QuranRepository {
    override fun observeSurahs(): Flow<List<Surah>> =
        localDataSource.observeSurahs().map { entities -> entities.map { it.asDomain() } }

    override fun observeSurah(number: Int): Flow<Surah?> =
        localDataSource.observeSurah(number).map { it?.asDomain() }

    override fun observeAyahsForSurah(surahNumber: Int): Flow<List<Ayah>> =
        localDataSource.observeAyahsForSurah(surahNumber).map { list -> list.map { it.asDomain() } }

    override fun observeAyahsForPage(pageNumber: Int): Flow<List<Ayah>> =
        localDataSource.observeAyahsForPage(pageNumber).map { list -> list.map { it.asDomain() } }

    override fun observeAyahsForJuz(juzNumber: Int): Flow<List<Ayah>> =
        localDataSource.observeAyahsForJuz(juzNumber).map { list -> list.map { it.asDomain() } }

    override suspend fun getAyah(id: Int): Ayah? = localDataSource.getAyah(id)?.asDomain()

    override suspend fun searchAyahs(normalizedQuery: String, limit: Int): List<AyahSearchResult> {
        val safeLimit = limit.coerceIn(1, 200)
        val matchQuery = ArabicNormalizer.toFtsPrefixQuery(normalizedQuery)
        if (matchQuery.length < 3) return emptyList()
        return localDataSource.searchAyahs(matchQuery, safeLimit).map { row ->
            AyahSearchResult(row.ayah.asDomain(), row.surahNameArabic)
        }
    }

    override suspend fun isPopulated(): Boolean = localDataSource.isPopulated()

    override suspend fun replaceCorpus(corpus: QuranCorpus) {
        localDataSource.replaceCorpus(
            surahs = corpus.surahs.map { it.asEntity() },
            ayahs = corpus.ayahs.map { it.asEntity() },
        )
    }
}
