package com.noor.app.ui.tafsir

import androidx.compose.runtime.Immutable
import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir

@Immutable
data class TafsirUiState(
    val ayah: Ayah? = null,
    val tafsir: Tafsir? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
)
