package com.noor.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.AppLaunchNavigation
import com.noor.app.navigation.NoorNavHost
import com.noor.core.designsystem.theme.NoorTheme
import com.noor.core.navigation.NoorRoutes
import com.noor.core.navigation.rememberNoorNavController
import com.noor.domain.model.ThemeMode

@Composable
fun NoorApp(
    startDestination: String = NoorRoutes.HOME,
    openAdhkarRequestId: Long = AppLaunchNavigation.NO_REQUEST,
    viewModel: AppViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    NoorTheme(
        darkTheme = darkTheme,
        useDynamicColor = settings.dynamicColorEnabled,
    ) {
        val navController = rememberNoorNavController()
        LaunchedEffect(openAdhkarRequestId) {
            if (openAdhkarRequestId != AppLaunchNavigation.NO_REQUEST) {
                navController.navigate(NoorRoutes.ADHKAR) {
                    launchSingleTop = true
                }
            }
        }
        NoorNavHost(
            navController = navController,
            startDestination = startDestination,
        )
    }
}
