package com.noor.app.ui.adhkar

import com.noor.app.testing.MainDispatcherRule
import com.noor.domain.model.AdhkarReminderSettings
import com.noor.domain.model.Dhikr
import com.noor.domain.repository.AdhkarReminderScheduler
import com.noor.domain.repository.AdhkarRepository
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdhkarRemindersViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun schedulerFailureEmitsFeedbackAndKeepsSettingsFlowAlive() = runTest {
        val repository = FakeAdhkarRepository(
            initialSettings = AdhkarReminderSettings(morningEnabled = true),
        )
        val viewModel = AdhkarRemindersViewModel(
            repository = repository,
            scheduler = ThrowingScheduler,
        )
        val collection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect()
        }
        val event = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }

        advanceUntilIdle()

        assertEquals(AdhkarReminderEvent.SchedulingFailed, event.await())
        assertEquals(true, viewModel.settings.value.morningEnabled)
        collection.cancel()
    }

    @Test
    fun repositoryWriteFailureEmitsUpdateFeedback() = runTest {
        val viewModel = AdhkarRemindersViewModel(
            repository = FakeAdhkarRepository(failWrites = true),
            scheduler = RecordingScheduler(),
        )
        val event = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }

        viewModel.setEveningEnabled(true)
        advanceUntilIdle()

        assertEquals(AdhkarReminderEvent.UpdateFailed, event.await())
    }

    private class FakeAdhkarRepository(
        initialSettings: AdhkarReminderSettings = AdhkarReminderSettings(),
        private val failWrites: Boolean = false,
    ) : AdhkarRepository {
        private val settings = MutableStateFlow(initialSettings)

        override val adhkar: Flow<List<Dhikr>> = MutableStateFlow(emptyList())
        override val reminderSettings: Flow<AdhkarReminderSettings> = settings

        override suspend fun setFavorite(dhikrId: String, favorite: Boolean) = Unit

        override suspend fun setMorningReminderEnabled(enabled: Boolean) = write {
            settings.value = settings.value.copy(morningEnabled = enabled)
        }

        override suspend fun setEveningReminderEnabled(enabled: Boolean) = write {
            settings.value = settings.value.copy(eveningEnabled = enabled)
        }

        private inline fun write(block: () -> Unit) {
            if (failWrites) throw IOException("storage unavailable")
            block()
        }
    }

    private object ThrowingScheduler : AdhkarReminderScheduler {
        override fun synchronize(settings: AdhkarReminderSettings) {
            throw IllegalStateException("WorkManager unavailable")
        }
    }

    private class RecordingScheduler : AdhkarReminderScheduler {
        override fun synchronize(settings: AdhkarReminderSettings) = Unit
    }
}
