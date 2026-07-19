package com.noor.app.ui.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.R
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorPatternBackground
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.Bookmark

@Composable
fun BookmarksRoute(
    onBack: () -> Unit,
    onOpenBookmark: (Bookmark) -> Unit,
    viewModel: BookmarksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val removalFailedMessage = stringResource(R.string.bookmark_remove_error)

    LaunchedEffect(viewModel, removalFailedMessage) {
        viewModel.events.collect { event ->
            when (event) {
                BookmarksEvent.RemovalFailed -> snackbarHostState.showSnackbar(removalFailedMessage)
            }
        }
    }

    BookmarksScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenBookmark = onOpenBookmark,
        onRemoveBookmark = viewModel::remove,
    )
}

@Composable
internal fun BookmarksScreen(
    uiState: BookmarksUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenBookmark: (Bookmark) -> Unit,
    onRemoveBookmark: (Int) -> Unit,
) {
    NoorPatternBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = { BookmarksTopBar(onBack = onBack) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { padding ->
            NoorContentContainer(modifier = Modifier.padding(padding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = NoorDesignSystem.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                ) {
                    when {
                        uiState.isLoading -> BookmarksStatusPanel(
                            title = stringResource(R.string.bookmarks_loading_title),
                            body = stringResource(R.string.bookmarks_loading_body),
                            modifier = Modifier.padding(top = NoorDesignSystem.spacing.medium),
                            isLoading = true,
                        )

                        uiState.hasError -> BookmarksStatusPanel(
                            title = stringResource(R.string.bookmarks_error_title),
                            body = stringResource(R.string.bookmarks_load_error),
                            modifier = Modifier.padding(top = NoorDesignSystem.spacing.medium),
                            actionLabel = stringResource(R.string.retry),
                            actionTag = BookmarksTestTags.RETRY,
                            onAction = onRetry,
                        )

                        uiState.bookmarks.isEmpty() -> BookmarksStatusPanel(
                            title = stringResource(R.string.bookmarks_empty_title),
                            body = stringResource(R.string.bookmarks_empty_body),
                            modifier = Modifier.padding(top = NoorDesignSystem.spacing.medium),
                            actionLabel = stringResource(R.string.bookmarks_browse_quran),
                            actionTag = BookmarksTestTags.EMPTY_ACTION,
                            onAction = onBack,
                        )

                        else -> LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(BookmarksTestTags.LIST),
                            contentPadding = PaddingValues(
                                top = NoorDesignSystem.spacing.medium,
                                bottom = 32.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
                        ) {
                            item(
                                key = "bookmarks_header",
                                contentType = "header",
                            ) {
                                BookmarksSummaryHeader(
                                    countLabel = pluralStringResource(
                                        R.plurals.bookmarks_count,
                                        uiState.bookmarks.size,
                                        uiState.bookmarks.size,
                                    ),
                                    modifier = Modifier.padding(
                                        bottom = NoorDesignSystem.spacing.extraSmall,
                                    ),
                                )
                            }
                            items(
                                items = uiState.bookmarks,
                                key = { it.ayah.id },
                                contentType = { "bookmark" },
                            ) { bookmark ->
                                BookmarkCard(
                                    bookmark = bookmark,
                                    isRemoving = bookmark.ayah.id in uiState.pendingRemovalIds,
                                    onOpen = { onOpenBookmark(bookmark) },
                                    onRemove = { onRemoveBookmark(bookmark.ayah.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
