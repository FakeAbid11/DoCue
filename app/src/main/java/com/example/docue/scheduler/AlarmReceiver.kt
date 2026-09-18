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

private const val TAG = "AlarmReceiver"

class AlarmReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_ALARM_FIRE) return

        val reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1)
        if (reminderId == -1L) {
            Log.e(TAG, "Received alarm with invalid reminder ID")
            return
        }

        Log.d(TAG, "Alarm received for reminder $reminderId")

        val pendingResult = goAsync()

        scope.launch {
            try {
                val app = context.applicationContext as DoCueApplication
                val repository = app.repository
                val reminder = repository.getReminderById(reminderId)

                if (reminder == null) {
                    Log.d(TAG, "Reminder $reminderId no longer exists, ignoring stale alarm")
                    return@launch
                }

                if (!reminder.isEnabled) {
                    Log.d(TAG, "Reminder $reminderId is disabled, ignoring")
                    return@launch
                }

                val now = System.currentTimeMillis()
                val scheduler = ReminderScheduler(context)

                if (scheduler.isExpired(reminder, now)) {
                    Log.d(TAG, "Reminder $reminderId is expired, ignoring")
                    return@launch
                }

                if (reminder.repeatType == com.example.docue.data.local.RepeatType.NONE) {
                    if (reminder.reminderTimeMillis < now) {
                        val delayMinutes = (now - reminder.reminderTimeMillis) / (60 * 1000)
                        Log.d(TAG, "One-time reminder $reminderId fired $delayMinutes minutes late, ignoring")
                        return@launch
                    }
                }

                val notificationHelper = NotificationHelper(context)
                notificationHelper.createNotificationChannel()

                val canShowNotification = notificationHelper.canShowNotifications()
                if (!canShowNotification) {
                    Log.w(TAG, "Notification permission not granted for reminder $reminderId, skipping notification")
                } else {
                    notificationHelper.showReminderNotification(reminder)
                }

                scheduler.scheduleNext(reminder)

                Log.d(TAG, "Processed reminder $reminderId successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm for reminder $reminderId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
