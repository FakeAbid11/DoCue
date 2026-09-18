package com.example.docue.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.docue.DoCueApplication
import com.example.docue.data.local.RepeatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "AlarmReceiver"
private const val DIAGNOSTIC_TAG = "DoCueDiagnostic"

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
        Log.d(DIAGNOSTIC_TAG, "ALARM_RECEIVED: reminderId=$reminderId")

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

                val delayMinutes = if (reminder.reminderTimeMillis < now) {
                    (now - reminder.reminderTimeMillis) / (60 * 1000)
                } else {
                    0L
                }
                if (delayMinutes > 0) {
                    Log.d(DIAGNOSTIC_TAG, "ALARM_DELAYED: reminder $reminderId fired $delayMinutes minutes late")
                }

                val notificationHelper = NotificationHelper(context)

                notificationHelper.showReminderPopup(reminder)
                Log.d(DIAGNOSTIC_TAG, "POPUP_LAUNCHED: reminder $reminderId (${reminder.title})")

                scheduler.scheduleNext(reminder)

                Log.d(TAG, "Processed reminder $reminderId successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm for reminder $reminderId", e)
                Log.e(DIAGNOSTIC_TAG, "ALARM_ERROR: reminder $reminderId - ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
