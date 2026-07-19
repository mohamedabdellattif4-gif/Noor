package com.noor.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

private val UiFontFamily = FontFamily.SansSerif
private val QuranFallbackFontFamily = FontFamily.Serif

/** Material 3 typography tuned for readable Arabic and Latin interface text. */
internal val NoorTypography = Typography(
    displayLarge = uiTextStyle(size = 57, lineHeight = 68),
    displayMedium = uiTextStyle(size = 45, lineHeight = 56),
    displaySmall = uiTextStyle(size = 36, lineHeight = 46),
    headlineLarge = uiTextStyle(size = 32, lineHeight = 42, weight = FontWeight.SemiBold),
    headlineMedium = uiTextStyle(size = 28, lineHeight = 38, weight = FontWeight.SemiBold),
    headlineSmall = uiTextStyle(size = 24, lineHeight = 34, weight = FontWeight.SemiBold),
    titleLarge = uiTextStyle(size = 22, lineHeight = 32, weight = FontWeight.SemiBold),
    titleMedium = uiTextStyle(size = 16, lineHeight = 26, weight = FontWeight.SemiBold),
    titleSmall = uiTextStyle(size = 14, lineHeight = 22, weight = FontWeight.SemiBold),
    bodyLarge = uiTextStyle(size = 18, lineHeight = 30),
    bodyMedium = uiTextStyle(size = 16, lineHeight = 26),
    bodySmall = uiTextStyle(size = 14, lineHeight = 22),
    labelLarge = uiTextStyle(size = 14, lineHeight = 20, weight = FontWeight.SemiBold),
    labelMedium = uiTextStyle(size = 12, lineHeight = 18, weight = FontWeight.SemiBold),
    labelSmall = uiTextStyle(size = 11, lineHeight = 16, weight = FontWeight.SemiBold),
)

/**
 * Noor-specific reading roles not covered by Material 3.
 *
 * The platform serif family is only a safe fallback. A verified Quran font remains a reader-feature
 * decision because its shaping, diacritics, license, and APK impact require dedicated validation.
 */
@Immutable
data class NoorExtendedTypography(
    val quranLarge: TextStyle = quranTextStyle(size = 32, lineHeight = 58),
    val quranMedium: TextStyle = quranTextStyle(size = 26, lineHeight = 48),
    val verseMetadata: TextStyle = uiTextStyle(
        size = 13,
        lineHeight = 20,
        weight = FontWeight.Medium,
        textDirection = TextDirection.ContentOrRtl,
    ),
)

internal val DefaultNoorExtendedTypography = NoorExtendedTypography()

private fun uiTextStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    textDirection: TextDirection = TextDirection.Content,
): TextStyle = TextStyle(
    fontFamily = UiFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
    textDirection = textDirection,
)

private fun quranTextStyle(
    size: Int,
    lineHeight: Int,
): TextStyle = TextStyle(
    fontFamily = QuranFallbackFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
    textDirection = TextDirection.ContentOrRtl,
)
