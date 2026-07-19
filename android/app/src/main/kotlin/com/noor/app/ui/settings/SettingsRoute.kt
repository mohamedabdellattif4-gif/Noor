package com.noor.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.app.R
import com.noor.core.designsystem.component.NoorCard
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorTopAppBar
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.Reciter
import com.noor.domain.model.ThemeMode
import com.noor.domain.model.UserSettings

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenLegal: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updateError = stringResource(R.string.settings_update_error)

    LaunchedEffect(viewModel, updateError) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.UpdateFailed -> snackbarHostState.showSnackbar(updateError)
            }
        }
    }

    SettingsScreen(
        settings = settings,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onOpenLegal = onOpenLegal,
        onThemeModeChange = viewModel::setThemeMode,
        onDynamicColorChange = viewModel::setDynamicColor,
        onTextScaleChange = viewModel::setTextScale,
        onReciterChange = viewModel::setReciter,
    )
}

@Composable
internal fun SettingsScreen(
    settings: UserSettings,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onOpenLegal: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onTextScaleChange: (Float) -> Unit,
    onReciterChange: (Reciter) -> Unit,
) {
    Scaffold(
        topBar = {
            NoorTopAppBar(
                title = stringResource(R.string.settings),
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        NoorContentContainer(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(NoorDesignSystem.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            ) {
                SettingsSection(title = stringResource(R.string.appearance)) {
                    Column(modifier = Modifier.selectableGroup()) {
                        ThemeMode.entries.forEach { mode ->
                            SelectionRow(
                                label = themeModeLabel(mode),
                                selected = settings.themeMode == mode,
                                onClick = { onThemeModeChange(mode) },
                                modifier = Modifier.testTag(SettingsTestTags.themeMode(mode)),
                            )
                        }
                    }
                    SwitchRow(
                        label = stringResource(R.string.dynamic_color),
                        checked = settings.dynamicColorEnabled,
                        onCheckedChange = onDynamicColorChange,
                        modifier = Modifier.testTag(SettingsTestTags.DYNAMIC_COLOR),
                    )
                }
                SettingsSection(title = stringResource(R.string.reading)) {
                    var previewScale by remember(settings.quranTextScale) {
                        mutableFloatStateOf(settings.quranTextScale)
                    }
                    Text(stringResource(R.string.quran_text_size))
                    Slider(
                        value = previewScale,
                        onValueChange = { previewScale = it },
                        onValueChangeFinished = { onTextScaleChange(previewScale) },
                        valueRange = UserSettings.MIN_TEXT_SCALE..UserSettings.MAX_TEXT_SCALE,
                        steps = 7,
                        modifier = Modifier.testTag(SettingsTestTags.TEXT_SCALE),
                    )
                    Text(
                        text = stringResource(R.string.quran_text_preview),
                        modifier = Modifier.fillMaxWidth(),
                        style = NoorDesignSystem.extendedTypography.quranMedium.copy(
                            fontSize = (26f * previewScale).sp,
                            lineHeight = (48f * previewScale).sp,
                        ),
                    )
                }
                SettingsSection(title = stringResource(R.string.audio)) {
                    Column(modifier = Modifier.selectableGroup()) {
                        Reciter.entries.forEach { reciter ->
                            SelectionRow(
                                label = reciterLabel(reciter),
                                selected = settings.reciter == reciter,
                                onClick = { onReciterChange(reciter) },
                                modifier = Modifier.testTag(SettingsTestTags.reciter(reciter)),
                            )
                        }
                    }
                }
                SettingsSection(title = stringResource(R.string.about_and_licenses)) {
                    Text(stringResource(R.string.attribution_summary))
                    Text(
                        text = stringResource(R.string.privacy_summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = onOpenLegal,
                        modifier = Modifier.testTag(SettingsTestTags.LEGAL),
                    ) {
                        Text(stringResource(R.string.open_legal_notices))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    NoorCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = NoorDesignSystem.spacing.small))
        content()
    }
}

@Composable
private fun SelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = NoorDesignSystem.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = NoorDesignSystem.spacing.small),
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
            )
            .padding(horizontal = NoorDesignSystem.spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = NoorDesignSystem.spacing.medium),
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun themeModeLabel(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    },
)

@Composable
private fun reciterLabel(reciter: Reciter): String = stringResource(
    when (reciter) {
        Reciter.ALAFASY -> R.string.reciter_alafasy
        Reciter.HUSARY -> R.string.reciter_husary
        Reciter.MINSHAWI -> R.string.reciter_minshawi
    },
)

internal object SettingsTestTags {
    const val DYNAMIC_COLOR = "settings_dynamic_color"
    const val TEXT_SCALE = "settings_text_scale"
    const val LEGAL = "settings_legal"

    fun themeMode(mode: ThemeMode): String = "settings_theme_${mode.name.lowercase()}"
    fun reciter(reciter: Reciter): String = "settings_reciter_${reciter.name.lowercase()}"
}
