package com.noor.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.noor.core.designsystem.R

@Composable
fun NoorLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 32.dp,
    strokeWidth: Dp = 3.dp,
) {
    val loadingDescription = stringResource(R.string.noor_loading)
    CircularProgressIndicator(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = loadingDescription
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            },
        color = color,
        strokeWidth = strokeWidth,
    )
}

@Composable
fun NoorLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        NoorLoadingIndicator()
    }
}
