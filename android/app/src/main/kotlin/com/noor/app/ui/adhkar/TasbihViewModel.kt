package com.noor.app.ui.adhkar

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class TasbihViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(TasbihUiState())
    val uiState: StateFlow<TasbihUiState> = _uiState.asStateFlow()

    fun increment() = _uiState.update { state ->
        state.copy(count = (state.count + 1).coerceAtMost(state.target))
    }

    fun reset() = _uiState.update { it.copy(count = 0) }

    fun selectPhrase(phrase: TasbihPhrase) = _uiState.update {
        it.copy(phrase = phrase, count = 0, target = phrase.defaultTarget)
    }

    fun selectTarget(target: Int) {
        require(target in TARGETS)
        _uiState.update { it.copy(target = target, count = it.count.coerceAtMost(target)) }
    }

    private companion object {
        val TARGETS = setOf(33, 100)
    }
}

data class TasbihUiState(
    val phrase: TasbihPhrase = TasbihPhrase.SUBHAN_ALLAH,
    val count: Int = 0,
    val target: Int = phrase.defaultTarget,
) {
    init {
        require(count >= 0)
        require(target > 0)
        require(count <= target)
    }

    val isComplete: Boolean get() = count >= target
}

enum class TasbihPhrase(val arabic: String, val defaultTarget: Int) {
    SUBHAN_ALLAH("سُبْحَانَ اللَّهِ", 33),
    ALHAMDULILLAH("الْحَمْدُ لِلَّهِ", 33),
    ALLAHU_AKBAR("اللَّهُ أَكْبَرُ", 33),
    ASTAGHFIRULLAH("أَسْتَغْفِرُ اللَّهَ", 100),
    SALAWAT("اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", 100),
}
