package com.noor.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noor.app.R
import com.noor.core.designsystem.component.NoorLineIcon
import com.noor.core.designsystem.component.NoorLineIconType
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.Ayah
import com.noor.domain.model.Surah

@Composable
internal fun ContinueReadingCard(
    surah: Surah?,
    ayah: Ayah?,
    onClick: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 210.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.emeraldElevated),
        border = BorderStroke(1.dp, colors.mutedGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NoorDesignSystem.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
            ) {
                Text(
                    text = stringResource(R.string.continue_reading),
                    color = colors.onEmerald.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = surah?.nameArabic ?: stringResource(R.string.start_with_fatihah),
                    color = colors.onEmerald,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContinueMetadata(
                        icon = NoorLineIconType.Bookmark,
                        text = stringResource(
                            R.string.ayah_number_short,
                            ayah?.numberInSurah ?: 1,
                        ),
                    )
                    ContinueMetadata(
                        icon = NoorLineIconType.BookOpen,
                        text = stringResource(
                            R.string.page_number_short,
                            ayah?.pageNumber ?: surah?.startPage ?: 1,
                        ),
                    )
                }
                Button(
                    onClick = onClick,
                    enabled = surah != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.goldHighlight,
                        contentColor = colors.emeraldDeep,
                        disabledContainerColor = colors.goldHighlight.copy(alpha = 0.6f),
                        disabledContentColor = colors.emeraldDeep.copy(alpha = 0.6f),
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = NoorDesignSystem.spacing.large,
                        vertical = NoorDesignSystem.spacing.small,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            if (ayah == null) R.string.start_reading else R.string.continue_reading,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            QuranArchArtwork(
                modifier = Modifier
                    .width(132.dp)
                    .height(176.dp),
            )
        }
    }
}

@Composable
private fun ContinueMetadata(icon: NoorLineIconType, text: String) {
    val colors = NoorDesignSystem.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoorLineIcon(
            type = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = colors.goldHighlight,
        )
        Text(
            text = text,
            color = colors.onEmerald.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun QuranArchArtwork(modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val outer = Path().apply {
            moveTo(size.width * 0.12f, size.height * 0.94f)
            lineTo(size.width * 0.12f, size.height * 0.42f)
            cubicTo(
                size.width * 0.12f,
                size.height * 0.16f,
                size.width * 0.34f,
                size.height * 0.06f,
                size.width * 0.50f,
                size.height * 0.02f,
            )
            cubicTo(
                size.width * 0.66f,
                size.height * 0.06f,
                size.width * 0.88f,
                size.height * 0.16f,
                size.width * 0.88f,
                size.height * 0.42f,
            )
            lineTo(size.width * 0.88f, size.height * 0.94f)
            close()
        }
        val inner = Path().apply {
            moveTo(size.width * 0.23f, size.height * 0.82f)
            lineTo(size.width * 0.23f, size.height * 0.44f)
            cubicTo(
                size.width * 0.23f,
                size.height * 0.26f,
                size.width * 0.39f,
                size.height * 0.17f,
                size.width * 0.50f,
                size.height * 0.13f,
            )
            cubicTo(
                size.width * 0.61f,
                size.height * 0.17f,
                size.width * 0.77f,
                size.height * 0.26f,
                size.width * 0.77f,
                size.height * 0.44f,
            )
            lineTo(size.width * 0.77f, size.height * 0.82f)
        }
        drawPath(outer, colors.mutedGold.copy(alpha = 0.78f), style = stroke)
        drawPath(inner, colors.goldHighlight.copy(alpha = 0.48f), style = stroke)
        drawRoundRect(
            color = colors.ivory.copy(alpha = 0.10f),
            topLeft = Offset(size.width * 0.31f, size.height * 0.29f),
            size = Size(size.width * 0.38f, size.height * 0.47f),
            cornerRadius = CornerRadius(size.width * 0.18f),
            style = Fill,
        )
        val leftPage = Path().apply {
            moveTo(size.width * 0.18f, size.height * 0.80f)
            quadraticTo(
                size.width * 0.36f,
                size.height * 0.70f,
                size.width * 0.49f,
                size.height * 0.82f,
            )
            lineTo(size.width * 0.49f, size.height * 0.96f)
            quadraticTo(
                size.width * 0.34f,
                size.height * 0.86f,
                size.width * 0.18f,
                size.height * 0.91f,
            )
            close()
        }
        val rightPage = Path().apply {
            moveTo(size.width * 0.82f, size.height * 0.80f)
            quadraticTo(
                size.width * 0.64f,
                size.height * 0.70f,
                size.width * 0.51f,
                size.height * 0.82f,
            )
            lineTo(size.width * 0.51f, size.height * 0.96f)
            quadraticTo(
                size.width * 0.66f,
                size.height * 0.86f,
                size.width * 0.82f,
                size.height * 0.91f,
            )
            close()
        }
        drawPath(leftPage, colors.ivory, style = Fill)
        drawPath(rightPage, colors.ivory, style = Fill)
        drawPath(leftPage, colors.mutedGold, style = stroke)
        drawPath(rightPage, colors.mutedGold, style = stroke)
        drawLine(
            color = colors.mutedGold,
            start = Offset(size.width * 0.34f, size.height * 0.96f),
            end = Offset(size.width * 0.66f, size.height * 0.96f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
internal fun ReadingProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(7.dp),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = colors.emerald.copy(alpha = 0.12f),
                cornerRadius = CornerRadius(size.height / 2f),
            )
            drawRoundRect(
                color = colors.emerald,
                size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                cornerRadius = CornerRadius(size.height / 2f),
            )
        }
    }
}

@Composable
internal fun ReciterWaveform(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.height(32.dp)) {
        val amplitudes = listOf(
            0.22f,
            0.42f,
            0.70f,
            0.35f,
            0.82f,
            0.52f,
            0.95f,
            0.44f,
            0.72f,
            0.32f,
            0.58f,
            0.26f,
        )
        val step = size.width / amplitudes.size
        amplitudes.forEachIndexed { index, amplitude ->
            val x = step * index + step / 2f
            val halfHeight = size.height * amplitude * 0.42f
            drawLine(
                color = color.copy(alpha = 0.78f),
                start = Offset(x, size.height / 2f - halfHeight),
                end = Offset(x, size.height / 2f + halfHeight),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
internal fun NoorMemorialOrnament(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        repeat(4) { index ->
            val angle = index * 90f
            rotate(angle, pivot = center) {
                val petal = Path().apply {
                    moveTo(center.x, center.y - size.minDimension * 0.42f)
                    quadraticTo(
                        center.x + size.minDimension * 0.18f,
                        center.y - size.minDimension * 0.17f,
                        center.x,
                        center.y,
                    )
                    quadraticTo(
                        center.x - size.minDimension * 0.18f,
                        center.y - size.minDimension * 0.17f,
                        center.x,
                        center.y - size.minDimension * 0.42f,
                    )
                }
                drawPath(petal, color, style = stroke)
            }
        }
        drawCircle(color, radius = size.minDimension * 0.08f, center = center, style = Fill)
    }
}
