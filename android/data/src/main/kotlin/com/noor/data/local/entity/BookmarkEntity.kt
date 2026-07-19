package com.noor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = AyahEntity::class,
            parentColumns = ["id"],
            childColumns = ["ayah_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["created_at"])],
)
data class BookmarkEntity(
    @PrimaryKey @ColumnInfo(name = "ayah_id") val ayahId: Int,
    @ColumnInfo(name = "created_at") val createdAtEpochMillis: Long,
)
