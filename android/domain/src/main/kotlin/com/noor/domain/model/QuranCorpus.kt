package com.noor.domain.model

/**
 * Validated aggregate used for an atomic Quran corpus import.
 *
 * Defensive copies prevent callers from mutating the validated collections after construction.
 * The aggregate rejects partial imports, duplicate identifiers, orphan ayahs, and metadata counts
 * that disagree with the supplied verse list before the data layer starts a database transaction.
 */
class QuranCorpus(
    surahs: List<Surah>,
    ayahs: List<Ayah>,
) {
    val surahs: List<Surah> = surahs.toList()
    val ayahs: List<Ayah> = ayahs.toList()

    init {
        require(this.surahs.size == Surah.EXPECTED_COUNT) {
            "Quran corpus must contain all 114 surahs."
        }
        require(this.ayahs.size == EXPECTED_AYAH_COUNT) {
            "Quran corpus must contain all 6,236 ayahs."
        }
        val expectedSurahNumbers = (Surah.MIN_NUMBER..Surah.MAX_NUMBER).toSet()
        require(this.surahs.map(Surah::number).toSet() == expectedSurahNumbers) {
            "Quran corpus must contain each surah number exactly once."
        }
        require(this.ayahs.map(Ayah::id).toSet() == (1..EXPECTED_AYAH_COUNT).toSet()) {
            "Quran corpus must contain each canonical global ayah id exactly once."
        }
        require(
            this.ayahs.map { it.surahNumber to it.numberInSurah }.toSet().size == this.ayahs.size,
        ) {
            "Quran corpus contains duplicate ayah positions."
        }

        val surahNumbers = this.surahs.map(Surah::number).toSet()
        require(this.ayahs.all { it.surahNumber in surahNumbers }) {
            "Quran corpus contains ayahs referencing missing surahs."
        }

        val actualAyahCounts = this.ayahs.groupingBy(Ayah::surahNumber).eachCount()
        require(this.surahs.all { actualAyahCounts[it.number] == it.ayahCount }) {
            "One or more surah ayah counts do not match the supplied ayahs."
        }
    }

    companion object {
        const val EXPECTED_AYAH_COUNT: Int = 6236
    }
}
