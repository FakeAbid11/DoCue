package com.example.docue.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docue.ui.components.DoCueSectionHeader
import com.example.docue.ui.theme.ThemeMode
import com.example.docue.util.OemSettingsLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val reliabilityState by viewModel.reliabilityState.collectAsStateWithLifecycle()
    val backgroundProtectionState by viewModel.backgroundProtectionState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshReliabilityState()
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Delete all cues?") },
            text = { Text("This will permanently remove all your reminders. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllReminders()
                        showResetDialog = false
                    }
                ) {
                    Text("Delete all", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            DoCueSectionHeader(icon = Icons.Outlined.Palette, title = "Appearance")
            ThemeSettingSection(
                selectedMode = themeMode,
                onModeSelected = { viewModel.setThemeMode(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DoCueSectionHeader(icon = Icons.Outlined.Notifications, title = "Reminder reliability")
            ReliabilitySection(
                state = reliabilityState,
                onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                onOpenExactAlarmSettings = { viewModel.openExactAlarmSettings() },
                onOpenBatteryOptimizationSettings = { viewModel.openBatteryOptimizationSettings() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DoCueSectionHeader(icon = Icons.Outlined.PhoneAndroid, title = "Background protection")
            BackgroundProtectionSection(
                state = backgroundProtectionState,
                onOpenAutoStartSettings = { viewModel.openAutoStartSettings() },
                onOpenBatterySettings = { viewModel.openBatterySettings() },
                onOpenAppInfoSettings = { viewModel.openAppInfoSettings() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DoCueSectionHeader(icon = Icons.Outlined.DeleteForever, title = "Danger zone")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { showResetDialog = true })
                    .semantics {
                        contentDescription = "Delete all reminders"
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Delete all cues",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Start fresh",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DoCueSectionHeader(icon = Icons.Outlined.Info, title = "About")
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "DoCue v1.1.0",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Save it. Get cued. Do it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ThemeSettingSection(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .semantics {
                        contentDescription = "${mode.name.lowercase()} theme"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == mode,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = when (mode) {
                        ThemeMode.SYSTEM -> "System default"
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ReliabilitySection(
    state: ReliabilityState,
    onOpenNotificationSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onOpenBatteryOptimizationSettings: () -> Unit
) {
    Column {
        ReliabilityItem(
            icon = Icons.Outlined.Notifications,
            title = "Notifications",
            status = if (state.notificationsEnabled) "Enabled" else "Disabled",
            statusColor = if (state.notificationsEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
            onClick = if (!state.notificationsEnabled) onOpenNotificationSettings else null
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ReliabilityItem(
                icon = Icons.Outlined.Alarm,
                title = "Exact alarms",
                status = if (state.exactAlarmsAllowed) "Allowed" else "Denied",
                statusColor = if (state.exactAlarmsAllowed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                onClick = if (!state.exactAlarmsAllowed) onOpenExactAlarmSettings else null
            )
        }

        ReliabilityItem(
            icon = if (state.batteryOptimizationRestricted) Icons.Outlined.Warning else Icons.Outlined.BatteryAlert,
            title = "Battery optimization",
            status = if (state.batteryOptimizationRestricted) {
                "May affect reliability"
            } else {
                "Not restricted"
            },
            statusColor = if (state.batteryOptimizationRestricted) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            onClick = onOpenBatteryOptimizationSettings
        )
    }
}

@Composable
private fun ReliabilityItem(
    icon: ImageVector,
    title: String,
    status: String,
    statusColor: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics {
                contentDescription = "$title: $status"
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
        }
        if (onClick != null) {
            Text(
                text = "Fix",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BackgroundProtectionSection(
    state: BackgroundProtectionState,
    onOpenAutoStartSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenAppInfoSettings: () -> Unit
) {
    Column {
        Text(
            text = "Your phone (${state.manufacturerDisplayName}) may restrict DoCue in the background.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ReliabilityItem(
            icon = Icons.Outlined.PhoneAndroid,
            title = "Auto-start / background launch",
            status = "Review device settings",
            statusColor = MaterialTheme.colorScheme.primary,
            onClick = onOpenAutoStartSettings
        )

        ReliabilityItem(
            icon = if (state.batteryOptimizationRestricted) Icons.Outlined.Warning else Icons.Outlined.BatteryAlert,
            title = "Battery usage",
            status = if (state.batteryOptimizationRestricted) {
                "May affect reliability"
            } else {
                "Not restricted"
            },
            statusColor = if (state.batteryOptimizationRestricted) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            onClick = onOpenBatterySettings
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "May improve reminder reliability",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onOpenAppInfoSettings,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Open DoCue settings")
            }
        }
    }
}
