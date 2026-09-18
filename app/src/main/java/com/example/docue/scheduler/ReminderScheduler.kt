package com.example.docue.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.docue.data.local.ReminderEntity
import com.example.docue.data.local.RepeatType
import java.time.Instant

private const val TAG = "ReminderScheduler"

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: ReminderEntity) {
        val triggerMillis = calculateNextTriggerMillis(reminder) ?: run {
            Log.d(TAG, "No future trigger for reminder ${reminder.id}, skipping")
            return
        }

        if (triggerMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Trigger time is in the past for reminder ${reminder.id}, skipping")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_FIRE
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder ${reminder.id} for ${Instant.ofEpochMilli(triggerMillis)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule exact alarm for reminder ${reminder.id}", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancel(reminderId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_FIRE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled alarm for reminder $reminderId")
    }

    fun scheduleNext(reminder: ReminderEntity) {
        if (reminder.repeatType == RepeatType.NONE) {
            Log.d(TAG, "One-time reminder ${reminder.id} triggered, not rescheduling")
            return
        }

        schedule(reminder)
    }

    internal fun calculateNextTriggerMillis(reminder: ReminderEntity): Long? =
        TriggerCalculator.calculateNextTriggerMillis(reminder)

    fun rescheduleAll(activeReminders: List<ReminderEntity>) {
        activeReminders.forEach { reminder ->
            if (reminder.isEnabled) {
                schedule(reminder)
            }
        }
    }

    fun reconcile(activeReminders: List<ReminderEntity>) {
        Log.d(TAG, "Starting reconciliation for ${activeReminders.size} reminders")

        val now = System.currentTimeMillis()

        for (reminder in activeReminders) {
            if (!reminder.isEnabled) {
                Log.d(TAG, "Reminder ${reminder.id} is disabled, cancelling alarm")
                cancel(reminder.id)
                continue
            }

            if (TriggerCalculator.isExpired(reminder, now)) {
                Log.d(TAG, "Reminder ${reminder.id} is expired, cancelling alarm")
                cancel(reminder.id)
                continue
            }

            val nextTrigger = calculateNextTriggerMillis(reminder)
            if (nextTrigger == null) {
                Log.d(TAG, "Reminder ${reminder.id} has no future trigger, cancelling alarm")
                cancel(reminder.id)
                continue
            }

            if (nextTrigger <= now) {
                Log.d(TAG, "Reminder ${reminder.id} next trigger is in the past, cancelling alarm")
                cancel(reminder.id)
                continue
            }

            Log.d(TAG, "Reminder ${reminder.id} reconciled, scheduling for ${Instant.ofEpochMilli(nextTrigger)}")
            schedule(reminder)
        }
    }

    fun isExpired(reminder: ReminderEntity, nowMillis: Long = System.currentTimeMillis()): Boolean =
        TriggerCalculator.isExpired(reminder, nowMillis)

    companion object {
        const val ACTION_ALARM_FIRE = "com.example.docue.ACTION_ALARM_FIRE"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
    }
}
