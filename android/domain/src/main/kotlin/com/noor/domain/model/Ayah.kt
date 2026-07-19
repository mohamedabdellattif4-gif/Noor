package com.noor.domain.model

/** Immutable Quran verse and navigation metadata. */
data class Ayah(
    val id: Int,
    val surahNumber: Int,
    val numberInSurah: Int,
    val textUthmani: String,
    val juzNumber: Int,
    val hizbQuarter: Int,
    val pageNumber: Int,
) {
    init {
        require(id > 0) { "Ayah id must be positive." }
        require(surahNumber in Surah.MIN_NUMBER..Surah.MAX_NUMBER) {
            "Ayah surah number must be between 1 and 114."
        }
        require(numberInSurah > 0) { "Ayah number within its surah must be positive." }
        require(textUthmani.isNotBlank()) { "Uthmani ayah text must not be blank." }
        require(juzNumber in MIN_JUZ..MAX_JUZ) { "Juz number must be between 1 and 30." }
        require(hizbQuarter in MIN_HIZB_QUARTER..MAX_HIZB_QUARTER) {
            "Hizb quarter must be between 1 and 240."
        }
        require(pageNumber > 0) { "Page number must be positive." }
    }

    companion object {
        const val MIN_JUZ: Int = 1
        const val MAX_JUZ: Int = 30
        const val MIN_HIZB_QUARTER: Int = 1
        const val MAX_HIZB_QUARTER: Int = 240
    }
}
