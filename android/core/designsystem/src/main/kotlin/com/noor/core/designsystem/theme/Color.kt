package com.noor.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Noor's brand palette.
 *
 * Emerald represents calm and growth, while the restrained gold accent provides
 * hierarchy without reducing long-form reading comfort.
 */
internal object NoorPalette {
    val Emerald10 = Color(0xFF002116)
    val Emerald20 = Color(0xFF003827)
    val Emerald30 = Color(0xFF00513A)
    val Emerald40 = Color(0xFF006C4C)
    val Emerald80 = Color(0xFF6FDBA9)
    val Emerald90 = Color(0xFF89F8C5)

    val Sage10 = Color(0xFF0B1F17)
    val Sage20 = Color(0xFF20352B)
    val Sage30 = Color(0xFF374B40)
    val Sage40 = Color(0xFF4E6358)
    val Sage80 = Color(0xFFB5CCBD)
    val Sage90 = Color(0xFFD1E8D9)

    val Gold10 = Color(0xFF201C00)
    val Gold20 = Color(0xFF383000)
    val Gold30 = Color(0xFF514700)
    val Gold40 = Color(0xFF6B5E00)
    val Gold80 = Color(0xFFDAC74C)
    val Gold90 = Color(0xFFF7E45C)

    val Error10 = Color(0xFF410002)
    val Error20 = Color(0xFF690005)
    val Error30 = Color(0xFF93000A)
    val Error40 = Color(0xFFBA1A1A)
    val Error80 = Color(0xFFFFB4AB)
    val Error90 = Color(0xFFFFDAD6)

    val Neutral4 = Color(0xFF101412)
    val Neutral6 = Color(0xFF151916)
    val Neutral10 = Color(0xFF191C1A)
    val Neutral12 = Color(0xFF1D201E)
    val Neutral17 = Color(0xFF282B29)
    val Neutral20 = Color(0xFF2E312F)
    val Neutral22 = Color(0xFF333634)
    val Neutral24 = Color(0xFF373A38)
    val Neutral87 = Color(0xFFD9DCD8)
    val Neutral90 = Color(0xFFE1E3DF)
    val Neutral92 = Color(0xFFE7E9E5)
    val Neutral94 = Color(0xFFECEEEA)
    val Neutral95 = Color(0xFFEFF1ED)
    val Neutral96 = Color(0xFFF2F4F0)
    val Neutral98 = Color(0xFFF8FAF6)
    val Neutral99 = Color(0xFFFBFDF9)

    val NeutralVariant30 = Color(0xFF404943)
    val NeutralVariant50 = Color(0xFF707973)
    val NeutralVariant60 = Color(0xFF89938C)
    val NeutralVariant80 = Color(0xFFBFC9C1)
    val NeutralVariant90 = Color(0xFFDBE5DE)
}

internal val NoorLightColorScheme = lightColorScheme(
    primary = NoorPalette.Emerald40,
    onPrimary = Color.White,
    primaryContainer = NoorPalette.Emerald90,
    onPrimaryContainer = NoorPalette.Emerald10,
    secondary = NoorPalette.Sage40,
    onSecondary = Color.White,
    secondaryContainer = NoorPalette.Sage90,
    onSecondaryContainer = NoorPalette.Sage10,
    tertiary = NoorPalette.Gold40,
    onTertiary = Color.White,
    tertiaryContainer = NoorPalette.Gold90,
    onTertiaryContainer = NoorPalette.Gold10,
    error = NoorPalette.Error40,
    onError = Color.White,
    errorContainer = NoorPalette.Error90,
    onErrorContainer = NoorPalette.Error10,
    background = NoorPalette.Neutral99,
    onBackground = NoorPalette.Neutral10,
    surface = NoorPalette.Neutral99,
    onSurface = NoorPalette.Neutral10,
    surfaceVariant = NoorPalette.NeutralVariant90,
    onSurfaceVariant = NoorPalette.NeutralVariant30,
    outline = NoorPalette.NeutralVariant50,
    outlineVariant = NoorPalette.NeutralVariant80,
    scrim = Color.Black,
    inverseSurface = NoorPalette.Neutral20,
    inverseOnSurface = NoorPalette.Neutral95,
    inversePrimary = NoorPalette.Emerald80,
    surfaceDim = NoorPalette.Neutral87,
    surfaceBright = NoorPalette.Neutral99,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = NoorPalette.Neutral98,
    surfaceContainer = NoorPalette.Neutral96,
    surfaceContainerHigh = NoorPalette.Neutral94,
    surfaceContainerHighest = NoorPalette.Neutral92,
)

internal val NoorDarkColorScheme = darkColorScheme(
    primary = NoorPalette.Emerald80,
    onPrimary = NoorPalette.Emerald20,
    primaryContainer = NoorPalette.Emerald30,
    onPrimaryContainer = NoorPalette.Emerald90,
    secondary = NoorPalette.Sage80,
    onSecondary = NoorPalette.Sage20,
    secondaryContainer = NoorPalette.Sage30,
    onSecondaryContainer = NoorPalette.Sage90,
    tertiary = NoorPalette.Gold80,
    onTertiary = NoorPalette.Gold20,
    tertiaryContainer = NoorPalette.Gold30,
    onTertiaryContainer = NoorPalette.Gold90,
    error = NoorPalette.Error80,
    onError = NoorPalette.Error20,
    errorContainer = NoorPalette.Error30,
    onErrorContainer = NoorPalette.Error90,
    background = NoorPalette.Neutral4,
    onBackground = NoorPalette.Neutral90,
    surface = NoorPalette.Neutral4,
    onSurface = NoorPalette.Neutral90,
    surfaceVariant = NoorPalette.NeutralVariant30,
    onSurfaceVariant = NoorPalette.NeutralVariant80,
    outline = NoorPalette.NeutralVariant60,
    outlineVariant = NoorPalette.NeutralVariant30,
    scrim = Color.Black,
    inverseSurface = NoorPalette.Neutral90,
    inverseOnSurface = NoorPalette.Neutral20,
    inversePrimary = NoorPalette.Emerald40,
    surfaceDim = NoorPalette.Neutral4,
    surfaceBright = NoorPalette.Neutral24,
    surfaceContainerLowest = Color(0xFF0B0F0C),
    surfaceContainerLow = NoorPalette.Neutral10,
    surfaceContainer = NoorPalette.Neutral12,
    surfaceContainerHigh = NoorPalette.Neutral17,
    surfaceContainerHighest = NoorPalette.Neutral22,
)
