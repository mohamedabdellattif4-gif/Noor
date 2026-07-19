package com.noor.app.ui.reader

/**
 * Returns whether the reader should render the non-numbered opening basmala above a surah.
 *
 * Al-Fatihah stores the basmala as ayah 1 in Noor's verified corpus, while At-Tawbah has no
 * opening basmala. All other surahs use the conventional non-numbered opening presentation.
 */
internal fun shouldShowReaderBasmala(surahNumber: Int): Boolean {
    require(surahNumber in 1..114)
    return surahNumber != 1 && surahNumber != 9
}
