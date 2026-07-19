package com.noor.core.designsystem.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.noor.core.designsystem.component.NoorCard
import com.noor.core.designsystem.component.NoorFilledButton
import com.noor.core.designsystem.component.NoorLoadingIndicator
import com.noor.core.designsystem.component.NoorOutlinedButton
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.core.designsystem.theme.NoorTheme

@NoorThemePreviews
@Composable
private fun NoorComponentsPreview() {
    NoorTheme {
        Column(
            modifier = Modifier.padding(NoorDesignSystem.spacing.large),
            verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
        ) {
            NoorCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "نور",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ",
                    style = NoorDesignSystem.extendedTypography.quranMedium,
                )
            }
            NoorFilledButton(
                text = "متابعة القراءة",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            NoorOutlinedButton(
                text = "العلامات المرجعية",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            NoorLoadingIndicator()
        }
    }
}
