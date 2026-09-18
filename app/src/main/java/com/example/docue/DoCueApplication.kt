package com.example.docue

import android.app.Application
import android.util.Log
import com.example.docue.data.local.DoCueDatabase
import com.example.docue.data.repository.ReminderRepository
import com.example.docue.scheduler.NotificationHelper
import com.example.docue.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "DoCueApplication"

class DoCueApplication : Application() {

    val database: DoCueDatabase by lazy { DoCueDatabase.getInstance(this) }
    val repository: ReminderRepository by lazy { ReminderRepository(database.reminderDao()) }
    val scheduler: ReminderScheduler by lazy { ReminderScheduler(this) }
    val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()

        reconcileRemindersOnStartup()
    }

    private fun reconcileRemindersOnStartup() {
        applicationScope.launch {
            try {
                Log.d(TAG, "Reconciling reminders on app startup")
                val activeReminders = repository.getActiveRemindersOnce()
                Log.d(TAG, "Found ${activeReminders.size} active reminders")
                scheduler.reconcile(activeReminders)
                Log.d(TAG, "Startup reconciliation complete")
            } catch (e: Exception) {
                Log.e(TAG, "Error during startup reconciliation", e)
            }
        }
    }
}
