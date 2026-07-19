package com.noor.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Brand colors used by Noor's premium Islamic surfaces.
 *
 * These colors intentionally remain stable when dynamic color is enabled so branded Quran screens
 * preserve their spiritual identity, contrast, and visual hierarchy.
 */
@Immutable
data class NoorPremiumColors(
    val emeraldDeep: Color,
    val emerald: Color,
    val emeraldElevated: Color,
    val emeraldMuted: Color,
    val ivory: Color,
    val ivoryElevated: Color,
    val mutedGold: Color,
    val goldHighlight: Color,
    val onEmerald: Color,
    val onIvory: Color,
    val onIvoryMuted: Color,
)

internal val NoorLightPremiumColors = NoorPremiumColors(
    emeraldDeep = Color(0xFF052E26),
    emerald = Color(0xFF073F34),
    emeraldElevated = Color(0xFF0A4C3E),
    emeraldMuted = Color(0xFF1B6555),
    ivory = Color(0xFFFFF8E8),
    ivoryElevated = Color(0xFFF8EED8),
    mutedGold = Color(0xFFC4A35A),
    goldHighlight = Color(0xFFE4C97F),
    onEmerald = Color(0xFFFFF7E7),
    onIvory = Color(0xFF16372F),
    onIvoryMuted = Color(0xFF5B675F),
)

internal val NoorDarkPremiumColors = NoorPremiumColors(
    emeraldDeep = Color(0xFF031E19),
    emerald = Color(0xFF052E26),
    emeraldElevated = Color(0xFF0A4438),
    emeraldMuted = Color(0xFF27705F),
    ivory = Color(0xFFFFF5DF),
    ivoryElevated = Color(0xFFF3E6CB),
    mutedGold = Color(0xFFD0B46D),
    goldHighlight = Color(0xFFF0D58B),
    onEmerald = Color(0xFFFFF7E7),
    onIvory = Color(0xFF102E27),
    onIvoryMuted = Color(0xFF58655D),
)

internal val LocalNoorPremiumColors = staticCompositionLocalOf { NoorLightPremiumColors }
