package com.noor.app.ui.audio

internal sealed interface AudioEvent {
    data object ReciterUpdateFailed : AudioEvent
}
