package com.example.docue.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.docue.MainActivity
import com.example.docue.R
import com.example.docue.data.local.ActionType
import com.example.docue.data.local.ReminderEntity
import com.example.docue.ui.screens.popup.PopupActivity

private const val TAG = "NotificationHelper"
private const val CHANNEL_ID = "docue_reminders"
private const val CHANNEL_NAME = "DoCue Reminders"
private const val CHANNEL_DESCRIPTION = "Reminders from DoCue"

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun canShowNotifications(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isTargetAppAvailable(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Target app not found: $packageName")
            false
        }
    }

    fun showReminderNotification(reminder: ReminderEntity) {
        val openIntent = createOpenIntent(reminder)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = createActionIntent(reminder.id, ACTION_DONE)
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt() + 100000,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = createActionIntent(reminder.id, ACTION_SNOOZE)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt() + 200000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tapIntent = createOpenIntent(reminder)
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt() + 300000,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtitle = when (reminder.actionType) {
            ActionType.APP -> {
                if (reminder.targetPackage.isNotBlank() && !isTargetAppAvailable(reminder.targetPackage)) {
                    "App unavailable — edit this cue"
                } else {
                    "Open ${reminder.targetAppName.ifBlank { "app" }}"
                }
            }
            ActionType.LINK -> "Open link"
            ActionType.SIMPLE -> reminder.notes.ifBlank { "Time for your reminder" }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.title)
            .setContentText(subtitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "OPEN", openPendingIntent)
            .addAction(0, "DONE", donePendingIntent)
            .addAction(0, "SNOOZE", snoozePendingIntent)
            .build()

        try {
            notificationManager.notify(reminder.id.toInt(), notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to post notification for reminder ${reminder.id}: ${e.message}")
        }
    }

    fun dismissNotification(reminderId: Long) {
        notificationManager.cancel(reminderId.toInt())
    }

    fun showReminderPopup(reminder: ReminderEntity) {
        try {
            val intent = PopupActivity.createIntent(context, reminder)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "Launched popup for reminder ${reminder.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch popup for reminder ${reminder.id}, falling back to notification", e)
            showReminderNotification(reminder)
        }
    }

    private fun createOpenIntent(reminder: ReminderEntity): Intent {
        return when (reminder.actionType) {
            ActionType.APP -> {
                if (reminder.targetPackage.isNotBlank() && isTargetAppAvailable(reminder.targetPackage)) {
                    context.packageManager.getLaunchIntentForPackage(reminder.targetPackage)
                        ?: Intent().apply {
                            setClassName(context, MainActivity::class.java.name)
                            putExtra(EXTRA_OPEN_REMINDER_ID, reminder.id)
                        }
                } else {
                    Intent(context, MainActivity::class.java).apply {
                        putExtra(EXTRA_OPEN_REMINDER_ID, reminder.id)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            }
            ActionType.LINK -> {
                try {
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse(reminder.targetUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create link intent for reminder ${reminder.id}", e)
                    Intent(context, MainActivity::class.java).apply {
                        putExtra(EXTRA_OPEN_REMINDER_ID, reminder.id)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            }
            ActionType.SIMPLE -> {
                Intent(context, MainActivity::class.java).apply {
                    putExtra(EXTRA_OPEN_REMINDER_ID, reminder.id)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }
    }

    private fun createActionIntent(reminderId: Long, action: String): Intent {
        return Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
    }

    companion object {
        const val ACTION_DONE = "com.example.docue.ACTION_DONE"
        const val ACTION_SNOOZE = "com.example.docue.ACTION_SNOOZE"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_OPEN_REMINDER_ID = "extra_open_reminder_id"
    }
}
