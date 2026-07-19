package com.noor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.noor.data.local.entity.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun observeAll(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs WHERE number = :number LIMIT 1")
    fun observeByNumber(number: Int): Flow<SurahEntity?>

    @Query("SELECT COUNT(*) FROM surahs") suspend fun count(): Int
    @Query("SELECT SUM(ayah_count) FROM surahs") suspend fun totalAyahCount(): Int?
    @Upsert suspend fun upsertAll(surahs: List<SurahEntity>)
}
