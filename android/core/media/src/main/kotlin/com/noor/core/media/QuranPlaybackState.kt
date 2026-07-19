package com.noor.core.media

sealed interface QuranPlaybackState {
    data object Idle : QuranPlaybackState
    data object Buffering : QuranPlaybackState
    data class Playing(val mediaId: String?) : QuranPlaybackState
    data object Paused : QuranPlaybackState
    data object Error : QuranPlaybackState
}
