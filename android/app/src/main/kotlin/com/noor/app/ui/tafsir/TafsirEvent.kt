package com.noor.app.ui.tafsir

/** One-shot feedback emitted while previously loaded tafsir remains visible. */
sealed interface TafsirEvent {
    data object RefreshFailed : TafsirEvent
}
