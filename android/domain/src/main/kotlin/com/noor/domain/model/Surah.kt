package com.noor.domain.model

/** Immutable, framework-independent Quran chapter metadata. */
data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameTransliterated: String?,
    val nameTranslated: String?,
    val revelationType: RevelationType,
    val ayahCount: Int,
    val startPage: Int,
) {
    init {
        require(number in MIN_NUMBER..MAX_NUMBER) { "Surah number must be between 1 and 114." }
        require(nameArabic.isNotBlank()) { "Arabic surah name must not be blank." }
        require(nameTransliterated == null || nameTransliterated.isNotBlank()) {
            "Transliterated surah name must be null or non-blank."
        }
        require(nameTranslated == null || nameTranslated.isNotBlank()) {
            "Translated surah name must be null or non-blank."
        }
        require(ayahCount > 0) { "Surah ayah count must be positive." }
        require(startPage > 0) { "Surah start page must be positive." }
    }

    companion object {
        const val MIN_NUMBER: Int = 1
        const val MAX_NUMBER: Int = 114
        const val EXPECTED_COUNT: Int = MAX_NUMBER
    }
}
