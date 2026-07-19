package com.noor.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.noor.core.designsystem.theme.NoorDesignSystem

@Composable
fun NoorCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(NoorDesignSystem.spacing.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        NoorCardContent(
            contentPadding = contentPadding,
            content = content,
        )
    }
}

@Composable
fun NoorClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(NoorDesignSystem.spacing.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        NoorCardContent(
            contentPadding = contentPadding,
            content = content,
        )
    }
}

@Composable
private fun NoorCardContent(
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.padding(contentPadding),
        content = content,
    )
}
