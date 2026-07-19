package com.noor.domain.model

/** Identifies whether the displayed tafsir came from Room or a new network response. */
enum class TafsirOrigin {
    CACHE,
    NETWORK,
}

/** Immutable tafsir content associated with one canonical Quran ayah. */
data class Tafsir(
    val ayahId: Int,
    val text: String,
    val footnotes: String? = null,
    val sourceKey: String,
    val sourceName: String,
    val fetchedAtEpochMillis: Long,
    val origin: TafsirOrigin,
) {
    init {
        require(ayahId > 0) { "Tafsir ayah id must be positive." }
        require(text.isNotBlank()) { "Tafsir text must not be blank." }
        require(footnotes == null || footnotes.isNotBlank()) {
            "Tafsir footnotes must be null or non-blank."
        }
        require(sourceKey.isNotBlank()) { "Tafsir source key must not be blank." }
        require(sourceName.isNotBlank()) { "Tafsir source name must not be blank." }
        require(fetchedAtEpochMillis >= 0L) { "Tafsir fetch time must not be negative." }
    }
}
