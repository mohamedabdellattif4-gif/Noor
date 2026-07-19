package com.noor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.noor.data.local.entity.AyahEntity
import com.noor.data.local.model.AyahWithSurahName
import kotlinx.coroutines.flow.Flow

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE surah_number = :surahNumber ORDER BY number_in_surah ASC")
    fun observeForSurah(surahNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE page_number = :pageNumber ORDER BY id ASC")
    fun observeForPage(pageNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE juz_number = :juzNumber ORDER BY id ASC")
    fun observeForJuz(juzNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): AyahEntity?

    @Query(
        """
        SELECT a.*, s.name_arabic AS surah_name_arabic
        FROM ayahs_fts
        INNER JOIN ayahs AS a ON a.id = ayahs_fts.rowid
        INNER JOIN surahs AS s ON s.number = a.surah_number
        WHERE ayahs_fts MATCH :matchQuery
        ORDER BY a.id ASC
        LIMIT :limit
        """,
    )
    suspend fun search(matchQuery: String, limit: Int): List<AyahWithSurahName>


    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun count(): Int

    @Upsert suspend fun upsertAll(ayahs: List<AyahEntity>)
}
