package com.noor.app.ui.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.noor.app.R
import com.noor.core.designsystem.component.NoorLineIcon
import com.noor.core.designsystem.component.NoorLineIconType
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import com.noor.domain.model.UserSettings
import kotlin.math.roundToInt

@Composable
internal fun ReaderTopBar(
    surahName: String,
    juzNumber: Int,
    pageNumber: Int,
    onBack: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Surface(
        color = colors.emeraldDeep,
        contentColor = colors.onEmerald,
        shadowElevation = 6.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 78.dp)
                .padding(
                    horizontal = NoorDesignSystem.spacing.small,
                    vertical = NoorDesignSystem.spacing.small,
                ),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.back),
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                NoorLineIcon(
                    type = NoorLineIconType.Arrow,
                    contentDescription = null,
                    modifier = Modifier
                        .size(25.dp)
                        .graphicsLayer { rotationZ = 180f },
                    tint = colors.goldHighlight,
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.reader_surah_title, surahName),
                    modifier = Modifier.testTag(ReaderTestTags.SURAH_TITLE),
                    color = colors.onEmerald,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(
                        R.string.reader_position,
                        juzNumber,
                        pageNumber,
                    ),
                    color = colors.goldHighlight,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
internal fun ReaderReadingSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.ivory,
        contentColor = colors.onIvory,
        border = BorderStroke(1.dp, colors.mutedGold.copy(alpha = 0.78f)),
        shadowElevation = 8.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ReaderCornerOrnaments(
                modifier = Modifier
                    .matchParentSize()
                    .padding(5.dp),
            )
            content()
        }
    }
}

@Composable
internal fun ReaderOpeningHeader(
    showBasmala: Boolean,
    onPlaySurah: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = NoorDesignSystem.spacing.large,
                top = NoorDesignSystem.spacing.large,
                end = NoorDesignSystem.spacing.large,
                bottom = NoorDesignSystem.spacing.small,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
    ) {
        OrnamentalDivider()
        if (showBasmala) {
            Text(
                text = stringResource(R.string.reader_basmala),
                modifier = Modifier.fillMaxWidth(),
                color = colors.onIvory,
                style = NoorDesignSystem.extendedTypography.quranMedium,
                textAlign = TextAlign.Center,
            )
        }
        TextButton(
            onClick = onPlaySurah,
            colors = ButtonDefaults.textButtonColors(contentColor = colors.emeraldDeep),
        ) {
            NoorLineIcon(
                type = NoorLineIconType.Play,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = colors.mutedGold,
            )
            Spacer(modifier = Modifier.size(NoorDesignSystem.spacing.small))
            Text(
                text = stringResource(R.string.reader_play_surah),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        OrnamentalDivider()
    }
}

@Composable
internal fun ReaderAyahRow(
    ayah: Ayah,
    textScale: Float,
    bookmarked: Boolean,
    playing: Boolean,
) {
    val colors = NoorDesignSystem.colors
    val background = if (playing) {
        colors.goldHighlight.copy(alpha = 0.14f)
    } else {
        Color.Transparent
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(background),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = NoorDesignSystem.spacing.large,
                        vertical = NoorDesignSystem.spacing.medium,
                    ),
                horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ayah.textUthmani,
                    modifier = Modifier.weight(1f),
                    color = colors.onIvory,
                    style = NoorDesignSystem.extendedTypography.quranLarge.copy(
                        fontSize = (32f * textScale).sp,
                        lineHeight = (58f * textScale).sp,
                    ),
                    textAlign = TextAlign.Start,
                )
                AyahNumberMedallion(
                    number = ayah.numberInSurah,
                    highlighted = bookmarked || playing,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = NoorDesignSystem.spacing.large),
                color = colors.mutedGold.copy(alpha = 0.26f),
                thickness = 0.7.dp,
            )
        }
    }
}

@Composable
internal fun ReaderBottomBar(
    currentAyah: Ayah,
    bookmarked: Boolean,
    bookmarkUpdateInProgress: Boolean,
    playbackState: QuranPlaybackState,
    currentMediaId: String?,
    onToggleBookmark: () -> Unit,
    onOpenTafsir: () -> Unit,
    onListen: () -> Unit,
    onTextSize: () -> Unit,
    onStopAudio: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Surface(
        color = colors.emeraldDeep,
        contentColor = colors.onEmerald,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            if (playbackState !is QuranPlaybackState.Idle) {
                ReaderPlaybackStrip(
                    playbackState = playbackState,
                    onStopAudio = onStopAudio,
                )
            }
            HorizontalDivider(
                color = colors.mutedGold.copy(alpha = 0.55f),
                thickness = 1.dp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 76.dp)
                    .padding(horizontal = NoorDesignSystem.spacing.extraSmall),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReaderBottomAction(
                    label = stringResource(R.string.reader_bookmark_action),
                    icon = NoorLineIconType.Bookmark,
                    selected = bookmarked,
                    enabled = !bookmarkUpdateInProgress,
                    testTag = ReaderTestTags.BOOKMARK_ACTION,
                    onClick = onToggleBookmark,
                )
                ReaderBottomAction(
                    label = stringResource(R.string.reader_tafsir_action),
                    icon = NoorLineIconType.Document,
                    testTag = ReaderTestTags.TAFSIR_ACTION,
                    onClick = onOpenTafsir,
                )
                ReaderBottomAction(
                    label = readerListenLabel(
                        state = playbackState,
                        currentMediaId = currentMediaId,
                        ayahId = currentAyah.id,
                    ),
                    icon = NoorLineIconType.Headphones,
                    selected = true.takeIf { isCurrentAyahPlaying(playbackState, currentAyah.id) },
                    testTag = ReaderTestTags.LISTEN_ACTION,
                    onClick = onListen,
                )
                ReaderBottomAction(
                    label = stringResource(R.string.reader_text_size_action),
                    textIcon = "Aa",
                    testTag = ReaderTestTags.TEXT_SIZE_ACTION,
                    onClick = onTextSize,
                )
            }
        }
    }
}

@Composable
internal fun ReaderTextScaleDialog(
    initialScale: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    val colors = NoorDesignSystem.colors
    var previewScale by remember(initialScale) { mutableFloatStateOf(initialScale) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .testTag(ReaderTestTags.TEXT_SIZE_DIALOG),
            shape = MaterialTheme.shapes.extraLarge,
            color = colors.ivory,
            contentColor = colors.onIvory,
            border = BorderStroke(1.dp, colors.mutedGold),
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier.padding(NoorDesignSystem.spacing.large),
                verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            ) {
                Text(
                    text = stringResource(R.string.reader_text_size_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.emeraldDeep,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        R.string.reader_text_scale_percent,
                        (previewScale * 100).roundToInt(),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onIvoryMuted,
                )
                Slider(
                    value = previewScale,
                    onValueChange = { previewScale = it },
                    valueRange = UserSettings.MIN_TEXT_SCALE..UserSettings.MAX_TEXT_SCALE,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.emeraldDeep,
                        activeTrackColor = colors.mutedGold,
                        inactiveTrackColor = colors.mutedGold.copy(alpha = 0.25f),
                    ),
                )
                Text(
                    text = stringResource(R.string.quran_text_preview),
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.onIvory,
                    style = NoorDesignSystem.extendedTypography.quranMedium.copy(
                        fontSize = (26f * previewScale).sp,
                        lineHeight = (48f * previewScale).sp,
                    ),
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            onConfirm(previewScale)
                            onDismiss()
                        },
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ReaderBottomAction(
    label: String,
    onClick: () -> Unit,
    icon: NoorLineIconType? = null,
    textIcon: String? = null,
    selected: Boolean? = null,
    enabled: Boolean = true,
    testTag: String,
) {
    val colors = NoorDesignSystem.colors
    val tint = if (selected == true) colors.goldHighlight else colors.onEmerald.copy(alpha = 0.88f)
    val selectionSemantics = if (selected == null) {
        Modifier
    } else {
        Modifier.semantics { this.selected = selected == true }
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .sizeIn(minWidth = 48.dp, minHeight = 64.dp)
            .testTag(testTag)
            .then(selectionSemantics)
            .alpha(if (enabled) 1f else 0.55f)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            )
            .padding(
                horizontal = NoorDesignSystem.spacing.extraSmall,
                vertical = NoorDesignSystem.spacing.small,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        when {
            icon != null -> NoorLineIcon(
                type = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = tint,
            )

            textIcon != null -> Text(
                text = textIcon,
                color = tint,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ReaderPlaybackStrip(
    playbackState: QuranPlaybackState,
    onStopAudio: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = NoorDesignSystem.spacing.medium,
                end = NoorDesignSystem.spacing.small,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = playbackStatusText(playbackState),
            modifier = Modifier.weight(1f),
            color = if (playbackState is QuranPlaybackState.Error) {
                MaterialTheme.colorScheme.error
            } else {
                colors.goldHighlight
            },
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
        TextButton(
            onClick = onStopAudio,
            colors = ButtonDefaults.textButtonColors(contentColor = colors.onEmerald),
        ) {
            Text(stringResource(R.string.reader_stop_audio))
        }
    }
}

@Composable
private fun playbackStatusText(state: QuranPlaybackState): String = stringResource(
    when (state) {
        QuranPlaybackState.Idle -> R.string.audio_idle
        QuranPlaybackState.Buffering -> R.string.audio_buffering
        is QuranPlaybackState.Playing -> R.string.audio_playing
        QuranPlaybackState.Paused -> R.string.audio_paused
        QuranPlaybackState.Error -> R.string.audio_error
    },
)

@Composable
private fun readerListenLabel(
    state: QuranPlaybackState,
    currentMediaId: String?,
    ayahId: Int,
): String = stringResource(
    when {
        state is QuranPlaybackState.Playing && state.mediaId == "ayah:$ayahId" -> {
            R.string.reader_pause_action
        }

        state is QuranPlaybackState.Paused && currentMediaId == "ayah:$ayahId" -> {
            R.string.reader_resume_action
        }

        state is QuranPlaybackState.Buffering && currentMediaId == "ayah:$ayahId" -> {
            R.string.reader_pause_action
        }

        else -> R.string.reader_listen_action
    },
)

private fun isCurrentAyahPlaying(state: QuranPlaybackState, ayahId: Int): Boolean =
    state is QuranPlaybackState.Playing && state.mediaId == "ayah:$ayahId"

@Composable
private fun AyahNumberMedallion(number: Int, highlighted: Boolean) {
    val colors = NoorDesignSystem.colors
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension * 0.46f
            val innerRadius = size.minDimension * 0.34f
            val star = Path()
            repeat(16) { index ->
                val angle = Math.toRadians((index * 22.5 - 90.0))
                val radius = if (index % 2 == 0) outerRadius else innerRadius
                val point = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * radius,
                    y = center.y + kotlin.math.sin(angle).toFloat() * radius,
                )
                if (index == 0) star.moveTo(point.x, point.y) else star.lineTo(point.x, point.y)
            }
            star.close()
            drawPath(
                path = star,
                color = if (highlighted) colors.mutedGold else colors.ivoryElevated,
                style = Fill,
            )
            drawPath(
                path = star,
                color = colors.mutedGold,
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
            drawCircle(
                color = if (highlighted) colors.emeraldDeep else colors.ivory,
                radius = size.minDimension * 0.26f,
                center = center,
                style = Fill,
            )
            drawCircle(
                color = colors.mutedGold,
                radius = size.minDimension * 0.26f,
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
        }
        Text(
            text = number.toString(),
            color = if (highlighted) colors.goldHighlight else colors.onIvory,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun OrnamentalDivider() {
    val colors = NoorDesignSystem.colors
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp),
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val lineGap = 22.dp.toPx()
        drawLine(
            color = colors.mutedGold.copy(alpha = 0.62f),
            start = Offset(size.width * 0.18f, center.y),
            end = Offset(center.x - lineGap, center.y),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = colors.mutedGold.copy(alpha = 0.62f),
            start = Offset(center.x + lineGap, center.y),
            end = Offset(size.width * 0.82f, center.y),
            strokeWidth = 1.dp.toPx(),
        )
        val diamond = Path().apply {
            moveTo(center.x, center.y - 7.dp.toPx())
            lineTo(center.x + 7.dp.toPx(), center.y)
            lineTo(center.x, center.y + 7.dp.toPx())
            lineTo(center.x - 7.dp.toPx(), center.y)
            close()
        }
        drawPath(diamond, colors.mutedGold, style = Fill)
        drawCircle(
            color = colors.ivory,
            radius = 2.5.dp.toPx(),
            center = center,
            style = Fill,
        )
    }
}

@Composable
private fun ReaderCornerOrnaments(modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Canvas(modifier = modifier) {
        val length = 38.dp.toPx().coerceAtMost(size.minDimension * 0.22f)
        val inset = 8.dp.toPx()
        val stroke = Stroke(
            width = 1.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )

        fun cornerPath(start: Offset, horizontal: Float, vertical: Float): Path = Path().apply {
            moveTo(start.x, start.y + vertical * length)
            lineTo(start.x, start.y + vertical * length * 0.36f)
            quadraticTo(
                start.x,
                start.y,
                start.x + horizontal * length * 0.36f,
                start.y,
            )
            lineTo(start.x + horizontal * length, start.y)
            moveTo(
                start.x + horizontal * length * 0.18f,
                start.y + vertical * length * 0.72f,
            )
            lineTo(
                start.x + horizontal * length * 0.18f,
                start.y + vertical * length * 0.34f,
            )
            quadraticTo(
                start.x + horizontal * length * 0.18f,
                start.y + vertical * length * 0.18f,
                start.x + horizontal * length * 0.34f,
                start.y + vertical * length * 0.18f,
            )
            lineTo(
                start.x + horizontal * length * 0.72f,
                start.y + vertical * length * 0.18f,
            )
        }

        val topStart = Offset(inset, inset)
        val topEnd = Offset(size.width - inset, inset)
        val bottomStart = Offset(inset, size.height - inset)
        val bottomEnd = Offset(size.width - inset, size.height - inset)
        listOf(
            cornerPath(topStart, 1f, 1f),
            cornerPath(topEnd, -1f, 1f),
            cornerPath(bottomStart, 1f, -1f),
            cornerPath(bottomEnd, -1f, -1f),
        ).forEach { path ->
            drawPath(path, colors.mutedGold.copy(alpha = 0.7f), style = stroke)
        }
    }
}


internal object ReaderTestTags {
    const val SURAH_TITLE = "reader_surah_title"
    const val BOOKMARK_ACTION = "reader_action_bookmark"
    const val TAFSIR_ACTION = "reader_action_tafsir"
    const val LISTEN_ACTION = "reader_action_listen"
    const val TEXT_SIZE_ACTION = "reader_action_text_size"
    const val TEXT_SIZE_DIALOG = "reader_text_size_dialog"
}
