package com.noor.app.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.noor.app.R
import com.noor.core.designsystem.component.NoorCard
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorTopAppBar
import com.noor.core.designsystem.theme.NoorDesignSystem

@Composable
fun LegalRoute(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            NoorTopAppBar(
                title = stringResource(R.string.legal_notices),
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                },
            )
        },
    ) { padding ->
        NoorContentContainer(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(NoorDesignSystem.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            ) {
                NoorCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.privacy_policy),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.privacy_policy_body),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                SourceCard(
                    title = stringResource(R.string.source_tanzil_title),
                    description = stringResource(R.string.source_tanzil_body),
                    onOpen = { uriHandler.openUri("https://tanzil.net") },
                )
                SourceCard(
                    title = stringResource(R.string.source_quran_meta_title),
                    description = stringResource(R.string.source_quran_meta_body),
                    onOpen = {
                        uriHandler.openUri("https://github.com/quran-center/quran-meta")
                    },
                )
                SourceCard(
                    title = stringResource(R.string.source_online_services_title),
                    description = stringResource(R.string.source_online_services_body),
                    onOpen = { uriHandler.openUri("https://quranenc.com") },
                )
                SourceCard(
                    title = stringResource(R.string.source_audio_title),
                    description = stringResource(R.string.source_audio_body),
                    onOpen = { uriHandler.openUri("https://everyayah.com") },
                )
            }
        }
    }
}

@Composable
private fun SourceCard(title: String, description: String, onOpen: () -> Unit) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(description, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onOpen) {
            Text(stringResource(R.string.open_source_website))
        }
    }
}
