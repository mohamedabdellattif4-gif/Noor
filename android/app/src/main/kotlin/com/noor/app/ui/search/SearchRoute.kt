package com.noor.app.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.R
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorLineIconType
import com.noor.core.designsystem.component.NoorPatternBackground
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.AyahSearchResult

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onOpenResult: (AyahSearchResult) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onClear = viewModel::clearQuery,
        onRetry = viewModel::retry,
        onBack = onBack,
        onOpenResult = onOpenResult,
    )
}

@Composable
internal fun SearchScreen(
    uiState: SearchUiState,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenResult: (AyahSearchResult) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.query, uiState.hasSearched) {
        if (uiState.hasSearched && uiState.results.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    NoorPatternBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = { SearchTopBar(onBack = onBack) },
        ) { padding ->
            NoorContentContainer(modifier = Modifier.padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .padding(horizontal = NoorDesignSystem.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                ) {
                    PremiumSearchField(
                        query = uiState.query,
                        onQueryChanged = onQueryChanged,
                        onClear = onClear,
                        onSearchAction = { keyboardController?.hide() },
                        modifier = Modifier.padding(top = NoorDesignSystem.spacing.medium),
                    )
                    when {
                        uiState.isSearching -> SearchStatusPanel(
                            title = stringResource(R.string.search_loading_title),
                            body = stringResource(R.string.search_loading_body),
                            isLoading = true,
                        )

                        uiState.hasError -> SearchStatusPanel(
                            title = stringResource(R.string.search_error),
                            body = stringResource(R.string.search_error_body),
                            icon = NoorLineIconType.Search,
                            actionLabel = stringResource(R.string.retry),
                            onAction = onRetry,
                        )

                        uiState.hasSearched && uiState.results.isEmpty() -> SearchStatusPanel(
                            title = stringResource(R.string.no_search_results),
                            body = stringResource(R.string.search_try_different_words),
                            icon = NoorLineIconType.Search,
                        )

                        uiState.results.isNotEmpty() -> LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(SearchTestTags.RESULTS),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
                        ) {
                            item(
                                key = "results_header",
                                contentType = "header",
                            ) {
                                SearchResultsHeader(
                                    countLabel = pluralStringResource(
                                        R.plurals.search_results_count,
                                        uiState.results.size,
                                        uiState.results.size,
                                    ),
                                    modifier = Modifier.padding(vertical = NoorDesignSystem.spacing.extraSmall),
                                )
                            }
                            items(
                                items = uiState.results,
                                key = { it.ayah.id },
                                contentType = { "search_result" },
                            ) { result ->
                                SearchResultCard(
                                    result = result,
                                    onClick = { onOpenResult(result) },
                                )
                            }
                        }

                        else -> SearchStatusPanel(
                            title = stringResource(R.string.search_initial_title),
                            body = stringResource(R.string.search_initial_body),
                            icon = NoorLineIconType.BookOpen,
                        )
                    }
                }
            }
        }
    }
}
