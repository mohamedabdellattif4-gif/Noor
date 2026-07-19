package com.noor.app.ui.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noor.app.R
import com.noor.core.designsystem.component.NoorFilledButton
import com.noor.core.designsystem.component.NoorLineIcon
import com.noor.core.designsystem.component.NoorLineIconType
import com.noor.core.designsystem.component.NoorLoadingIndicator
import com.noor.core.designsystem.component.NoorOutlinedButton
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.core.media.QuranPlaybackInfo
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Reciter
import com.noor.domain.model.Surah
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sin

internal object AudioTestTags {
    const val PLAYER_CARD = "audio_player_card"
    const val PLAY_PAUSE = "audio_play_pause"
    const val PREVIOUS = "audio_previous"
    const val NEXT = "audio_next"
    const val STOP = "audio_stop"
    const val OPEN_READER = "audio_open_reader"
    const val RETRY = "audio_retry"
    const val SURAH_LIST = "audio_surah_list"
    fun reciter(reciter: Reciter): String = "audio_reciter_${reciter.name.lowercase(Locale.ROOT)}"
    fun surah(number: Int): String = "audio_surah_$number"
}

@Composable
internal fun AudioTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.emeraldDeep,
        contentColor = colors.onEmerald,
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 88.dp)
                .padding(horizontal = NoorDesignSystem.spacing.small),
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = colors.onEmerald),
            ) {
                NoorLineIcon(
                    type = NoorLineIconType.Arrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.goldHighlight,
                )
                Spacer(modifier = Modifier.width(NoorDesignSystem.spacing.extraSmall))
                Text(stringResource(R.string.back))
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.audio_screen_title),
                    color = colors.onEmerald,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.audio_screen_subtitle),
                    color = colors.goldHighlight,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
internal fun PremiumAudioPlayerCard(
    uiState: AudioUiState,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenReader: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    val info = uiState.playbackInfo
    val currentAyah = uiState.currentAyah
    val playerTitle = info.title
        ?: uiState.selectedSurah?.let { stringResource(R.string.audio_surah_title, it.nameArabic) }
        ?: stringResource(R.string.audio_select_surah_title)
    val playerArtist = info.artist ?: reciterLabel(uiState.reciter)
    val isPlayingOrBuffering = uiState.playbackState is QuranPlaybackState.Playing ||
        uiState.playbackState is QuranPlaybackState.Buffering

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AudioTestTags.PLAYER_CARD),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.emeraldElevated),
        border = BorderStroke(1.dp, colors.mutedGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NoorDesignSystem.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
        ) {
            Surface(
                shape = CircleShape,
                color = colors.ivory,
                border = BorderStroke(1.dp, colors.goldHighlight),
                shadowElevation = 8.dp,
            ) {
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    NoorLineIcon(
                        type = NoorLineIconType.Headphones,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = colors.emerald,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = playerTitle,
                    color = colors.onEmerald,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = playerArtist,
                    color = colors.goldHighlight,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (currentAyah != null) {
                    Text(
                        text = stringResource(
                            R.string.audio_current_ayah_metadata,
                            currentAyah.numberInSurah,
                            info.currentIndex + 1,
                            info.mediaCount.coerceAtLeast(1),
                        ),
                        color = colors.onEmerald.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            PremiumAudioWaveform(
                progress = info.progressFraction ?: 0f,
                active = isPlayingOrBuffering,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            )

            AudioProgressControl(
                info = info,
                onSeek = onSeek,
            )

            AudioPlaybackStatus(state = uiState.playbackState)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AudioTransportButton(
                    icon = NoorLineIconType.Previous,
                    contentDescription = stringResource(R.string.audio_previous_ayah),
                    enabled = info.canSkipPrevious,
                    testTag = AudioTestTags.PREVIOUS,
                    onClick = onPrevious,
                )
                AudioMainTransportButton(
                    state = uiState.playbackState,
                    queueIsSelected = uiState.isSelectedQueueActive,
                    enabled = uiState.canPlaySelectedSurah,
                    onClick = onPlayPause,
                )
                AudioTransportButton(
                    icon = NoorLineIconType.Next,
                    contentDescription = stringResource(R.string.audio_next_ayah),
                    enabled = info.canSkipNext,
                    testTag = AudioTestTags.NEXT,
                    onClick = onNext,
                )
                AudioTransportButton(
                    icon = NoorLineIconType.Stop,
                    contentDescription = stringResource(R.string.audio_stop),
                    enabled = uiState.playbackState !is QuranPlaybackState.Idle,
                    testTag = AudioTestTags.STOP,
                    onClick = onStop,
                )
            }

            NoorOutlinedButton(
                text = stringResource(R.string.audio_open_in_reader),
                onClick = onOpenReader,
                enabled = uiState.selectedSurah != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(minHeight = 48.dp)
                    .testTag(AudioTestTags.OPEN_READER),
                leadingIcon = {
                    NoorLineIcon(
                        type = NoorLineIconType.BookOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.emerald,
                    )
                },
            )
        }
    }
}

@Composable
private fun AudioProgressControl(
    info: QuranPlaybackInfo,
    onSeek: (Long) -> Unit,
) {
    val colors = NoorDesignSystem.colors
    val duration = info.durationMs
    var sliderValue by remember(info.mediaId) { mutableFloatStateOf(info.progressFraction ?: 0f) }
    var dragging by remember(info.mediaId) { mutableStateOf(false) }

    LaunchedEffect(info.positionMs, info.durationMs, dragging) {
        if (!dragging) sliderValue = info.progressFraction ?: 0f
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (duration > 0L) {
            Slider(
                value = sliderValue,
                onValueChange = {
                    dragging = true
                    sliderValue = it
                },
                onValueChangeFinished = {
                    dragging = false
                    onSeek((duration * sliderValue).roundToLong())
                },
                valueRange = 0f..1f,
            )
        } else {
            AudioProgressTrack(
                progress = info.progressFraction ?: 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(4.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatPlaybackTime(if (dragging) (duration * sliderValue).roundToLong() else info.positionMs),
                color = colors.onEmerald.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = formatPlaybackTime(duration),
                color = colors.onEmerald.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun AudioProgressTrack(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Canvas(modifier = modifier) {
        drawRoundRect(
            color = colors.onEmerald.copy(alpha = 0.18f),
            cornerRadius = CornerRadius(size.height / 2f),
        )
        drawRoundRect(
            color = colors.goldHighlight,
            size = androidx.compose.ui.geometry.Size(
                width = size.width * progress.coerceIn(0f, 1f),
                height = size.height,
            ),
            cornerRadius = CornerRadius(size.height / 2f),
        )
    }
}

@Composable
private fun PremiumAudioWaveform(
    progress: Float,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Canvas(modifier = modifier) {
        val bars = 31
        val gap = size.width / (bars * 2f)
        val barWidth = gap.coerceAtLeast(2.dp.toPx())
        val activeLimit = (bars * progress.coerceIn(0f, 1f)).roundToLong().toInt()
        repeat(bars) { index ->
            val normalized = index.toFloat() / (bars - 1).coerceAtLeast(1)
            val wave = 0.28f + (abs(sin(normalized * Math.PI * 4.0).toFloat()) * 0.58f)
            val height = size.height * wave
            val x = gap + index * (barWidth + gap)
            val color = when {
                !active -> colors.onEmerald.copy(alpha = 0.28f)
                index <= activeLimit -> colors.goldHighlight
                else -> colors.onEmerald.copy(alpha = 0.34f)
            }
            drawLine(
                color = color,
                start = Offset(x, (size.height - height) / 2f),
                end = Offset(x, (size.height + height) / 2f),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun AudioMainTransportButton(
    state: QuranPlaybackState,
    queueIsSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    val isActive = queueIsSelected &&
        (state is QuranPlaybackState.Playing || state is QuranPlaybackState.Buffering)
    Surface(
        shape = CircleShape,
        color = colors.goldHighlight,
        contentColor = colors.emeraldDeep,
        shadowElevation = 8.dp,
        modifier = Modifier.alpha(if (enabled) 1f else 0.45f),
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(68.dp)
                .testTag(AudioTestTags.PLAY_PAUSE),
        ) {
            if (queueIsSelected && state is QuranPlaybackState.Buffering) {
                NoorLoadingIndicator(
                    color = colors.emeraldDeep,
                    size = 28.dp,
                    strokeWidth = 3.dp,
                )
            } else {
                NoorLineIcon(
                    type = if (isActive) NoorLineIconType.Pause else NoorLineIconType.Play,
                    contentDescription = stringResource(
                        if (isActive) R.string.audio_pause else R.string.audio_play,
                    ),
                    modifier = Modifier.size(30.dp),
                    tint = colors.emeraldDeep,
                )
            }
        }
    }
}

@Composable
private fun AudioTransportButton(
    icon: NoorLineIconType,
    contentDescription: String,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(52.dp)
            .testTag(testTag),
    ) {
        NoorLineIcon(
            type = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(25.dp)
                .alpha(if (enabled) 1f else 0.36f),
            tint = colors.onEmerald,
        )
    }
}

@Composable
private fun AudioPlaybackStatus(state: QuranPlaybackState) {
    val colors = NoorDesignSystem.colors
    val resource = when (state) {
        QuranPlaybackState.Idle -> R.string.audio_idle
        QuranPlaybackState.Buffering -> R.string.audio_buffering
        is QuranPlaybackState.Playing -> R.string.audio_playing
        QuranPlaybackState.Paused -> R.string.audio_paused
        QuranPlaybackState.Error -> R.string.audio_error
    }
    Text(
        text = stringResource(resource),
        color = if (state is QuranPlaybackState.Error) {
            MaterialTheme.colorScheme.error
        } else {
            colors.onEmerald.copy(alpha = 0.78f)
        },
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
internal fun ReciterSelector(
    selectedReciter: Reciter,
    isUpdating: Boolean,
    onSelect: (Reciter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.audio_choose_reciter),
                color = colors.onEmerald,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (isUpdating) {
                NoorLoadingIndicator(
                    color = colors.goldHighlight,
                    size = 20.dp,
                    strokeWidth = 2.dp,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
        ) {
            Reciter.entries.forEach { reciter ->
                ReciterOption(
                    reciter = reciter,
                    selected = selectedReciter == reciter,
                    enabled = !isUpdating,
                    onClick = { onSelect(reciter) },
                )
            }
        }
    }
}

@Composable
private fun ReciterOption(
    reciter: Reciter,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Surface(
        modifier = Modifier
            .sizeIn(minWidth = 158.dp, minHeight = 56.dp)
            .semantics { this.selected = selected }
            .testTag(AudioTestTags.reciter(reciter))
            .clickable(
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.goldHighlight else colors.ivory,
        contentColor = colors.onIvory,
        border = BorderStroke(
            1.dp,
            if (selected) colors.goldHighlight else colors.mutedGold.copy(alpha = 0.55f),
        ),
        shadowElevation = if (selected) 6.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NoorLineIcon(
                type = NoorLineIconType.Headphones,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colors.emerald,
            )
            Text(
                text = reciterLabel(reciter),
                color = colors.onIvory,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun AudioSurahListHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(R.string.audio_surah_list_title),
                color = colors.onEmerald,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = pluralStringResource(R.plurals.audio_surah_count, count, count),
                color = colors.goldHighlight,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        HorizontalDivider(
            modifier = Modifier.width(92.dp),
            color = colors.mutedGold.copy(alpha = 0.7f),
        )
    }
}

@Composable
internal fun AudioSurahCard(
    surah: Surah,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .testTag(AudioTestTags.surah(surah.number)),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.ivoryElevated else colors.ivory,
        ),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) colors.goldHighlight else colors.mutedGold.copy(alpha = 0.42f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 7.dp else 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NoorDesignSystem.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) colors.emerald else colors.emerald.copy(alpha = 0.08f),
                contentColor = if (selected) colors.onEmerald else colors.emerald,
                border = BorderStroke(1.dp, colors.mutedGold.copy(alpha = 0.7f)),
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = surah.number.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = stringResource(R.string.audio_surah_title, surah.nameArabic),
                    color = colors.onIvory,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        R.string.audio_surah_metadata,
                        surah.ayahCount,
                        surah.startPage,
                    ),
                    color = colors.onIvoryMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            NoorLineIcon(
                type = if (selected) NoorLineIconType.Play else NoorLineIconType.Headphones,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colors.emerald,
            )
        }
    }
}

@Composable
internal fun AudioStatusPanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = BorderStroke(1.dp, colors.mutedGold.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NoorDesignSystem.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
        ) {
            Surface(
                shape = CircleShape,
                color = colors.emerald.copy(alpha = 0.10f),
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        NoorLoadingIndicator(
                            color = colors.emerald,
                            size = 30.dp,
                            strokeWidth = 3.dp,
                        )
                    } else {
                        NoorLineIcon(
                            type = NoorLineIconType.Headphones,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = colors.emerald,
                        )
                    }
                }
            }
            Text(
                text = title,
                color = colors.onIvory,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = body,
                color = colors.onIvoryMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                NoorFilledButton(
                    text = actionLabel,
                    onClick = onAction,
                    modifier = Modifier
                        .sizeIn(minHeight = 48.dp)
                        .testTag(AudioTestTags.RETRY),
                )
            }
        }
    }
}

@Composable
internal fun reciterLabel(reciter: Reciter): String = stringResource(
    when (reciter) {
        Reciter.ALAFASY -> R.string.reciter_alafasy
        Reciter.HUSARY -> R.string.reciter_husary
        Reciter.MINSHAWI -> R.string.reciter_minshawi
    },
)

private fun formatPlaybackTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1_000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}
