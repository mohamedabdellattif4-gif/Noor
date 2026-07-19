package com.noor.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val LocalNoorExtendedTypography = staticCompositionLocalOf {
    DefaultNoorExtendedTypography
}

/**
 * Root theme for Noor UI.
 *
 * Dynamic color is opt-in so the Quran-reading identity remains consistent by default.
 */
@Composable
fun NoorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> NoorDarkColorScheme
        else -> NoorLightColorScheme
    }

    CompositionLocalProvider(
        LocalNoorSpacing provides DefaultNoorSpacing,
        LocalNoorExtendedTypography provides DefaultNoorExtendedTypography,
        LocalNoorPremiumColors provides if (darkTheme) NoorDarkPremiumColors else NoorLightPremiumColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NoorTypography,
            shapes = NoorShapes,
            content = content,
        )
    }
}

/** Noor-specific design tokens that are not part of [MaterialTheme]. */
object NoorDesignSystem {
    val colors: NoorPremiumColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNoorPremiumColors.current

    val spacing: NoorSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalNoorSpacing.current

    val extendedTypography: NoorExtendedTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalNoorExtendedTypography.current
}
