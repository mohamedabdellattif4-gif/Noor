package com.noor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.noor.data.local.entity.TafsirEntity

@Dao
interface TafsirDao {
    @Query("SELECT * FROM tafsirs WHERE ayah_id = :ayahId LIMIT 1")
    suspend fun getByAyahId(ayahId: Int): TafsirEntity?

    @Upsert suspend fun upsert(tafsir: TafsirEntity)

    @Query("DELETE FROM tafsirs WHERE fetched_at < :epochMillis")
    suspend fun deleteOlderThan(epochMillis: Long): Int
}
