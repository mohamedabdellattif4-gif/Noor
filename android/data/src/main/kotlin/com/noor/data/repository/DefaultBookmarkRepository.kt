package com.noor.data.repository

import com.noor.data.local.dao.BookmarkDao
import com.noor.data.local.entity.BookmarkEntity
import com.noor.data.mapper.asDomain
import com.noor.domain.model.Bookmark
import com.noor.domain.repository.BookmarkRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DefaultBookmarkRepository @Inject constructor(
    private val dao: BookmarkDao,
) : BookmarkRepository {
    override fun observeBookmarks(): Flow<List<Bookmark>> = dao.observeAll().map { rows ->
        rows.map { row ->
            Bookmark(
                ayah = row.ayah.asDomain(),
                surahNameArabic = row.surahNameArabic,
                createdAtEpochMillis = row.createdAtEpochMillis,
            )
        }
    }

    override fun observeBookmarkedAyahIds(): Flow<Set<Int>> =
        dao.observeAyahIds().map { it.toSet() }

    override suspend fun setBookmarked(ayahId: Int, bookmarked: Boolean) {
        require(ayahId > 0)
        if (bookmarked) {
            dao.upsert(BookmarkEntity(ayahId, System.currentTimeMillis()))
        } else {
            dao.delete(ayahId)
        }
    }
}
