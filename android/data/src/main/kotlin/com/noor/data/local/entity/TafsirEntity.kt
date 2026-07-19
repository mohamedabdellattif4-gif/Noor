package com.noor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tafsirs",
    foreignKeys = [
        ForeignKey(
            entity = AyahEntity::class,
            parentColumns = ["id"],
            childColumns = ["ayah_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["fetched_at"])],
)
data class TafsirEntity(
    @PrimaryKey @ColumnInfo(name = "ayah_id") val ayahId: Int,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "footnotes") val footnotes: String?,
    @ColumnInfo(name = "source_key") val sourceKey: String,
    @ColumnInfo(name = "source_name") val sourceName: String,
    @ColumnInfo(name = "fetched_at") val fetchedAtEpochMillis: Long,
)
