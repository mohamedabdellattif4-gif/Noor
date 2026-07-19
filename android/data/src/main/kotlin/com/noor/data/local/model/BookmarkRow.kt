package com.noor.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.noor.data.local.entity.AyahEntity

data class BookmarkRow(
    @Embedded val ayah: AyahEntity,
    @ColumnInfo(name = "surah_name_arabic") val surahNameArabic: String,
    @ColumnInfo(name = "bookmark_created_at") val createdAtEpochMillis: Long,
)
