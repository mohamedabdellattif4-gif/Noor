package com.noor.app.ui.search

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.noor.app.R
import com.noor.core.designsystem.component.NoorFilledButton
import com.noor.core.designsystem.component.NoorLineIcon
import com.noor.core.designsystem.component.NoorLineIconType
import com.noor.core.designsystem.component.NoorLoadingIndicator
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.AyahSearchResult

internal object SearchTestTags {
    const val QUERY_FIELD = "search_query_field"
    const val CLEAR_QUERY = "search_clear_query"
    const val RETRY = "search_retry"
    const val RESULTS = "search_results"
    fun result(ayahId: Int): String = "search_result_$ayahId"
}

@Composable
internal fun SearchTopBar(
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
                    text = stringResource(R.string.search_screen_title),
                    color = colors.onEmerald,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.search_screen_subtitle),
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
internal fun PremiumSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    onSearchAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .testTag(SearchTestTags.QUERY_FIELD),
        placeholder = {
            Text(
                text = stringResource(R.string.search_hint),
                color = colors.onIvoryMuted,
            )
        },
        leadingIcon = {
            NoorLineIcon(
                type = NoorLineIconType.Search,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = colors.emerald,
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.testTag(SearchTestTags.CLEAR_QUERY),
                ) {
                    NoorLineIcon(
                        type = NoorLineIconType.Close,
                        contentDescription = stringResource(R.string.search_clear),
                        modifier = Modifier.size(20.dp),
                        tint = colors.emerald,
                    )
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.ivory,
            unfocusedContainerColor = colors.ivory,
            disabledContainerColor = colors.ivory.copy(alpha = 0.72f),
            focusedTextColor = colors.onIvory,
            unfocusedTextColor = colors.onIvory,
            cursorColor = colors.emerald,
            focusedBorderColor = colors.mutedGold,
            unfocusedBorderColor = colors.mutedGold.copy(alpha = 0.62f),
            focusedPlaceholderColor = colors.onIvoryMuted,
            unfocusedPlaceholderColor = colors.onIvoryMuted,
        ),
    )
}

@Composable
internal fun SearchStatusPanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: NoorLineIconType = NoorLineIconType.Search,
    isLoading: Boolean = false,
    actionLabel: String? = null,
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
                            type = icon,
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
                        .testTag(SearchTestTags.RETRY),
                )
            }
        }
    }
}

@Composable
internal fun SearchResultsHeader(
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
                text = stringResource(R.string.search_results_title),
                color = colors.onEmerald,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = countLabel,
                color = colors.goldHighlight,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        DecorativeSearchDivider(modifier = Modifier.size(width = 74.dp, height = 18.dp))
    }
}

@Composable
internal fun SearchResultCard(
    result: AyahSearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(SearchTestTags.result(result.ayah.id)),
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
                SearchAyahMedallion(
                    number = result.ayah.numberInSurah,
                    modifier = Modifier.size(48.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.surah_and_ayah,
                            result.surahNameArabic,
                            result.ayah.numberInSurah,
                        ),
                        color = colors.emerald,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.search_result_position,
                            result.ayah.juzNumber,
                            result.ayah.pageNumber,
                        ),
                        color = colors.onIvoryMuted,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                NoorLineIcon(
                    type = NoorLineIconType.Arrow,
                    contentDescription = stringResource(R.string.search_open_result),
                    modifier = Modifier.size(22.dp),
                    tint = colors.mutedGold,
                )
            }
            Spacer(modifier = Modifier.height(NoorDesignSystem.spacing.extraSmall))
            Text(
                text = result.ayah.textUthmani,
                color = colors.onIvory,
                style = NoorDesignSystem.extendedTypography.quranMedium,
                textAlign = TextAlign.Start,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SearchAyahMedallion(
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
private fun DecorativeSearchDivider(modifier: Modifier = Modifier) {
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
