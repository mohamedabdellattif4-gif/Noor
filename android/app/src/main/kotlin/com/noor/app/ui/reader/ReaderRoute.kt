package com.noor.app.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.R
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorFilledButton
import com.noor.core.designsystem.component.NoorLoadingScreen
import com.noor.core.designsystem.component.NoorPatternBackground
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.core.media.QuranPlaybackState
import com.noor.domain.model.Ayah
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun ReaderRoute(
    onBack: () -> Unit,
    onOpenTafsir: (Ayah) -> Unit,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val bookmarkError = stringResource(R.string.reader_bookmark_update_error)
    val textScaleError = stringResource(R.string.reader_text_scale_update_error)
    val progressError = stringResource(R.string.reader_progress_update_error)

    LaunchedEffect(viewModel, bookmarkError, textScaleError, progressError) {
        viewModel.events.collect { event ->
            snackbarHostState.showSnackbar(
                when (event) {
                    ReaderEvent.BookmarkUpdateFailed -> bookmarkError
                    ReaderEvent.TextScaleUpdateFailed -> textScaleError
                    ReaderEvent.ReadingProgressUpdateFailed -> progressError
                },
            )
        }
    }

    ReaderScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onToggleBookmark = viewModel::toggleBookmark,
        onPlayAyah = viewModel::playAyah,
        onPlaySurah = viewModel::playSurah,
        onTogglePause = viewModel::togglePause,
        onStopAudio = viewModel::stopAudio,
        onSetTextScale = viewModel::setTextScale,
        onAyahVisible = viewModel::markRead,
        onOpenTafsir = { ayah ->
            viewModel.markRead(ayah)
            onOpenTafsir(ayah)
        },
    )
}

@Composable
internal fun ReaderScreen(
    uiState: ReaderUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleBookmark: (Ayah) -> Unit,
    onPlayAyah: (Ayah) -> Unit,
    onPlaySurah: () -> Unit,
    onTogglePause: () -> Unit,
    onStopAudio: () -> Unit,
    onSetTextScale: (Float) -> Unit,
    onAyahVisible: (Ayah) -> Unit,
    onOpenTafsir: (Ayah) -> Unit,
) {
    val listState = rememberLazyListState()
    var showTextScaleDialog by rememberSaveable { mutableStateOf(false) }
    val currentAyah by remember(listState, uiState.ayahs, uiState.targetAyahId) {
        derivedStateOf {
            val visibleAyahId = listState.layoutInfo.visibleItemsInfo
                .firstOrNull { item -> item.key is Int }
                ?.key as? Int
            uiState.ayahs.firstOrNull { it.id == visibleAyahId }
                ?: uiState.targetAyahId?.let { targetId ->
                    uiState.ayahs.firstOrNull { it.id == targetId }
                }
                ?: uiState.ayahs.firstOrNull()
        }
    }
    val topAyah = currentAyah ?: uiState.ayahs.firstOrNull()
    val topJuz = topAyah?.juzNumber ?: 1
    val topPage = topAyah?.pageNumber ?: uiState.surah?.startPage ?: 1

    LaunchedEffect(uiState.targetAyahId, uiState.ayahs) {
        val targetId = uiState.targetAyahId ?: return@LaunchedEffect
        val index = uiState.ayahs.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            listState.scrollToItem(index + READER_HEADER_ITEMS)
        }
    }

    LaunchedEffect(listState, uiState.ayahs) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .firstOrNull { item -> item.key is Int }
                ?.key as? Int
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collectLatest { ayahId ->
                delay(LAST_READ_DEBOUNCE_MS)
                uiState.ayahs.firstOrNull { it.id == ayahId }?.let(onAyahVisible)
            }
    }

    Scaffold(
        containerColor = NoorDesignSystem.colors.emeraldDeep,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ReaderTopBar(
                surahName = uiState.surah?.nameArabic ?: stringResource(R.string.reader),
                juzNumber = topJuz,
                pageNumber = topPage,
                onBack = onBack,
            )
        },
        bottomBar = {
            if (!uiState.isLoading && !uiState.hasError) {
                currentAyah?.let { ayah ->
                    ReaderBottomBar(
                        currentAyah = ayah,
                        bookmarked = ayah.id in uiState.bookmarkedAyahIds,
                        playbackState = uiState.playbackState,
                        currentMediaId = uiState.playbackInfo.mediaId,
                        bookmarkUpdateInProgress = ayah.id in uiState.pendingBookmarkAyahIds,
                        onToggleBookmark = { onToggleBookmark(ayah) },
                        onOpenTafsir = { onOpenTafsir(ayah) },
                        onListen = {
                            when (resolveReaderListenAction(
                                playbackState = uiState.playbackState,
                                currentMediaId = uiState.playbackInfo.mediaId,
                                currentAyahId = ayah.id,
                            )) {
                                ReaderListenAction.PLAY_CURRENT -> onPlayAyah(ayah)
                                ReaderListenAction.TOGGLE_PAUSE -> onTogglePause()
                            }
                        },
                        onTextSize = { showTextScaleDialog = true },
                        onStopAudio = onStopAudio,
                    )
                }
            }
        },
    ) { scaffoldPadding ->
        when {
            uiState.isLoading -> NoorLoadingScreen(modifier = Modifier.padding(scaffoldPadding))
            uiState.hasError -> ReaderError(
                modifier = Modifier.padding(scaffoldPadding),
                onRetry = onRetry,
                onBack = onBack,
            )
            else -> NoorPatternBackground {
                NoorContentContainer(modifier = Modifier.padding(scaffoldPadding)) {
                    ReaderReadingSurface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = NoorDesignSystem.spacing.small,
                                top = NoorDesignSystem.spacing.small,
                                end = NoorDesignSystem.spacing.small,
                                bottom = NoorDesignSystem.spacing.extraSmall,
                            ),
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = NoorDesignSystem.spacing.small,
                                bottom = NoorDesignSystem.spacing.large,
                            ),
                        ) {
                            item(key = "reader_opening", contentType = "reader_header") {
                                ReaderOpeningHeader(
                                    showBasmala = uiState.surah?.number?.let(::shouldShowReaderBasmala) == true,
                                    onPlaySurah = onPlaySurah,
                                )
                            }
                            items(
                                count = uiState.ayahs.size,
                                key = { index -> uiState.ayahs[index].id },
                                contentType = { "ayah" },
                            ) { index ->
                                val ayah = uiState.ayahs[index]
                                ReaderAyahRow(
                                    ayah = ayah,
                                    textScale = uiState.textScale,
                                    bookmarked = ayah.id in uiState.bookmarkedAyahIds,
                                    playing = isPlayingAyah(uiState.playbackState, ayah.id),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTextScaleDialog) {
        ReaderTextScaleDialog(
            initialScale = uiState.textScale,
            onDismiss = { showTextScaleDialog = false },
            onConfirm = onSetTextScale,
        )
    }
}

@Composable
private fun ReaderError(modifier: Modifier, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(NoorDesignSystem.spacing.large),
        verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.reader_load_error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
        NoorFilledButton(text = stringResource(R.string.retry), onClick = onRetry)
        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}

private fun isPlayingAyah(state: QuranPlaybackState, ayahId: Int): Boolean =
    state is QuranPlaybackState.Playing && state.mediaId == "ayah:$ayahId"

private const val READER_HEADER_ITEMS = 1
private const val LAST_READ_DEBOUNCE_MS = 750L
