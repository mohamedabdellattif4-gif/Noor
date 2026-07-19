package com.noor.app.ui.home

import androidx.activity.compose.ReportDrawnWhen
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.Ayah
import com.noor.domain.model.Reciter
import com.noor.domain.model.RevelationType
import com.noor.domain.model.Surah
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    onOpenSurah: (Int, Int?) -> Unit,
    onOpenTafsir: (Ayah) -> Unit,
    onSearch: () -> Unit,
    onBookmarks: () -> Unit,
    onAudio: () -> Unit,
    onAdhkar: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportDrawnWhen { !uiState.isLoading }
    HomeScreen(
        uiState = uiState,
        onOpenSurah = onOpenSurah,
        onOpenTafsir = onOpenTafsir,
        onSearch = onSearch,
        onBookmarks = onBookmarks,
        onAudio = onAudio,
        onAdhkar = onAdhkar,
        onSettings = onSettings,
        onRetry = viewModel::retry,
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onOpenSurah: (Int, Int?) -> Unit,
    onOpenTafsir: (Ayah) -> Unit,
    onSearch: () -> Unit,
    onBookmarks: () -> Unit,
    onAudio: () -> Unit,
    onAdhkar: () -> Unit,
    onSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val primarySurah = uiState.primarySurah
    val primaryAyah = uiState.continueAyah
    val openPrimaryReader: () -> Unit = {
        primarySurah?.let { surah ->
            onOpenSurah(surah.number, uiState.primaryAyahId)
        }
    }

    NoorPatternBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NoorHomeBottomBar(
                    onHome = {
                        coroutineScope.launch { listState.animateScrollToItem(0) }
                    },
                    onSearch = onSearch,
                    onBookmarks = onBookmarks,
                    onSettings = onSettings,
                )
            },
        ) { scaffoldPadding ->
            when {
                uiState.isLoading -> NoorLoadingScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                )

                uiState.hasError -> HomeLoadError(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                    onRetry = onRetry,
                )

                else -> NoorContentContainer {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = HOME_CONTENT_MAX_WIDTH),
                        contentPadding = PaddingValues(
                            start = NoorDesignSystem.spacing.medium,
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
                                NoorDesignSystem.spacing.small,
                            end = NoorDesignSystem.spacing.medium,
                            bottom = scaffoldPadding.calculateBottomPadding() +
                                NoorDesignSystem.spacing.large,
                        ),
                        verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                    ) {
                        item(key = "header", contentType = "header") {
                            NoorHomeHeader(onSettings = onSettings)
                        }
                        item(key = "memorial", contentType = "memorial") {
                            MemorialCard()
                        }
                        item(key = "continue", contentType = "continue") {
                            ContinueReadingCard(
                                surah = primarySurah,
                                ayah = primaryAyah,
                                onClick = openPrimaryReader,
                            )
                        }
                        item(key = "daily_progress", contentType = "daily_progress") {
                            DailyReadingRow(
                                pagesRead = uiState.dailyPagesRead,
                                goalPages = uiState.dailyGoalPages,
                                streakDays = uiState.readingStreakDays,
                                onContinue = openPrimaryReader,
                            )
                        }
                        item(key = "quick_access", contentType = "quick_access") {
                            SectionTitle(text = stringResource(R.string.quick_access))
                            QuickAccessGrid(
                                actions = listOf(
                                    HomeQuickAction(
                                        title = R.string.quran,
                                        icon = NoorLineIconType.BookOpen,
                                        onClick = openPrimaryReader,
                                        enabled = primarySurah != null,
                                    ),
                                    HomeQuickAction(
                                        title = R.string.surah_list,
                                        icon = NoorLineIconType.Surahs,
                                        onClick = {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(SURAH_LIST_ITEM_INDEX)
                                            }
                                        },
                                    ),
                                    HomeQuickAction(
                                        title = R.string.search,
                                        icon = NoorLineIconType.Search,
                                        onClick = onSearch,
                                    ),
                                    HomeQuickAction(
                                        title = R.string.listen,
                                        icon = NoorLineIconType.Headphones,
                                        onClick = onAudio,
                                    ),
                                    HomeQuickAction(
                                        title = R.string.bookmarks,
                                        icon = NoorLineIconType.Bookmark,
                                        onClick = onBookmarks,
                                    ),
                                    HomeQuickAction(
                                        title = R.string.tafsir,
                                        icon = NoorLineIconType.Document,
                                        onClick = { primaryAyah?.let(onOpenTafsir) },
                                        enabled = primaryAyah != null,
                                    ),
                                    HomeQuickAction(
                                        title = R.string.adhkar,
                                        icon = NoorLineIconType.Heart,
                                        onClick = onAdhkar,
                                    ),
                                ),
                            )
                        }
                        item(key = "recent_reciter", contentType = "recent_reciter") {
                            RecentReciterCard(
                                reciter = uiState.selectedReciter,
                                onClick = onAudio,
                                enabled = true,
                            )
                        }
                        item(key = "surah_heading", contentType = "surah_heading") {
                            SectionTitle(text = stringResource(R.string.surah_list))
                        }
                        items(
                            count = uiState.surahs.size,
                            key = { index -> uiState.surahs[index].number },
                            contentType = { "surah" },
                        ) { index ->
                            PremiumSurahCard(
                                surah = uiState.surahs[index],
                                onClick = {
                                    onOpenSurah(uiState.surahs[index].number, null)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoorHomeHeader(onSettings: () -> Unit) {
    val colors = NoorDesignSystem.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center,
            ) {
                NoorLineIcon(
                    type = NoorLineIconType.Bell,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colors.goldHighlight.copy(alpha = 0.55f),
                )
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.brand_name_arabic),
                    color = colors.goldHighlight,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.app_name_english),
                    color = colors.onEmerald.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            IconButton(
                onClick = onSettings,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                NoorLineIcon(
                    type = NoorLineIconType.Settings,
                    contentDescription = stringResource(R.string.settings),
                    modifier = Modifier.size(24.dp),
                    tint = colors.goldHighlight,
                )
            }
        }
        Text(
            text = stringResource(R.string.islamic_greeting),
            modifier = Modifier.padding(top = NoorDesignSystem.spacing.extraSmall),
            color = colors.onEmerald.copy(alpha = 0.92f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MemorialCard() {
    val colors = NoorDesignSystem.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.ivory,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.mutedGold),
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = NoorDesignSystem.spacing.medium,
                vertical = NoorDesignSystem.spacing.small,
            ),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NoorMemorialOrnament(
                modifier = Modifier.size(34.dp),
                color = colors.mutedGold,
            )
            Text(
                text = stringResource(R.string.memorial_note),
                modifier = Modifier.weight(1f),
                color = colors.onIvory,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DailyReadingRow(
    pagesRead: Int,
    goalPages: Int,
    streakDays: Int,
    onContinue: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
    ) {
        Card(
            onClick = onContinue,
            modifier = Modifier.weight(2f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = colors.ivory),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                colors.mutedGold.copy(alpha = 0.65f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NoorLineIcon(
                        type = NoorLineIconType.BookOpen,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = colors.emerald,
                    )
                    Text(
                        text = stringResource(R.string.daily_reading),
                        color = colors.onIvory,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                ReadingProgressBar(
                    progress = if (goalPages > 0) pagesRead.toFloat() / goalPages else 0f,
                )
                Text(
                    text = stringResource(
                        R.string.reading_progress_pages,
                        pagesRead,
                        goalPages,
                    ),
                    color = colors.onIvoryMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.large,
            color = colors.ivory,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                colors.mutedGold.copy(alpha = 0.65f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.extraSmall),
            ) {
                Text(
                    text = stringResource(R.string.streak),
                    color = colors.onIvory,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = streakDays.toString(),
                    color = colors.emerald,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.streak_days,
                        streakDays,
                        streakDays,
                    ),
                    color = colors.onIvoryMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun QuickAccessGrid(actions: List<HomeQuickAction>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columnCount = if (maxWidth < 360.dp) 2 else 3
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
        ) {
            actions.chunked(columnCount).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
                ) {
                    rowActions.forEach { action ->
                        QuickAccessCard(
                            action = action,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columnCount - rowActions.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(action: HomeQuickAction, modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Card(
        onClick = action.onClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = action.enabled,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = colors.ivory,
            disabledContainerColor = colors.ivory.copy(alpha = 0.55f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.mutedGold.copy(alpha = if (action.enabled) 0.55f else 0.25f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = NoorDesignSystem.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
        ) {
            NoorLineIcon(
                type = action.icon,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = colors.emerald.copy(alpha = if (action.enabled) 1f else 0.45f),
            )
            Text(
                text = stringResource(action.title),
                color = colors.onIvory.copy(alpha = if (action.enabled) 1f else 0.45f),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val colors = NoorDesignSystem.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = NoorDesignSystem.spacing.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = colors.mutedGold.copy(alpha = 0.7f),
        ) { Box(modifier = Modifier.size(height = 1.dp, width = 1.dp)) }
        Text(
            text = text,
            color = colors.onEmerald,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Surface(
            modifier = Modifier.weight(1f),
            color = colors.mutedGold.copy(alpha = 0.7f),
        ) { Box(modifier = Modifier.size(height = 1.dp, width = 1.dp)) }
    }
}

@Composable
private fun RecentReciterCard(
    reciter: Reciter,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val colors = NoorDesignSystem.colors
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.emeraldElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.mutedGold),
    ) {
        Row(
            modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = colors.goldHighlight,
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    NoorLineIcon(
                        type = NoorLineIconType.Play,
                        contentDescription = stringResource(R.string.listen),
                        modifier = Modifier.size(22.dp),
                        tint = colors.emeraldDeep,
                    )
                }
            }
            ReciterWaveform(
                modifier = Modifier.weight(1f),
                color = colors.goldHighlight,
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.recent_reciter),
                    color = colors.onEmerald.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = reciterName(reciter),
                    color = colors.onEmerald,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = colors.ivory,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.mutedGold),
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    NoorLineIcon(
                        type = NoorLineIconType.Headphones,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colors.emerald,
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumSurahCard(surah: Surah, onClick: () -> Unit) {
    val colors = NoorDesignSystem.colors
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.mutedGold.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = colors.emerald,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.mutedGold),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = surah.number.toString(),
                        color = colors.goldHighlight,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.nameArabic,
                    color = colors.onIvory,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(
                        if (surah.revelationType == RevelationType.MECCAN) {
                            R.string.meccan
                        } else {
                            R.string.medinan
                        },
                    ),
                    color = colors.onIvoryMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = pluralStringResource(R.plurals.ayah_count, surah.ayahCount, surah.ayahCount),
                color = colors.onIvoryMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun NoorHomeBottomBar(
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onBookmarks: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = colors.emerald,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.mutedGold.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NoorDesignSystem.spacing.small),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            HomeBottomItem(
                title = stringResource(R.string.home),
                icon = NoorLineIconType.Home,
                selected = true,
                onClick = onHome,
            )
            HomeBottomItem(
                title = stringResource(R.string.search),
                icon = NoorLineIconType.Search,
                selected = false,
                onClick = onSearch,
            )
            HomeBottomItem(
                title = stringResource(R.string.bookmarks),
                icon = NoorLineIconType.Heart,
                selected = false,
                onClick = onBookmarks,
            )
            HomeBottomItem(
                title = stringResource(R.string.settings),
                icon = NoorLineIconType.More,
                selected = false,
                onClick = onSettings,
            )
        }
    }
}

@Composable
private fun RowScope.HomeBottomItem(
    title: String,
    icon: NoorLineIconType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        color = Color.Transparent,
        contentColor = if (selected) colors.goldHighlight else colors.onEmerald.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier.padding(vertical = NoorDesignSystem.spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.extraSmall),
        ) {
            NoorLineIcon(
                type = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (selected) colors.goldHighlight else colors.onEmerald.copy(alpha = 0.72f),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HomeLoadError(modifier: Modifier, onRetry: () -> Unit) {
    val colors = NoorDesignSystem.colors
    Column(
        modifier = modifier.padding(NoorDesignSystem.spacing.large),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.quran_load_error),
            color = colors.onEmerald,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        NoorFilledButton(
            text = stringResource(R.string.retry),
            onClick = onRetry,
            modifier = Modifier.padding(top = NoorDesignSystem.spacing.medium),
        )
    }
}

@Composable
private fun reciterName(reciter: Reciter): String = stringResource(
    when (reciter) {
        Reciter.ALAFASY -> R.string.reciter_alafasy
        Reciter.HUSARY -> R.string.reciter_husary
        Reciter.MINSHAWI -> R.string.reciter_minshawi
    },
)

private data class HomeQuickAction(
    @StringRes val title: Int,
    val icon: NoorLineIconType,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

private val HOME_CONTENT_MAX_WIDTH = 680.dp
private const val SURAH_LIST_ITEM_INDEX = 6
