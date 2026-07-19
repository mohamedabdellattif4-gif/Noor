package com.noor.app.ui.adhkar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.R
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorFilledButton
import com.noor.core.designsystem.component.NoorLineIcon
import com.noor.core.designsystem.component.NoorLineIconType
import com.noor.core.designsystem.component.NoorLoadingScreen
import com.noor.core.designsystem.component.NoorPatternBackground
import com.noor.core.designsystem.component.NoorTopAppBar
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.AdhkarCategory
import com.noor.domain.model.Dhikr

@Composable
fun AdhkarRoute(
    onBack: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenReminders: () -> Unit,
    viewModel: AdhkarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updateError = stringResource(R.string.adhkar_update_error)
    LaunchedEffect(viewModel, updateError) {
        viewModel.events.collect { snackbarHostState.showSnackbar(updateError) }
    }
    AdhkarScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onOpenTasbih = onOpenTasbih,
        onOpenReminders = onOpenReminders,
        onQueryChange = viewModel::setQuery,
        onCategorySelected = viewModel::selectCategory,
        onToggleFavoritesOnly = viewModel::toggleFavoritesOnly,
        onToggleFavorite = viewModel::toggleFavorite,
        onIncrement = viewModel::increment,
        onReset = viewModel::reset,
    )
}

@Composable
internal fun AdhkarScreen(
    uiState: AdhkarUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenReminders: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (AdhkarCategory?) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onToggleFavorite: (Dhikr) -> Unit,
    onIncrement: (Dhikr) -> Unit,
    onReset: (Dhikr) -> Unit,
) {
    NoorPatternBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                NoorTopAppBar(
                    title = stringResource(R.string.adhkar_screen_title),
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            NoorContentContainer(modifier = Modifier.padding(padding)) {
                if (uiState.isLoading) {
                    NoorLoadingScreen(modifier = Modifier.fillMaxSize())
                    return@NoorContentContainer
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(AdhkarTestTags.CONTENT),
                    contentPadding = PaddingValues(NoorDesignSystem.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                ) {
                    item(key = "intro", contentType = "intro") {
                        Text(
                            text = stringResource(R.string.adhkar_screen_subtitle),
                            modifier = Modifier.fillMaxWidth(),
                            color = NoorDesignSystem.colors.onEmerald,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    item(key = "tools", contentType = "tools") {
                        AdhkarToolsRow(
                            onOpenTasbih = onOpenTasbih,
                            onOpenReminders = onOpenReminders,
                        )
                    }
                    item(key = "search", contentType = "search") {
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = onQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.adhkar_search)) },
                            singleLine = true,
                        )
                    }
                    item(key = "categories", contentType = "categories") {
                        AdhkarCategoryRow(
                            selected = uiState.selectedCategory,
                            onSelected = onCategorySelected,
                        )
                    }
                    item(key = "favorites", contentType = "favorites") {
                        FilterChip(
                            selected = uiState.favoritesOnly,
                            onClick = onToggleFavoritesOnly,
                            label = { Text(stringResource(R.string.adhkar_favorites_only)) },
                            leadingIcon = {
                                NoorLineIcon(
                                    type = NoorLineIconType.Heart,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                    }
                    if (uiState.hasError) {
                        item(key = "error", contentType = "error") { AdhkarErrorCard() }
                    } else if (uiState.items.isEmpty()) {
                        item(key = "empty", contentType = "empty") { AdhkarEmptyCard() }
                    } else {
                        items(
                            items = uiState.items,
                            key = Dhikr::id,
                            contentType = { "dhikr" },
                        ) { item ->
                            DhikrCard(
                                item = item,
                                completed = uiState.completedRepeats[item.id] ?: 0,
                                onToggleFavorite = { onToggleFavorite(item) },
                                onIncrement = { onIncrement(item) },
                                onReset = { onReset(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdhkarToolsRow(onOpenTasbih: () -> Unit, onOpenReminders: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
    ) {
        AdhkarToolCard(
            title = stringResource(R.string.tasbih_title),
            icon = NoorLineIconType.More,
            onClick = onOpenTasbih,
            modifier = Modifier.weight(1f),
        )
        AdhkarToolCard(
            title = stringResource(R.string.adhkar_reminders_title),
            icon = NoorLineIconType.Bell,
            onClick = onOpenReminders,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AdhkarToolCard(
    title: String,
    icon: NoorLineIconType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NoorDesignSystem.colors
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = BorderStroke(1.dp, colors.mutedGold),
    ) {
        Row(
            modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NoorLineIcon(icon, null, Modifier.size(24.dp), colors.emerald)
            Text(title, color = colors.onIvory, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun AdhkarCategoryRow(
    selected: AdhkarCategory?,
    onSelected: (AdhkarCategory?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small)) {
        item(key = "all") {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.adhkar_category_all)) },
            )
        }
        items(AdhkarCategory.entries, key = AdhkarCategory::key) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(category) },
                label = { Text(categoryLabel(category)) },
            )
        }
    }
}

@Composable
private fun categoryLabel(category: AdhkarCategory): String = stringResource(
    when (category) {
        AdhkarCategory.MORNING -> R.string.adhkar_category_morning
        AdhkarCategory.EVENING -> R.string.adhkar_category_evening
        AdhkarCategory.AFTER_PRAYER -> R.string.adhkar_category_after_prayer
        AdhkarCategory.SLEEP -> R.string.adhkar_category_sleep
        AdhkarCategory.WAKING -> R.string.adhkar_category_waking
        AdhkarCategory.GENERAL -> R.string.adhkar_category_general
    },
)

@Composable
private fun DhikrCard(
    item: Dhikr,
    completed: Int,
    onToggleFavorite: () -> Unit,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    val isComplete = completed >= item.repeatCount
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${AdhkarTestTags.ITEM_PREFIX}${item.id}"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = BorderStroke(1.dp, colors.mutedGold.copy(alpha = 0.7f)),
    ) {
        Column(
            modifier = Modifier.padding(NoorDesignSystem.spacing.large),
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = colors.emeraldDeep,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = categoryLabel(item.category),
                        color = colors.onIvoryMuted,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                IconToggleButton(
                    checked = item.isFavorite,
                    onCheckedChange = { onToggleFavorite() },
                    modifier = Modifier.testTag("${AdhkarTestTags.FAVORITE_PREFIX}${item.id}"),
                ) {
                    NoorLineIcon(
                        type = NoorLineIconType.Heart,
                        contentDescription = stringResource(
                            if (item.isFavorite) {
                                R.string.adhkar_remove_favorite
                            } else {
                                R.string.adhkar_add_favorite
                            },
                        ),
                        modifier = Modifier.size(25.dp),
                        tint = if (item.isFavorite) colors.goldHighlight else colors.onIvoryMuted,
                    )
                }
            }
            Text(
                text = item.textArabic,
                modifier = Modifier.fillMaxWidth(),
                color = colors.onIvory,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colors.emeraldDeep.copy(alpha = 0.08f),
            ) {
                Text(
                    text = item.reference,
                    modifier = Modifier.padding(NoorDesignSystem.spacing.small),
                    color = colors.onIvoryMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NoorFilledButton(
                    text = if (isComplete) {
                        stringResource(R.string.adhkar_completed)
                    } else {
                        stringResource(R.string.adhkar_count_progress, completed, item.repeatCount)
                    },
                    onClick = onIncrement,
                    enabled = !isComplete,
                )
                TextButton(onClick = onReset, enabled = completed > 0) {
                    Text(stringResource(R.string.adhkar_reset_count))
                }
            }
        }
    }
}


@Composable
private fun AdhkarErrorCard() {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = BorderStroke(1.dp, colors.mutedGold),
    ) {
        Text(
            text = stringResource(R.string.adhkar_load_error),
            modifier = Modifier.padding(NoorDesignSystem.spacing.large),
            color = colors.onIvory,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AdhkarEmptyCard() {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
    ) {
        Column(
            modifier = Modifier.padding(NoorDesignSystem.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
        ) {
            Text(
                text = stringResource(R.string.adhkar_empty_title),
                color = colors.emeraldDeep,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.adhkar_empty_body),
                color = colors.onIvoryMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

internal object AdhkarTestTags {
    const val CONTENT = "adhkar_content"
    const val ITEM_PREFIX = "adhkar_item_"
    const val FAVORITE_PREFIX = "adhkar_favorite_"
}
