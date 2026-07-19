package com.noor.domain.repository

import com.noor.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun observeBookmarks(): Flow<List<Bookmark>>
    fun observeBookmarkedAyahIds(): Flow<Set<Int>>
    suspend fun setBookmarked(ayahId: Int, bookmarked: Boolean)
}
