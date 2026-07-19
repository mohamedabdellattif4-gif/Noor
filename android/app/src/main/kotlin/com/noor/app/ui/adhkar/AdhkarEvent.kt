package com.noor.app.ui.adhkar

sealed interface AdhkarEvent {
    data object UpdateFailed : AdhkarEvent
}
