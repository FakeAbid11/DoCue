package com.example.docue.ui.screens.popup

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.docue.DoCueApplication
import com.example.docue.data.local.ActionType
import com.example.docue.data.local.RepeatType
import com.example.docue.data.local.ReminderEntity
import com.example.docue.scheduler.NotificationHelper
import com.example.docue.scheduler.ReminderScheduler
import com.example.docue.ui.theme.DoCueTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PopupActivity"
const val EXTRA_REMINDER_ID = "popup_reminder_id"
const val EXTRA_TITLE = "popup_title"
const val EXTRA_NOTES = "popup_notes"
const val EXTRA_ACTION_TYPE = "popup_action_type"
const val EXTRA_TARGET_PACKAGE = "popup_target_package"
const val EXTRA_TARGET_APP_NAME = "popup_target_app_name"
const val EXTRA_TARGET_URL = "popup_target_url"
const val EXTRA_REPEAT_TYPE = "popup_repeat_type"

class PopupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        if (reminderId == -1L) {
            Log.e(TAG, "No reminder ID provided, finishing")
            finish()
            return
        }

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val notes = intent.getStringExtra(EXTRA_NOTES) ?: ""
        val actionType = try {
            ActionType.valueOf(intent.getStringExtra(EXTRA_ACTION_TYPE) ?: "SIMPLE")
        } catch (_: Exception) {
            ActionType.SIMPLE
        }
        val targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""
        val targetAppName = intent.getStringExtra(EXTRA_TARGET_APP_NAME) ?: ""
        val targetUrl = intent.getStringExtra(EXTRA_TARGET_URL) ?: ""
        val repeatType = try {
            RepeatType.valueOf(intent.getStringExtra(EXTRA_REPEAT_TYPE) ?: "NONE")
        } catch (_: Exception) {
            RepeatType.NONE
        }

        setContent {
            DoCueTheme {
                PopupContent(
                    reminderId = reminderId,
                    title = title,
                    notes = notes,
                    actionType = actionType,
                    targetPackage = targetPackage,
                    targetAppName = targetAppName,
                    targetUrl = targetUrl,
                    repeatType = repeatType,
                    onDismiss = { finish() }
                )
            }
        }
    }

    companion object {
        fun createIntent(context: Context, reminder: ReminderEntity): Intent {
            return Intent(context, PopupActivity::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminder.id)
                putExtra(EXTRA_TITLE, reminder.title)
                putExtra(EXTRA_NOTES, reminder.notes)
                putExtra(EXTRA_ACTION_TYPE, reminder.actionType.name)
                putExtra(EXTRA_TARGET_PACKAGE, reminder.targetPackage)
                putExtra(EXTRA_TARGET_APP_NAME, reminder.targetAppName)
                putExtra(EXTRA_TARGET_URL, reminder.targetUrl)
                putExtra(EXTRA_REPEAT_TYPE, reminder.repeatType.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}

@Composable
private fun PopupContent(
    reminderId: Long,
    title: String,
    notes: String,
    actionType: ActionType,
    targetPackage: String,
    targetAppName: String,
    targetUrl: String,
    repeatType: RepeatType,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var secondsLeft by remember { mutableIntStateOf(60) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        for (i in 60 downTo 1) {
            secondsLeft = i
            delay(1000L)
        }
        onDismiss()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            handleOpen(context, actionType, targetPackage, targetUrl, onDismiss)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Open")
                }

                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            handleDone(context, reminderId, repeatType, onDismiss)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Done")
                }

                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            handleSnooze(context, reminderId, repeatType, onDismiss)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("Snooze")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Auto-closes in ${secondsLeft}s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

private fun handleOpen(
    context: Context,
    actionType: ActionType,
    targetPackage: String,
    targetUrl: String,
    onDismiss: () -> Unit
) {
    when (actionType) {
        ActionType.APP -> {
            if (targetPackage.isNotBlank()) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(launchIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to launch target app: $targetPackage", e)
                    }
                }
            }
        }
        ActionType.LINK -> {
            if (targetUrl.isNotBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(targetUrl))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open URL: $targetUrl", e)
                }
            }
        }
        ActionType.SIMPLE -> {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open DoCue", e)
                }
            }
        }
    }
    onDismiss()
}

private fun handleDone(
    context: Context,
    reminderId: Long,
    repeatType: RepeatType,
    onDismiss: () -> Unit
) {
    val app = context.applicationContext as DoCueApplication
    val repository = app.repository
    val scheduler = ReminderScheduler(context)

    val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
    scope.launch {
        try {
            val reminder = repository.getReminderById(reminderId)
            if (reminder != null) {
                if (repeatType == RepeatType.NONE) {
                    repository.updateReminder(reminder.copy(isEnabled = false))
                } else {
                    scheduler.scheduleNext(reminder)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling DONE for reminder $reminderId", e)
        }
    }
    onDismiss()
}

private fun handleSnooze(
    context: Context,
    reminderId: Long,
    repeatType: RepeatType,
    onDismiss: () -> Unit
) {
    val app = context.applicationContext as DoCueApplication
    val repository = app.repository
    val scheduler = ReminderScheduler(context)

    val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)
    scope.launch {
        try {
            val reminder = repository.getReminderById(reminderId)
            if (reminder != null) {
                val snoozeMillis = 10 * 60 * 1000L
                val snoozedTime = System.currentTimeMillis() + snoozeMillis
                val snoozedReminder = reminder.copy(reminderTimeMillis = snoozedTime)
                repository.updateReminder(snoozedReminder)
                scheduler.schedule(snoozedReminder)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling SNOOZE for reminder $reminderId", e)
        }
    }
    onDismiss()
}
