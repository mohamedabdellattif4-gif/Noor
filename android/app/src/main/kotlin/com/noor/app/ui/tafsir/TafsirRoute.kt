package com.noor.app.ui.tafsir

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun TafsirRoute(
    onBack: () -> Unit,
    viewModel: TafsirViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshError = stringResource(R.string.tafsir_refresh_error)

    LaunchedEffect(viewModel, refreshError) {
        viewModel.events.collect { event ->
            when (event) {
                TafsirEvent.RefreshFailed -> snackbarHostState.showSnackbar(refreshError)
            }
        }
    }

    TafsirScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
    )
}

@Composable
internal fun TafsirScreen(
    uiState: TafsirUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
) {
    NoorPatternBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TafsirTopBar(
                    isRefreshing = uiState.isRefreshing,
                    canRefresh = uiState.ayah != null && uiState.tafsir != null,
                    onBack = onBack,
                    onRefresh = onRefresh,
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { padding ->
            NoorContentContainer(modifier = Modifier.padding(padding)) {
                when {
                    uiState.isLoading -> TafsirStatusPanel(
                        title = stringResource(R.string.tafsir_loading_title),
                        body = stringResource(R.string.tafsir_loading_body),
                        isLoading = true,
                        modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
                    )

                    uiState.hasError -> TafsirStatusPanel(
                        title = stringResource(R.string.tafsir_error_title),
                        body = stringResource(R.string.tafsir_error_body),
                        actionLabel = stringResource(R.string.retry),
                        onAction = onRetry,
                        modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
                    )

                    uiState.ayah != null && uiState.tafsir != null -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(TafsirTestTags.CONTENT),
                        contentPadding = PaddingValues(
                            start = NoorDesignSystem.spacing.medium,
                            top = NoorDesignSystem.spacing.medium,
                            end = NoorDesignSystem.spacing.medium,
                            bottom = 32.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                    ) {
                        item(key = "tafsir_ayah", contentType = "ayah") {
                            TafsirAyahCard(ayah = uiState.ayah)
                        }
                        item(key = "tafsir_explanation", contentType = "explanation") {
                            TafsirExplanationCard(tafsir = uiState.tafsir)
                        }
                        uiState.tafsir.footnotes?.let { footnotes ->
                            item(key = "tafsir_footnotes", contentType = "footnotes") {
                                TafsirFootnotesCard(footnotes = footnotes)
                            }
                        }
                        item(key = "tafsir_source", contentType = "source") {
                            TafsirSourceCard(tafsir = uiState.tafsir)
                        }
                    }

                    else -> TafsirStatusPanel(
                        title = stringResource(R.string.tafsir_error_title),
                        body = stringResource(R.string.tafsir_error_body),
                        actionLabel = stringResource(R.string.retry),
                        onAction = onRetry,
                        modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
                    )
                }
            }
        }
    }
}
