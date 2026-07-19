package com.noor.app.ui.audio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.R
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorPatternBackground
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.Reciter

@Composable
fun AudioRoute(
    onBack: () -> Unit,
    onOpenReader: (Int, Int?) -> Unit,
    viewModel: AudioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val reciterError = stringResource(R.string.audio_reciter_update_error)

    LaunchedEffect(viewModel, reciterError) {
        viewModel.events.collect { event ->
            when (event) {
                AudioEvent.ReciterUpdateFailed -> snackbarHostState.showSnackbar(reciterError)
            }
        }
    }

    AudioScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onSelectSurah = viewModel::selectSurah,
        onSelectReciter = viewModel::setReciter,
        onPlayPause = viewModel::playOrPauseSelectedSurah,
        onPrevious = viewModel::skipToPrevious,
        onNext = viewModel::skipToNext,
        onStop = viewModel::stop,
        onSeek = viewModel::seekTo,
        onOpenReader = {
            uiState.selectedSurah?.let { surah ->
                val ayahId = uiState.playbackInfo.currentAyahId
                    ?.takeIf { id -> uiState.selectedAyahs.any { it.id == id } }
                    ?: uiState.selectedAyahs.firstOrNull()?.id
                onOpenReader(surah.number, ayahId)
            }
        },
    )
}

@Composable
internal fun AudioScreen(
    uiState: AudioUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectSurah: (Int) -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenReader: () -> Unit,
) {
    NoorPatternBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = { AudioTopBar(onBack = onBack) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { padding ->
            NoorContentContainer(modifier = Modifier.padding(padding)) {
                when {
                    uiState.isLoading -> AudioStatusPanel(
                        title = stringResource(R.string.audio_loading_title),
                        body = stringResource(R.string.audio_loading_body),
                        isLoading = true,
                        modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
                    )

                    uiState.hasError -> AudioStatusPanel(
                        title = stringResource(R.string.audio_load_error_title),
                        body = stringResource(R.string.audio_load_error_body),
                        actionLabel = stringResource(R.string.retry),
                        onAction = onRetry,
                        modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
                    )

                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(AudioTestTags.SURAH_LIST),
                        contentPadding = PaddingValues(
                            start = NoorDesignSystem.spacing.medium,
                            top = NoorDesignSystem.spacing.medium,
                            end = NoorDesignSystem.spacing.medium,
                            bottom = 32.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                    ) {
                        item(key = "audio_player", contentType = "player") {
                            PremiumAudioPlayerCard(
                                uiState = uiState,
                                onPlayPause = onPlayPause,
                                onPrevious = onPrevious,
                                onNext = onNext,
                                onStop = onStop,
                                onSeek = onSeek,
                                onOpenReader = onOpenReader,
                            )
                        }
                        item(key = "audio_reciters", contentType = "reciters") {
                            ReciterSelector(
                                selectedReciter = uiState.reciter,
                                isUpdating = uiState.isReciterUpdating,
                                onSelect = onSelectReciter,
                            )
                        }
                        item(key = "audio_surah_header", contentType = "header") {
                            AudioSurahListHeader(count = uiState.surahs.size)
                        }
                        items(
                            items = uiState.surahs,
                            key = { it.number },
                            contentType = { "audio_surah" },
                        ) { surah ->
                            AudioSurahCard(
                                surah = surah,
                                selected = surah.number == uiState.selectedSurah?.number,
                                onClick = { onSelectSurah(surah.number) },
                            )
                        }
                    }
                }
            }
        }
    }
}
