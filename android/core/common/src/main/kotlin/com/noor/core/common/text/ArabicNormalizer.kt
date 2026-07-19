package com.noor.core.common.text

import java.text.Normalizer

/** Normalizes Arabic only for search/indexing; Quran display text must remain verbatim. */
object ArabicNormalizer {
    private val marks = Regex("\\p{M}+")
    private val whitespace = Regex("\\s+")
    private val punctuation = Regex("[^\\p{L}\\p{N}\\s]")

    fun normalize(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFKD)
        .replace(marks, "")
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
        .replace('ٱ', 'ا')
        .replace('ى', 'ي')
        .replace('ؤ', 'و')
        .replace('ئ', 'ي')
        .replace('ة', 'ه')
        .replace('ـ', ' ')
        .replace(punctuation, " ")
        .replace(whitespace, " ")
        .trim()

    /** Builds a safe FTS4 prefix query from already normalized letters and digits. */
    fun toFtsPrefixQuery(value: String): String = normalize(value)
        .take(MAX_QUERY_LENGTH)
        .split(' ')
        .asSequence()
        .filter(String::isNotBlank)
        .take(MAX_QUERY_TOKENS)
        .map { it.take(MAX_TOKEN_LENGTH) }
        .joinToString(" AND ") { token -> "\"$token*\"" }

    const val MAX_QUERY_LENGTH: Int = 200
    const val MAX_QUERY_TOKENS: Int = 12
    const val MAX_TOKEN_LENGTH: Int = 32
}
