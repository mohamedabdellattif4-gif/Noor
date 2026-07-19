package com.noor.domain.model

/** A locally persisted bookmark enriched with Quran display data. */
data class Bookmark(
    val ayah: Ayah,
    val surahNameArabic: String,
    val createdAtEpochMillis: Long,
)
