package com.example.docue.ui.screens.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.EventNote
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docue.data.local.ActionType
import com.example.docue.ui.components.DoCueCueCard
import com.example.docue.ui.components.DoCueEmptyState
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
                title = {
                    Column {
                        Text(
                            text = viewModel.greeting,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateReminder,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.semantics {
                    contentDescription = "Create new cue"
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = reminders.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            DoCueEmptyState(
                icon = Icons.Outlined.EventNote,
                title = "All clear for today",
                subtitle = "Tap the + button to create a cue for something you don't want to forget.",
                modifier = Modifier.padding(padding)
            )
        }

        AnimatedVisibility(
            visible = reminders.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    DoCueCueCard(
                        title = reminder.title,
                        timeText = reminder.reminderTimeMillis.formatTimeFromMillis(),
                        actionLabel = when (reminder.actionType) {
                            ActionType.APP -> reminder.targetAppName.ifBlank { "Open app" }
                            ActionType.LINK -> "Open link"
                            ActionType.SIMPLE -> "Reminder"
                        },
                        actionIcon = when (reminder.actionType) {
                            ActionType.APP -> Icons.Filled.Apps
                            ActionType.LINK -> Icons.Filled.Link
                            ActionType.SIMPLE -> Icons.Filled.Notifications
                        },
                        notes = reminder.notes.ifBlank { null },
                        onClick = { onReminderClick(reminder.id) }
                    )
                }
            }
        }
    }
}
