package com.noor.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.noor.app.ui.adhkar.AdhkarRemindersRoute
import com.noor.app.ui.adhkar.AdhkarRoute
import com.noor.app.ui.adhkar.TasbihRoute
import com.noor.app.ui.audio.AudioRoute
import com.noor.app.ui.bookmarks.BookmarksRoute
import com.noor.app.ui.home.HomeRoute
import com.noor.app.ui.legal.LegalRoute
import com.noor.app.ui.reader.ReaderRoute
import com.noor.app.ui.search.SearchRoute
import com.noor.app.ui.settings.SettingsRoute
import com.noor.app.ui.tafsir.TafsirRoute
import com.noor.core.navigation.NoorRoutes

@Composable
fun NoorNavHost(
    navController: NavHostController,
    startDestination: String = NoorRoutes.HOME,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(NoorRoutes.HOME) {
            HomeRoute(
                onOpenSurah = { surah, ayah -> navController.navigate(NoorRoutes.reader(surah, ayah)) },
                onOpenTafsir = { ayah ->
                    navController.navigate(
                        NoorRoutes.tafsir(ayah.id),
                    )
                },
                onSearch = { navController.navigate(NoorRoutes.SEARCH) },
                onBookmarks = { navController.navigate(NoorRoutes.BOOKMARKS) },
                onAudio = { navController.navigate(NoorRoutes.AUDIO) },
                onAdhkar = { navController.navigate(NoorRoutes.ADHKAR) },
                onSettings = { navController.navigate(NoorRoutes.SETTINGS) },
            )
        }
        composable(
            route = NoorRoutes.READER,
            arguments = listOf(
                navArgument("surahNumber") { type = NavType.IntType },
                navArgument("ayahId") { type = NavType.IntType; defaultValue = -1 },
            ),
        ) {
            ReaderRoute(
                onBack = navController::popBackStack,
                onOpenTafsir = { ayah ->
                    navController.navigate(
                        NoorRoutes.tafsir(ayah.id),
                    )
                },
            )
        }
        composable(NoorRoutes.SEARCH) {
            SearchRoute(
                onBack = navController::popBackStack,
                onOpenResult = { result ->
                    navController.navigate(NoorRoutes.reader(result.ayah.surahNumber, result.ayah.id))
                },
            )
        }
        composable(NoorRoutes.BOOKMARKS) {
            BookmarksRoute(
                onBack = navController::popBackStack,
                onOpenBookmark = { bookmark ->
                    navController.navigate(NoorRoutes.reader(bookmark.ayah.surahNumber, bookmark.ayah.id))
                },
            )
        }
        composable(NoorRoutes.AUDIO) {
            AudioRoute(
                onBack = navController::popBackStack,
                onOpenReader = { surahNumber, ayahId ->
                    navController.navigate(NoorRoutes.reader(surahNumber, ayahId))
                },
            )
        }
        composable(NoorRoutes.ADHKAR) {
            AdhkarRoute(
                onBack = navController::popBackStack,
                onOpenTasbih = { navController.navigate(NoorRoutes.TASBIH) },
                onOpenReminders = { navController.navigate(NoorRoutes.ADHKAR_REMINDERS) },
            )
        }
        composable(NoorRoutes.TASBIH) { TasbihRoute(onBack = navController::popBackStack) }
        composable(NoorRoutes.ADHKAR_REMINDERS) {
            AdhkarRemindersRoute(onBack = navController::popBackStack)
        }
        composable(NoorRoutes.SETTINGS) {
            SettingsRoute(
                onBack = navController::popBackStack,
                onOpenLegal = { navController.navigate(NoorRoutes.LEGAL) },
            )
        }
        composable(NoorRoutes.LEGAL) { LegalRoute(onBack = navController::popBackStack) }
        composable(
            route = NoorRoutes.TAFSIR,
            arguments = listOf(
                navArgument("ayahId") { type = NavType.IntType },
            ),
        ) { TafsirRoute(onBack = navController::popBackStack) }
    }
}
