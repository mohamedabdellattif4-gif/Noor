package com.noor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ayahs",
    foreignKeys = [
        ForeignKey(
            entity = SurahEntity::class,
            parentColumns = ["number"],
            childColumns = ["surah_number"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["surah_number"]),
        Index(value = ["surah_number", "number_in_surah"], unique = true),
        Index(value = ["page_number"]),
        Index(value = ["juz_number"]),
    ],
)
data class AyahEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "surah_number") val surahNumber: Int,
    @ColumnInfo(name = "number_in_surah") val numberInSurah: Int,
    @ColumnInfo(name = "text_uthmani") val textUthmani: String,
    @ColumnInfo(name = "text_simple") val textSimple: String,
    @ColumnInfo(name = "juz_number") val juzNumber: Int,
    @ColumnInfo(name = "hizb_quarter") val hizbQuarter: Int,
    @ColumnInfo(name = "page_number") val pageNumber: Int,
)
