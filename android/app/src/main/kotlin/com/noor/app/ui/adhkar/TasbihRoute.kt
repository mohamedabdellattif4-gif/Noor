package com.noor.app.ui.adhkar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.R
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorPatternBackground
import com.noor.core.designsystem.component.NoorTopAppBar
import com.noor.core.designsystem.theme.NoorDesignSystem

@Composable
fun TasbihRoute(
    onBack: () -> Unit,
    viewModel: TasbihViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TasbihScreen(
        uiState = uiState,
        onBack = onBack,
        onIncrement = viewModel::increment,
        onReset = viewModel::reset,
        onPhraseSelected = viewModel::selectPhrase,
        onTargetSelected = viewModel::selectTarget,
    )
}

@Composable
internal fun TasbihScreen(
    uiState: TasbihUiState,
    onBack: () -> Unit,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onPhraseSelected: (TasbihPhrase) -> Unit,
    onTargetSelected: (Int) -> Unit,
) {
    val colors = NoorDesignSystem.colors
    val haptics = LocalHapticFeedback.current
    val incrementLabel = stringResource(R.string.tasbih_increment_action)
    val progressDescription = if (uiState.isComplete) {
        stringResource(R.string.tasbih_completed_state, uiState.target, uiState.target)
    } else {
        stringResource(R.string.adhkar_count_progress, uiState.count, uiState.target)
    }
    NoorPatternBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                NoorTopAppBar(
                    title = stringResource(R.string.tasbih_title),
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                    },
                    actions = {
                        TextButton(onClick = onReset, enabled = uiState.count > 0) {
                            Text(stringResource(R.string.adhkar_reset_count))
                        }
                    },
                )
            },
        ) { padding ->
            NoorContentContainer(modifier = Modifier.padding(padding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(NoorDesignSystem.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item(key = "phrase", contentType = "phrase") {
                        Text(
                            text = uiState.phrase.arabic,
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.onEmerald,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                    item(key = "counter", contentType = "counter") {
                        Surface(
                            modifier = Modifier
                                .sizeIn(minWidth = 240.dp, minHeight = 240.dp)
                                .testTag(TasbihTestTags.COUNTER)
                                .semantics(mergeDescendants = true) {
                                    stateDescription = progressDescription
                                }
                                .clickable(
                                    enabled = !uiState.isComplete,
                                    role = Role.Button,
                                    onClickLabel = incrementLabel,
                                ) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onIncrement()
                                },
                            shape = MaterialTheme.shapes.extraLarge,
                            color = if (uiState.isComplete) colors.goldHighlight else colors.ivory,
                            border = BorderStroke(3.dp, colors.mutedGold),
                            shadowElevation = 12.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = uiState.count.toString(),
                                        color = colors.emeraldDeep,
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = stringResource(R.string.tasbih_target_value, uiState.target),
                                        color = colors.onIvoryMuted,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    if (uiState.isComplete) {
                                        Text(
                                            text = stringResource(R.string.adhkar_completed),
                                            color = colors.emeraldDeep,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(key = "hint", contentType = "hint") {
                        Text(
                            text = stringResource(R.string.tasbih_tap_hint),
                            color = colors.onEmerald.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    item(key = "targets", contentType = "targets") {
                        Row(horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small)) {
                            listOf(33, 100).forEach { target ->
                                FilterChip(
                                    selected = uiState.target == target,
                                    onClick = { onTargetSelected(target) },
                                    label = { Text(target.toString()) },
                                )
                            }
                        }
                    }
                    item(key = "phrases_title", contentType = "phrases_title") {
                        Text(
                            text = stringResource(R.string.tasbih_choose_phrase),
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.onEmerald,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    TasbihPhrase.entries.forEach { phrase ->
                        item(key = phrase.name, contentType = "phrase_option") {
                            Card(
                                onClick = { onPhraseSelected(phrase) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (uiState.phrase == phrase) {
                                        colors.ivoryElevated
                                    } else {
                                        colors.ivory
                                    },
                                ),
                                border = BorderStroke(1.dp, colors.mutedGold),
                            ) {
                                Text(
                                    text = phrase.arabic,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(NoorDesignSystem.spacing.medium),
                                    color = colors.onIvory,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal object TasbihTestTags {
    const val COUNTER = "tasbih_counter"
}
