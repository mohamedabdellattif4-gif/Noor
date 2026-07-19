package com.noor.app.ui.bookmarks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
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
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.Bookmark

internal object BookmarksTestTags {
    const val LIST = "bookmarks_list"
    const val RETRY = "bookmarks_retry"
    const val EMPTY_ACTION = "bookmarks_empty_action"
    fun item(ayahId: Int): String = "bookmark_item_$ayahId"
    fun remove(ayahId: Int): String = "bookmark_remove_$ayahId"
}

@Composable
internal fun BookmarksTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.emeraldDeep,
        contentColor = colors.onEmerald,
        shadowElevation = 6.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 88.dp)
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
                    .padding(horizontal = 58.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.bookmarks_screen_title),
                    color = colors.onEmerald,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.bookmarks_screen_subtitle),
                    color = colors.goldHighlight,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun BookmarksStatusPanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    actionLabel: String? = null,
    actionTag: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = BorderStroke(1.dp, colors.mutedGold.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
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
                contentColor = colors.emerald,
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
                            type = NoorLineIconType.Bookmark,
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
                        .then(if (actionTag == null) Modifier else Modifier.testTag(actionTag)),
                )
            }
        }
    }
}

@Composable
internal fun BookmarksSummaryHeader(
    countLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.bookmarks_saved_title),
                color = colors.onEmerald,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = countLabel,
                color = colors.goldHighlight,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = stringResource(R.string.bookmarks_sorted_hint),
                color = colors.onEmerald.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        DecorativeBookmarksDivider(modifier = Modifier.size(width = 74.dp, height = 18.dp))
    }
}

@Composable
internal fun BookmarkCard(
    bookmark: Bookmark,
    isRemoving: Boolean,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Card(
        onClick = onOpen,
        modifier = modifier
            .fillMaxWidth()
            .testTag(BookmarksTestTags.item(bookmark.ayah.id)),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = BorderStroke(1.dp, colors.mutedGold.copy(alpha = 0.68f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NoorDesignSystem.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BookmarkAyahMedallion(
                    number = bookmark.ayah.numberInSurah,
                    modifier = Modifier.size(48.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.surah_and_ayah,
                            bookmark.surahNameArabic,
                            bookmark.ayah.numberInSurah,
                        ),
                        color = colors.emerald,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.bookmark_position,
                            bookmark.ayah.juzNumber,
                            bookmark.ayah.pageNumber,
                        ),
                        color = colors.onIvoryMuted,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                IconButton(
                    onClick = onRemove,
                    enabled = !isRemoving,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(BookmarksTestTags.remove(bookmark.ayah.id)),
                ) {
                    if (isRemoving) {
                        val removingDescription = stringResource(R.string.bookmark_removing)
                        Box(
                            modifier = Modifier.semantics {
                                contentDescription = removingDescription
                            },
                        ) {
                            NoorLoadingIndicator(
                                color = colors.emerald,
                                size = 22.dp,
                                strokeWidth = 2.4.dp,
                            )
                        }
                    } else {
                        NoorLineIcon(
                            type = NoorLineIconType.Delete,
                            contentDescription = stringResource(R.string.remove_bookmark),
                            modifier = Modifier.size(22.dp),
                            tint = colors.emerald,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(NoorDesignSystem.spacing.extraSmall))
            Text(
                text = bookmark.ayah.textUthmani,
                color = colors.onIvory,
                style = NoorDesignSystem.extendedTypography.quranMedium,
                textAlign = TextAlign.Start,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onOpen,
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.bookmark_open_ayah),
                        color = colors.emerald,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    NoorLineIcon(
                        type = NoorLineIconType.Arrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.mutedGold,
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkAyahMedallion(
    number: Int,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 1.4.dp.toPx()
            drawCircle(
                color = colors.mutedGold.copy(alpha = 0.18f),
                radius = size.minDimension * 0.47f,
            )
            drawCircle(
                color = colors.mutedGold,
                radius = size.minDimension * 0.40f,
                style = Stroke(width = strokeWidth),
            )
            val canvasCenter = center
            repeat(8) { index ->
                val angle = Math.toRadians(index * 45.0)
                val ornamentCenter = Offset(
                    x = canvasCenter.x + kotlin.math.cos(angle).toFloat() * size.minDimension * 0.45f,
                    y = canvasCenter.y + kotlin.math.sin(angle).toFloat() * size.minDimension * 0.45f,
                )
                drawCircle(
                    color = colors.mutedGold,
                    radius = size.minDimension * 0.035f,
                    center = ornamentCenter,
                )
            }
        }
        Text(
            text = number.toString(),
            color = colors.emeraldDeep,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DecorativeBookmarksDivider(modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Canvas(modifier = modifier) {
        val y = center.y
        drawLine(
            color = colors.mutedGold.copy(alpha = 0.72f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = colors.goldHighlight,
            radius = 3.dp.toPx(),
            center = center,
        )
    }
}
