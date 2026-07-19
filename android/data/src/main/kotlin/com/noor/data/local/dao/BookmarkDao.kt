package com.noor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.noor.data.local.entity.BookmarkEntity
import com.noor.data.local.model.BookmarkRow
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query(
        """
        SELECT a.*, s.name_arabic AS surah_name_arabic, b.created_at AS bookmark_created_at
        FROM bookmarks AS b
        INNER JOIN ayahs AS a ON a.id = b.ayah_id
        INNER JOIN surahs AS s ON s.number = a.surah_number
        ORDER BY b.created_at DESC
        """,
    )
    fun observeAll(): Flow<List<BookmarkRow>>

    @Query("SELECT ayah_id FROM bookmarks")
    fun observeAyahIds(): Flow<List<Int>>

    @Upsert suspend fun upsert(bookmark: BookmarkEntity)
    @Query("DELETE FROM bookmarks WHERE ayah_id = :ayahId") suspend fun delete(ayahId: Int)
}
