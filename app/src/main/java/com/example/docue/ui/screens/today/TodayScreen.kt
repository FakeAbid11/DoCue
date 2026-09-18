package com.example.docue.ui.screens.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docue.data.local.ActionType
import com.example.docue.ui.components.EmptyState
import com.example.docue.util.formatTimeFromMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onCreateReminder: () -> Unit,
    onReminderClick: (Long) -> Unit,
    viewModel: TodayViewModel = viewModel()
) {
    val reminders by viewModel.todayReminders.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Today") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateReminder,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.semantics {
                    contentDescription = "Create new reminder"
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Today,
                title = "Nothing to do yet",
                subtitle = "Tap + to create a cue for something you don't want to forget.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReminderClick(reminder.id) }
                            .semantics {
                                contentDescription = buildString {
                                    append(reminder.title)
                                    append(". ")
                                    append(reminder.reminderTimeMillis.formatTimeFromMillis())
                                    append(". ")
                                    append(
                                        when (reminder.actionType) {
                                            ActionType.APP -> "Opens ${reminder.targetAppName.ifBlank { "an app" }}"
                                            ActionType.LINK -> "Opens a link"
                                            ActionType.SIMPLE -> "Reminder"
                                        }
                                    )
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = reminder.reminderTimeMillis.formatTimeFromMillis(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                ActionChip(actionType = reminder.actionType)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (reminder.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = reminder.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(actionType: ActionType) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics {
            contentDescription = when (actionType) {
                ActionType.APP -> "App cue"
                ActionType.LINK -> "Link cue"
                ActionType.SIMPLE -> "Simple reminder"
            }
        }
    ) {
        Icon(
            imageVector = when (actionType) {
                ActionType.APP -> Icons.Default.Apps
                ActionType.LINK -> Icons.Default.Link
                ActionType.SIMPLE -> Icons.Default.Notifications
            },
            contentDescription = null,
            modifier = Modifier.padding(end = 4.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = when (actionType) {
                ActionType.APP -> "Open app"
                ActionType.LINK -> "Open link"
                ActionType.SIMPLE -> "Remind"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
