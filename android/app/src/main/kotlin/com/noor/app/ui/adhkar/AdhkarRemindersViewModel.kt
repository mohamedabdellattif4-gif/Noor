package com.noor.app.ui.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.domain.model.AdhkarReminderSettings
import com.noor.domain.repository.AdhkarReminderScheduler
import com.noor.domain.repository.AdhkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AdhkarRemindersViewModel @Inject constructor(
    private val repository: AdhkarRepository,
    private val scheduler: AdhkarReminderScheduler,
) : ViewModel() {
    private val eventChannel = Channel<AdhkarReminderEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    val settings: StateFlow<AdhkarReminderSettings> = repository.reminderSettings
        .onEach { settings ->
            try {
                scheduler.synchronize(settings)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                eventChannel.send(AdhkarReminderEvent.SchedulingFailed)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AdhkarReminderSettings(),
        )

    fun setMorningEnabled(enabled: Boolean) = update {
        repository.setMorningReminderEnabled(enabled)
    }

    fun setEveningEnabled(enabled: Boolean) = update {
        repository.setEveningReminderEnabled(enabled)
    }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                eventChannel.send(AdhkarReminderEvent.UpdateFailed)
            }
        }
    }
}

sealed interface AdhkarReminderEvent {
    data object UpdateFailed : AdhkarReminderEvent
    data object SchedulingFailed : AdhkarReminderEvent
}
