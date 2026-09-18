package com.example.docue.ui.screens.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docue.data.local.ActionType
import com.example.docue.data.local.RepeatType
import com.example.docue.util.formatDate
import com.example.docue.util.formatTime
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAppPicker: () -> Unit,
    onAppPickerResult: (packageName: String, appName: String) -> Unit,
    viewModel: CreateReminderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showExpirationPicker by remember { mutableStateOf(false) }

    if (showDeleteDialog && uiState.isEditing) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete cue?") },
            text = { Text("This reminder will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteReminder()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.reminderDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.updateDate(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.reminderTime.hour,
            initialMinute = uiState.reminderTime.minute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExpirationPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (uiState.expirationDate ?: uiState.reminderDate)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showExpirationPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.updateExpirationDate(date)
                        }
                        showExpirationPicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpirationPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(if (uiState.isEditing) "Edit Cue" else "Create Cue") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Go back"
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Delete cue"
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionLabel("What do you need to remember?")
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { error -> { Text(error) } },
                singleLine = true
            )

            SectionLabel("Add a note (optional)")
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Notes") },
                minLines = 2,
                maxLines = 4
            )

            HorizontalDivider()

            SectionLabel("When?")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.reminderDate.formatDate(),
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Pick date"
                            }
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    },
                    enabled = false
                )
                OutlinedTextField(
                    value = uiState.reminderTime.formatTime(),
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    label = { Text("Time") },
                    trailingIcon = {
                        IconButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Pick time"
                            }
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                        }
                    },
                    enabled = false
                )
            }

            HorizontalDivider()

            SectionLabel("Repeat")
            Column(modifier = Modifier.selectableGroup()) {
                RepeatType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = uiState.repeatType == type,
                                onClick = { viewModel.updateRepeatType(type) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.repeatType == type,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (type) {
                                RepeatType.NONE -> "Doesn't repeat"
                                RepeatType.DAILY -> "Every day"
                                RepeatType.WEEKLY -> "Every week"
                                RepeatType.WEEKDAYS -> "Weekdays"
                                RepeatType.WEEKENDS -> "Weekends"
                                RepeatType.CUSTOM_DAYS -> "Custom"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            if (uiState.repeatType == RepeatType.CUSTOM_DAYS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DayOfWeek.entries.forEach { day ->
                        val isSelected = day in uiState.customDays
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleCustomDay(day) },
                            label = {
                                Text(
                                    text = when (day) {
                                        DayOfWeek.MONDAY -> "M"
                                        DayOfWeek.TUESDAY -> "T"
                                        DayOfWeek.WEDNESDAY -> "W"
                                        DayOfWeek.THURSDAY -> "T"
                                        DayOfWeek.FRIDAY -> "F"
                                        DayOfWeek.SATURDAY -> "S"
                                        DayOfWeek.SUNDAY -> "S"
                                    }
                                )
                            },
                            modifier = Modifier.semantics {
                                contentDescription = day.toString()
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            SectionLabel("What should happen when I tap the cue?")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    onClick = { viewModel.updateActionType(ActionType.SIMPLE) },
                    selected = uiState.actionType == ActionType.SIMPLE
                ) {
                    Text("Just remind me")
                }
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    onClick = { viewModel.updateActionType(ActionType.APP) },
                    selected = uiState.actionType == ActionType.APP
                ) {
                    Text("Open app")
                }
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    onClick = { viewModel.updateActionType(ActionType.LINK) },
                    selected = uiState.actionType == ActionType.LINK
                ) {
                    Text("Open link")
                }
            }

            when (uiState.actionType) {
                ActionType.APP -> {
                    if (uiState.targetPackage.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.targetAppName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = uiState.targetPackage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearTargetApp() },
                                modifier = Modifier.semantics {
                                    contentDescription = "Remove selected app"
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    } else {
                        Button(
                            onClick = onNavigateToAppPicker,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Choose app")
                        }
                    }
                    if (uiState.actionError != null) {
                        Text(
                            text = uiState.actionError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                ActionType.LINK -> {
                    OutlinedTextField(
                        value = uiState.targetUrl,
                        onValueChange = { viewModel.updateTargetUrl(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://example.com") },
                        label = { Text("URL") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        singleLine = true,
                        isError = uiState.urlError != null,
                        supportingText = uiState.urlError?.let { error -> { Text(error) } }
                    )
                }
                ActionType.SIMPLE -> {}
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Expires", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (uiState.hasExpiration) "Yes" else "Never",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.hasExpiration,
                    onCheckedChange = { viewModel.updateHasExpiration(it) },
                    modifier = Modifier.semantics {
                        contentDescription = if (uiState.hasExpiration) "Disable expiration" else "Enable expiration"
                    }
                )
            }

            if (uiState.hasExpiration) {
                OutlinedTextField(
                    value = uiState.expirationDate?.formatDate() ?: "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Expiration date") },
                    trailingIcon = {
                        IconButton(
                            onClick = { showExpirationPicker = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Pick expiration date"
                            }
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    },
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveReminder() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.isEditing) "Save Changes" else "Save Cue")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun rememberCreateReminderViewModel(): CreateReminderViewModel {
    return viewModel()
}
