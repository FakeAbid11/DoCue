package com.example.docue.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.docue.DoCueApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private const val TAG = "NotificationActionReceiver"

class NotificationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1)
        if (reminderId == -1L) {
            Log.e(TAG, "Received action with invalid reminder ID")
            return
        }

        Log.d(TAG, "Notification action received: ${intent.action} for reminder $reminderId")

        val pendingResult = goAsync()

        scope.launch {
            try {
                val app = context.applicationContext as DoCueApplication
                val repository = app.repository
                val notificationHelper = NotificationHelper(context)
                val scheduler = ReminderScheduler(context)

                val reminder = repository.getReminderById(reminderId)
                if (reminder == null) {
                    Log.d(TAG, "Reminder $reminderId no longer exists")
                    notificationHelper.dismissNotification(reminderId)
                    return@launch
                }

                when (intent.action) {
                    NotificationHelper.ACTION_DONE -> {
                        Log.d(TAG, "DONE action for reminder $reminderId")
                        notificationHelper.dismissNotification(reminderId)

                        if (reminder.repeatType == com.example.docue.data.local.RepeatType.NONE) {
                            repository.updateReminder(reminder.copy(isEnabled = false))
                        } else {
                            scheduler.scheduleNext(reminder)
                        }
                    }

                    NotificationHelper.ACTION_SNOOZE -> {
                        Log.d(TAG, "SNOOZE action for reminder $reminderId")
                        notificationHelper.dismissNotification(reminderId)

                        val snoozeMillis = 10 * 60 * 1000L
                        val snoozedTime = System.currentTimeMillis() + snoozeMillis
                        val snoozedReminder = reminder.copy(reminderTimeMillis = snoozedTime)
                        repository.updateReminder(snoozedReminder)
                        scheduler.schedule(snoozedReminder)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling notification action for reminder $reminderId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
