package com.noor.app.ui.adhkar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.noor.app.R
import com.noor.core.designsystem.component.NoorContentContainer
import com.noor.core.designsystem.component.NoorPatternBackground
import com.noor.core.designsystem.component.NoorTopAppBar
import com.noor.core.designsystem.theme.NoorDesignSystem
import com.noor.domain.model.AdhkarCategory
import com.noor.domain.model.AdhkarReminderSettings
import kotlinx.coroutines.launch

@Composable
fun AdhkarRemindersRoute(
    onBack: () -> Unit,
    viewModel: AdhkarRemindersViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pendingCategory by remember { mutableStateOf<AdhkarCategory?>(null) }
    val deniedMessage = stringResource(R.string.adhkar_notification_permission_denied)
    val updateError = stringResource(R.string.adhkar_update_error)
    val schedulingError = stringResource(R.string.adhkar_scheduling_error)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val pending = pendingCategory
        pendingCategory = null
        if (granted) {
            when (pending) {
                AdhkarCategory.MORNING -> viewModel.setMorningEnabled(true)
                AdhkarCategory.EVENING -> viewModel.setEveningEnabled(true)
                else -> Unit
            }
        } else {
            coroutineScope.launch { snackbar.showSnackbar(deniedMessage) }
        }
    }
    LaunchedEffect(viewModel, updateError, schedulingError) {
        viewModel.events.collect { event ->
            snackbar.showSnackbar(
                when (event) {
                    AdhkarReminderEvent.UpdateFailed -> updateError
                    AdhkarReminderEvent.SchedulingFailed -> schedulingError
                },
            )
        }
    }

    fun requestEnable(category: AdhkarCategory, enabled: Boolean) {
        if (!enabled) {
            if (category == AdhkarCategory.MORNING) {
                viewModel.setMorningEnabled(false)
            } else {
                viewModel.setEveningEnabled(false)
            }
            return
        }
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            if (category == AdhkarCategory.MORNING) {
                viewModel.setMorningEnabled(true)
            } else {
                viewModel.setEveningEnabled(true)
            }
        } else {
            pendingCategory = category
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    AdhkarRemindersScreen(
        settings = settings,
        snackbarHostState = snackbar,
        onBack = onBack,
        onMorningChanged = { requestEnable(AdhkarCategory.MORNING, it) },
        onEveningChanged = { requestEnable(AdhkarCategory.EVENING, it) },
    )
}

@Composable
internal fun AdhkarRemindersScreen(
    settings: AdhkarReminderSettings,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onMorningChanged: (Boolean) -> Unit,
    onEveningChanged: (Boolean) -> Unit,
) {
    val colors = NoorDesignSystem.colors
    NoorPatternBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                NoorTopAppBar(
                    title = stringResource(R.string.adhkar_reminders_title),
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            NoorContentContainer(modifier = Modifier.padding(padding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(NoorDesignSystem.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
                ) {
                    item(key = "notice", contentType = "notice") {
                        Text(
                            text = stringResource(R.string.adhkar_reminders_notice),
                            color = colors.onEmerald,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    item(key = "morning", contentType = "reminder") {
                        ReminderCard(
                            title = stringResource(R.string.adhkar_morning_reminder_title),
                            time = stringResource(R.string.adhkar_reminder_time, settings.morningHour),
                            enabled = settings.morningEnabled,
                            onChanged = onMorningChanged,
                        )
                    }
                    item(key = "evening", contentType = "reminder") {
                        ReminderCard(
                            title = stringResource(R.string.adhkar_evening_reminder_title),
                            time = stringResource(R.string.adhkar_reminder_time, settings.eveningHour),
                            enabled = settings.eveningEnabled,
                            onChanged = onEveningChanged,
                        )
                    }
                    item(key = "system_note", contentType = "system_note") {
                        Text(
                            text = stringResource(R.string.adhkar_reminders_system_note),
                            color = colors.onEmerald.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    time: String,
    enabled: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    val colors = NoorDesignSystem.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.ivory),
        border = BorderStroke(1.dp, colors.mutedGold),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .toggleable(
                    value = enabled,
                    onValueChange = onChanged,
                    role = Role.Switch,
                )
                .padding(NoorDesignSystem.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(NoorDesignSystem.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.emeraldDeep,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = time, color = colors.onIvoryMuted)
            }
            Switch(checked = enabled, onCheckedChange = null)
        }
    }
}
