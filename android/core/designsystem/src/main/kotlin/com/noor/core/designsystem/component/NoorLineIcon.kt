package com.noor.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** Small fixed icon set drawn locally to avoid the deprecated Material Icons artifact. */
enum class NoorLineIconType {
    Bell,
    Settings,
    BookOpen,
    Surahs,
    Search,
    Headphones,
    Bookmark,
    Document,
    Home,
    Heart,
    Calendar,
    More,
    Play,
    Pause,
    Previous,
    Next,
    Stop,
    Arrow,
    Close,
    Delete,
}

@Composable
fun NoorLineIcon(
    type: NoorLineIconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    val effectiveTint = if (tint == Color.Unspecified) Color.Black else tint
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(
        modifier = modifier.then(
            if (contentDescription == null) Modifier else Modifier.semantics {
                this.contentDescription = contentDescription
            },
        ),
    ) {
        val width = size.minDimension
        val origin = Offset((size.width - width) / 2f, (size.height - width) / 2f)
        val stroke = Stroke(
            width = (width * 0.075f).coerceAtLeast(1.5.dp.toPx()),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )

        fun point(x: Float, y: Float) = Offset(origin.x + width * x, origin.y + width * y)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(effectiveTint, point(x1, y1), point(x2, y2), stroke.width, StrokeCap.Round)

        when (type) {
            NoorLineIconType.Search -> {
                drawCircle(
                    color = effectiveTint,
                    radius = width * 0.25f,
                    center = point(0.43f, 0.43f),
                    style = stroke,
                )
                line(0.61f, 0.61f, 0.84f, 0.84f)
            }

            NoorLineIconType.Bookmark -> {
                val path = Path().apply {
                    moveTo(point(0.29f, 0.15f).x, point(0.29f, 0.15f).y)
                    lineTo(point(0.71f, 0.15f).x, point(0.71f, 0.15f).y)
                    lineTo(point(0.71f, 0.85f).x, point(0.71f, 0.85f).y)
                    lineTo(point(0.50f, 0.68f).x, point(0.50f, 0.68f).y)
                    lineTo(point(0.29f, 0.85f).x, point(0.29f, 0.85f).y)
                    close()
                }
                drawPath(path, effectiveTint, style = stroke)
            }

            NoorLineIconType.BookOpen -> {
                val leftPage = Path().apply {
                    moveTo(point(0.10f, 0.24f).x, point(0.10f, 0.24f).y)
                    quadraticTo(
                        point(0.31f, 0.18f).x,
                        point(0.31f, 0.18f).y,
                        point(0.49f, 0.31f).x,
                        point(0.49f, 0.31f).y,
                    )
                    lineTo(point(0.49f, 0.82f).x, point(0.49f, 0.82f).y)
                    quadraticTo(
                        point(0.31f, 0.69f).x,
                        point(0.31f, 0.69f).y,
                        point(0.10f, 0.75f).x,
                        point(0.10f, 0.75f).y,
                    )
                    close()
                }
                val rightPage = Path().apply {
                    moveTo(point(0.90f, 0.24f).x, point(0.90f, 0.24f).y)
                    quadraticTo(
                        point(0.69f, 0.18f).x,
                        point(0.69f, 0.18f).y,
                        point(0.51f, 0.31f).x,
                        point(0.51f, 0.31f).y,
                    )
                    lineTo(point(0.51f, 0.82f).x, point(0.51f, 0.82f).y)
                    quadraticTo(
                        point(0.69f, 0.69f).x,
                        point(0.69f, 0.69f).y,
                        point(0.90f, 0.75f).x,
                        point(0.90f, 0.75f).y,
                    )
                    close()
                }
                drawPath(leftPage, effectiveTint, style = stroke)
                drawPath(rightPage, effectiveTint, style = stroke)
            }

            NoorLineIconType.Surahs -> {
                listOf(0.28f, 0.50f, 0.72f).forEach { y ->
                    drawCircle(effectiveTint, width * 0.045f, point(0.18f, y), style = Fill)
                    line(0.31f, y, 0.82f, y)
                }
            }

            NoorLineIconType.Headphones -> {
                drawArc(
                    color = effectiveTint,
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = point(0.17f, 0.17f),
                    size = Size(width * 0.66f, width * 0.66f),
                    style = stroke,
                )
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.12f, 0.48f),
                    size = Size(width * 0.16f, width * 0.31f),
                    cornerRadius = CornerRadius(width * 0.05f),
                    style = stroke,
                )
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.72f, 0.48f),
                    size = Size(width * 0.16f, width * 0.31f),
                    cornerRadius = CornerRadius(width * 0.05f),
                    style = stroke,
                )
            }

            NoorLineIconType.Document -> {
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.22f, 0.12f),
                    size = Size(width * 0.56f, width * 0.76f),
                    cornerRadius = CornerRadius(width * 0.05f),
                    style = stroke,
                )
                line(0.34f, 0.35f, 0.67f, 0.35f)
                line(0.34f, 0.50f, 0.67f, 0.50f)
                line(0.34f, 0.65f, 0.58f, 0.65f)
            }

            NoorLineIconType.Home -> {
                val roof = Path().apply {
                    moveTo(point(0.12f, 0.46f).x, point(0.12f, 0.46f).y)
                    lineTo(point(0.50f, 0.14f).x, point(0.50f, 0.14f).y)
                    lineTo(point(0.88f, 0.46f).x, point(0.88f, 0.46f).y)
                }
                drawPath(roof, effectiveTint, style = stroke)
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.24f, 0.42f),
                    size = Size(width * 0.52f, width * 0.43f),
                    cornerRadius = CornerRadius(width * 0.04f),
                    style = stroke,
                )
                line(0.50f, 0.85f, 0.50f, 0.62f)
            }

            NoorLineIconType.Heart -> {
                val path = Path().apply {
                    moveTo(point(0.50f, 0.84f).x, point(0.50f, 0.84f).y)
                    cubicTo(
                        point(0.14f, 0.61f).x,
                        point(0.14f, 0.61f).y,
                        point(0.12f, 0.33f).x,
                        point(0.12f, 0.33f).y,
                        point(0.32f, 0.23f).x,
                        point(0.32f, 0.23f).y,
                    )
                    cubicTo(
                        point(0.43f, 0.18f).x,
                        point(0.43f, 0.18f).y,
                        point(0.50f, 0.29f).x,
                        point(0.50f, 0.29f).y,
                        point(0.50f, 0.29f).x,
                        point(0.50f, 0.29f).y,
                    )
                    cubicTo(
                        point(0.50f, 0.29f).x,
                        point(0.50f, 0.29f).y,
                        point(0.57f, 0.18f).x,
                        point(0.57f, 0.18f).y,
                        point(0.68f, 0.23f).x,
                        point(0.68f, 0.23f).y,
                    )
                    cubicTo(
                        point(0.88f, 0.33f).x,
                        point(0.88f, 0.33f).y,
                        point(0.86f, 0.61f).x,
                        point(0.86f, 0.61f).y,
                        point(0.50f, 0.84f).x,
                        point(0.50f, 0.84f).y,
                    )
                    close()
                }
                drawPath(path, effectiveTint, style = stroke)
            }

            NoorLineIconType.Calendar -> {
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.16f, 0.20f),
                    size = Size(width * 0.68f, width * 0.64f),
                    cornerRadius = CornerRadius(width * 0.07f),
                    style = stroke,
                )
                line(0.16f, 0.39f, 0.84f, 0.39f)
                line(0.33f, 0.12f, 0.33f, 0.28f)
                line(0.67f, 0.12f, 0.67f, 0.28f)
                drawCircle(effectiveTint, width * 0.035f, point(0.35f, 0.58f))
                drawCircle(effectiveTint, width * 0.035f, point(0.53f, 0.58f))
                drawCircle(effectiveTint, width * 0.035f, point(0.70f, 0.58f))
            }

            NoorLineIconType.More -> {
                listOf(0.27f, 0.50f, 0.73f).forEach { x ->
                    drawCircle(effectiveTint, width * 0.055f, point(x, 0.50f), style = Fill)
                }
            }

            NoorLineIconType.Play -> {
                val path = Path().apply {
                    moveTo(point(0.34f, 0.22f).x, point(0.34f, 0.22f).y)
                    lineTo(point(0.78f, 0.50f).x, point(0.78f, 0.50f).y)
                    lineTo(point(0.34f, 0.78f).x, point(0.34f, 0.78f).y)
                    close()
                }
                drawPath(path, effectiveTint, style = Fill)
            }

            NoorLineIconType.Pause -> {
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.28f, 0.22f),
                    size = Size(width * 0.16f, width * 0.56f),
                    cornerRadius = CornerRadius(width * 0.035f),
                    style = Fill,
                )
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.56f, 0.22f),
                    size = Size(width * 0.16f, width * 0.56f),
                    cornerRadius = CornerRadius(width * 0.035f),
                    style = Fill,
                )
            }

            NoorLineIconType.Previous -> {
                line(0.24f, 0.23f, 0.24f, 0.77f)
                val path = Path().apply {
                    moveTo(point(0.72f, 0.22f).x, point(0.72f, 0.22f).y)
                    lineTo(point(0.34f, 0.50f).x, point(0.34f, 0.50f).y)
                    lineTo(point(0.72f, 0.78f).x, point(0.72f, 0.78f).y)
                    close()
                }
                drawPath(path, effectiveTint, style = Fill)
            }

            NoorLineIconType.Next -> {
                line(0.76f, 0.23f, 0.76f, 0.77f)
                val path = Path().apply {
                    moveTo(point(0.28f, 0.22f).x, point(0.28f, 0.22f).y)
                    lineTo(point(0.66f, 0.50f).x, point(0.66f, 0.50f).y)
                    lineTo(point(0.28f, 0.78f).x, point(0.28f, 0.78f).y)
                    close()
                }
                drawPath(path, effectiveTint, style = Fill)
            }

            NoorLineIconType.Stop -> {
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.25f, 0.25f),
                    size = Size(width * 0.50f, width * 0.50f),
                    cornerRadius = CornerRadius(width * 0.06f),
                    style = Fill,
                )
            }

            NoorLineIconType.Settings -> {
                drawCircle(effectiveTint, width * 0.16f, point(0.50f, 0.50f), style = stroke)
                repeat(8) { index ->
                    rotate(index * 45f, pivot = point(0.50f, 0.50f)) {
                        line(0.50f, 0.08f, 0.50f, 0.24f)
                    }
                }
            }

            NoorLineIconType.Bell -> {
                val path = Path().apply {
                    moveTo(point(0.23f, 0.66f).x, point(0.23f, 0.66f).y)
                    quadraticTo(
                        point(0.31f, 0.56f).x,
                        point(0.31f, 0.56f).y,
                        point(0.31f, 0.40f).x,
                        point(0.31f, 0.40f).y,
                    )
                    quadraticTo(
                        point(0.31f, 0.18f).x,
                        point(0.31f, 0.18f).y,
                        point(0.50f, 0.18f).x,
                        point(0.50f, 0.18f).y,
                    )
                    quadraticTo(
                        point(0.69f, 0.18f).x,
                        point(0.69f, 0.18f).y,
                        point(0.69f, 0.40f).x,
                        point(0.69f, 0.40f).y,
                    )
                    quadraticTo(
                        point(0.69f, 0.56f).x,
                        point(0.69f, 0.56f).y,
                        point(0.77f, 0.66f).x,
                        point(0.77f, 0.66f).y,
                    )
                    close()
                }
                drawPath(path, effectiveTint, style = stroke)
                drawArc(
                    effectiveTint,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = point(0.41f, 0.67f),
                    size = Size(width * 0.18f, width * 0.16f),
                    style = stroke,
                )
            }

            NoorLineIconType.Arrow -> {
                val direction = if (isRtl) -1f else 1f
                val centerX = 0.50f
                val tipX = centerX + 0.24f * direction
                line(centerX - 0.23f * direction, 0.50f, tipX, 0.50f)
                line(tipX, 0.50f, centerX + 0.07f * direction, 0.29f)
                line(tipX, 0.50f, centerX + 0.07f * direction, 0.71f)
            }

            NoorLineIconType.Close -> {
                line(0.24f, 0.24f, 0.76f, 0.76f)
                line(0.76f, 0.24f, 0.24f, 0.76f)
            }

            NoorLineIconType.Delete -> {
                drawRoundRect(
                    color = effectiveTint,
                    topLeft = point(0.28f, 0.30f),
                    size = Size(width * 0.44f, width * 0.56f),
                    cornerRadius = CornerRadius(width * 0.05f),
                    style = stroke,
                )
                line(0.21f, 0.24f, 0.79f, 0.24f)
                line(0.39f, 0.15f, 0.61f, 0.15f)
                line(0.42f, 0.42f, 0.42f, 0.72f)
                line(0.58f, 0.42f, 0.58f, 0.72f)
            }
        }
    }
}
