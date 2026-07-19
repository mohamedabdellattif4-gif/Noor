package com.noor.app.ui.adhkar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TasbihViewModelTest {
    @Test
    fun countStopsAtTheSelectedTargetAndCanReset() {
        val viewModel = TasbihViewModel()
        repeat(40) { viewModel.increment() }

        assertEquals(33, viewModel.uiState.value.count)
        assertTrue(viewModel.uiState.value.isComplete)

        viewModel.reset()
        assertEquals(0, viewModel.uiState.value.count)
    }

    @Test
    fun changingPhraseResetsCountAndUsesItsRecommendedTarget() {
        val viewModel = TasbihViewModel()
        repeat(5) { viewModel.increment() }

        viewModel.selectPhrase(TasbihPhrase.ASTAGHFIRULLAH)

        assertEquals(0, viewModel.uiState.value.count)
        assertEquals(100, viewModel.uiState.value.target)
    }
}
