package com.noor.domain.repository

import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir

/** Offline-first tafsir boundary keyed by Noor's canonical [Ayah]. */
interface TafsirRepository {
    /**
     * Returns cached content when available. When [forceRefresh] is true, a fresh response is
     * requested and replaces the cache only after complete validation.
     */
    suspend fun getTafsir(ayah: Ayah, forceRefresh: Boolean = false): Tafsir
}
