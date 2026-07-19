package com.noor.app.ui.tafsir

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noor.app.R
import com.noor.core.designsystem.component.NoorFilledButton
import com.noor.core.designsystem.component.NoorLoadingIndicator
import com.noor.core.designsystem.component.NoorTopAppBar
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.Ayah
import com.noor.domain.model.Tafsir
import com.noor.domain.model.TafsirOrigin

@Composable
internal fun TafsirTopBar(
    isRefreshing: Boolean,
    canRefresh: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    NoorTopAppBar(
        title = stringResource(R.string.tafsir_screen_title),
        navigationIcon = {
            TextButton(
                onClick = onBack,
                modifier = Modifier.testTag(TafsirTestTags.BACK),
            ) {
                Text(stringResource(R.string.back))
            }
        },
        actions = {
            TextButton(
                onClick = onRefresh,
                enabled = canRefresh && !isRefreshing,
                modifier = Modifier.testTag(TafsirTestTags.REFRESH),
            ) {
                if (isRefreshing) {
                    NoorLoadingIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        size = 18.dp,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.tafsir_refresh))
                }
            }
        },
    )
}

@Composable
internal fun TafsirStatusPanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = BorderStroke(1.dp, colors.mutedGold.copy(alpha = 0.7f)),
    ) {
        Column(
            modifier = Modifier.padding(NoorDesignSystem.spacing.large),
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isLoading) {
                NoorLoadingIndicator(color = colors.emerald, size = 28.dp)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.emeraldDeep,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onIvoryMuted,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                NoorFilledButton(
                    text = actionLabel,
                    onClick = onAction,
                    modifier = Modifier.testTag(TafsirTestTags.RETRY),
                )
            }
        }
    }
}

@Composable
internal fun TafsirAyahCard(ayah: Ayah, modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TafsirTestTags.AYAH_CARD),
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.ivory,
        contentColor = colors.onIvory,
        border = BorderStroke(1.dp, colors.mutedGold),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(NoorDesignSystem.spacing.large),
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.tafsir_ayah_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.emerald,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        R.string.tafsir_ayah_position,
                        ayah.numberInSurah,
                        ayah.pageNumber,
                        ayah.juzNumber,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onIvoryMuted,
                )
            }
            Text(
                text = ayah.textUthmani,
                modifier = Modifier.fillMaxWidth(),
                style = NoorDesignSystem.extendedTypography.quranLarge,
                color = colors.onIvory,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
internal fun TafsirExplanationCard(tafsir: Tafsir, modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TafsirTestTags.EXPLANATION_CARD),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.ivoryElevated),
        border = BorderStroke(1.dp, colors.mutedGold.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier.padding(NoorDesignSystem.spacing.large),
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
        ) {
            Text(
                text = stringResource(R.string.tafsir_explanation_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.emeraldDeep,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = tafsir.text,
                modifier = Modifier.testTag(TafsirTestTags.TAFSIR_TEXT),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onIvory,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
internal fun TafsirFootnotesCard(footnotes: String, modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TafsirTestTags.FOOTNOTES_CARD),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
    ) {
        Column(
            modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.small),
        ) {
            Text(
                text = stringResource(R.string.tafsir_footnotes_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.emerald,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = footnotes,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onIvoryMuted,
            )
        }
    }
}

@Composable
internal fun TafsirSourceCard(tafsir: Tafsir, modifier: Modifier = Modifier) {
    val colors = NoorDesignSystem.colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TafsirTestTags.SOURCE_CARD),
        shape = MaterialTheme.shapes.large,
        color = colors.emeraldDeep,
        contentColor = colors.onEmerald,
    ) {
        Row(
            modifier = Modifier.padding(NoorDesignSystem.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = if (tafsir.origin == TafsirOrigin.CACHE) {
                    colors.goldHighlight
                } else {
                    colors.emeraldMuted
                },
            ) {}
            Column(verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.extraSmall)) {
                Text(
                    text = tafsir.sourceName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        if (tafsir.origin == TafsirOrigin.CACHE) {
                            R.string.tafsir_cached_status
                        } else {
                            R.string.tafsir_network_status
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onEmerald.copy(alpha = 0.78f),
                )
            }
        }
    }
}

internal object TafsirTestTags {
    const val BACK = "tafsir_back"
    const val REFRESH = "tafsir_refresh"
    const val RETRY = "tafsir_retry"
    const val CONTENT = "tafsir_content"
    const val AYAH_CARD = "tafsir_ayah_card"
    const val EXPLANATION_CARD = "tafsir_explanation_card"
    const val TAFSIR_TEXT = "tafsir_text"
    const val FOOTNOTES_CARD = "tafsir_footnotes_card"
    const val SOURCE_CARD = "tafsir_source_card"
}
