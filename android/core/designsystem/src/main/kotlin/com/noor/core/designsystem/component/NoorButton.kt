package com.noor.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.noor.core.designsystem.theme.NoorDesignSystem

@Composable
fun NoorFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = if (isLoading) {
            ButtonDefaults.buttonColors(
                disabledContainerColor = colorScheme.primary,
                disabledContentColor = colorScheme.onPrimary,
            )
        } else {
            ButtonDefaults.buttonColors()
        },
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        NoorButtonContent(
            text = text,
            isLoading = isLoading,
            loadingIndicatorColor = colorScheme.onPrimary,
            leadingIcon = leadingIcon,
        )
    }
}

@Composable
fun NoorOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = if (isLoading) {
            ButtonDefaults.outlinedButtonColors(
                disabledContentColor = colorScheme.primary,
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        },
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        NoorButtonContent(
            text = text,
            isLoading = isLoading,
            loadingIndicatorColor = colorScheme.primary,
            leadingIcon = leadingIcon,
        )
    }
}

@Composable
private fun RowScope.NoorButtonContent(
    text: String,
    isLoading: Boolean,
    loadingIndicatorColor: Color,
    leadingIcon: (@Composable () -> Unit)?,
) {
    when {
        isLoading -> NoorLoadingIndicator(
            color = loadingIndicatorColor,
            size = 18.dp,
            strokeWidth = 2.dp,
        )

        leadingIcon != null -> leadingIcon.invoke()
    }

    if (isLoading || leadingIcon != null) {
        Spacer(modifier = Modifier.width(NoorDesignSystem.spacing.small))
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
    )
}
