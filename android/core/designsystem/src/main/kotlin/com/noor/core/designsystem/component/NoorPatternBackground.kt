package com.noor.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.noor.core.designsystem.theme.NoorDesignSystem

/** Brand background with a restrained geometric pattern that is cheap to redraw. */
@Composable
fun NoorPatternBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.emeraldDeep)
            .drawWithCache {
                val cell = 52.dp.toPx()
                val half = cell / 2f
                val strokeWidth = 0.75.dp.toPx()
                val patternColor = colors.mutedGold.copy(alpha = 0.075f)
                val path = Path()
                var y = -cell
                while (y < size.height + cell) {
                    var x = -cell
                    while (x < size.width + cell) {
                        path.moveTo(x + half, y)
                        path.lineTo(x + cell, y + half)
                        path.lineTo(x + half, y + cell)
                        path.lineTo(x, y + half)
                        path.close()
                        path.moveTo(x + half, y + cell * 0.22f)
                        path.lineTo(x + cell * 0.78f, y + half)
                        path.lineTo(x + half, y + cell * 0.78f)
                        path.lineTo(x + cell * 0.22f, y + half)
                        path.close()
                        x += cell
                    }
                    y += cell
                }
                onDrawBehind {
                    drawPath(path, patternColor, style = Stroke(width = strokeWidth))
                    drawCircle(
                        color = colors.mutedGold.copy(alpha = 0.04f),
                        radius = size.minDimension * 0.42f,
                        center = Offset(size.width * 0.78f, size.height * 0.08f),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
            },
        content = content,
    )
}
