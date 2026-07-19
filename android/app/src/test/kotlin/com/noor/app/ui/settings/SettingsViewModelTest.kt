package com.noor.app.ui.settings

import com.noor.app.testing.MainDispatcherRule
import com.noor.domain.model.Reciter
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings
import com.noor.domain.repository.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun successfulUpdateIsDelegatedToRepository() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, repository.values.value.themeMode)
    }

    @Test
    fun failedUpdateEmitsFeedbackWithoutCrashingTheScope() = runTest {
        val repository = FakeSettingsRepository(failWrites = true)
        val viewModel = SettingsViewModel(repository)
        val event = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }

        viewModel.setReciter(Reciter.HUSARY)
        advanceUntilIdle()

        assertEquals(SettingsEvent.UpdateFailed, event.await())
        assertEquals(Reciter.ALAFASY, repository.values.value.reciter)
    }

    private class FakeSettingsRepository(
        private val failWrites: Boolean = false,
    ) : SettingsRepository {
        val values = MutableStateFlow(UserSettings())
        override val settings: Flow<UserSettings> = values

        override suspend fun setThemeMode(value: ThemeMode) = write {
            values.value = values.value.copy(themeMode = value)
        }

        override suspend fun setDynamicColorEnabled(value: Boolean) = write {
            values.value = values.value.copy(dynamicColorEnabled = value)
        }

        override suspend fun setQuranTextScale(value: Float) = write {
            values.value = values.value.copy(quranTextScale = value)
        }

        override suspend fun setReciter(value: Reciter) = write {
            values.value = values.value.copy(reciter = value)
        }

        override suspend fun setLastRead(surahNumber: Int, ayahId: Int) = Unit

        private inline fun write(block: () -> Unit) {
            if (failWrites) throw IOException("storage unavailable")
            block()
        }
    }
}
