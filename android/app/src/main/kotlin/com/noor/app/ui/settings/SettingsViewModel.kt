package com.noor.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.domain.model.Reciter
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings
import com.noor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {
    private val eventChannel = Channel<SettingsEvent>(capacity = Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    val settings: StateFlow<UserSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings(),
    )

    fun setThemeMode(value: ThemeMode) = launch { repository.setThemeMode(value) }
    fun setDynamicColor(value: Boolean) = launch { repository.setDynamicColorEnabled(value) }
    fun setTextScale(value: Float) = launch { repository.setQuranTextScale(value) }
    fun setReciter(value: Reciter) = launch { repository.setReciter(value) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                eventChannel.send(SettingsEvent.UpdateFailed)
            }
        }
    }
}

sealed interface SettingsEvent {
    data object UpdateFailed : SettingsEvent
}
