package com.noor.app.ui.bookmarks

/** One-shot UI events emitted by [BookmarksViewModel]. */
sealed interface BookmarksEvent {
    data object RemovalFailed : BookmarksEvent
}
