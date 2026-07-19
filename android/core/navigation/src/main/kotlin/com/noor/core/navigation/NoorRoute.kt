package com.noor.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

object NoorRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val BOOKMARKS = "bookmarks"
    const val AUDIO = "audio"
    const val ADHKAR = "adhkar"
    const val TASBIH = "adhkar/tasbih"
    const val ADHKAR_REMINDERS = "adhkar/reminders"
    const val SETTINGS = "settings"
    const val LEGAL = "legal"

    const val READER = "reader/{surahNumber}?ayahId={ayahId}"
    const val TAFSIR = "tafsir/{ayahId}"

    fun reader(surahNumber: Int, ayahId: Int? = null): String =
        "reader/$surahNumber" + (ayahId?.let { "?ayahId=$it" } ?: "")

    fun tafsir(ayahId: Int): String = "tafsir/$ayahId"
}

@Composable
fun rememberNoorNavController(): NavHostController = rememberNavController()
