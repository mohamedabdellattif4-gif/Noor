package com.noor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey
    @ColumnInfo(name = "number")
    val number: Int,
    @ColumnInfo(name = "name_arabic")
    val nameArabic: String,
    @ColumnInfo(name = "name_transliterated")
    val nameTransliterated: String?,
    @ColumnInfo(name = "name_translated")
    val nameTranslated: String?,
    @ColumnInfo(name = "revelation_type")
    val revelationType: String,
    @ColumnInfo(name = "ayah_count")
    val ayahCount: Int,
    @ColumnInfo(name = "start_page")
    val startPage: Int,
)
