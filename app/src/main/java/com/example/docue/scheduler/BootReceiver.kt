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

private const val TAG = "BootReceiver"

class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Receiver triggered with action: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Boot/replacement completed, reconciling active reminders")

                val pendingResult = goAsync()

                scope.launch {
                    try {
                        val app = context.applicationContext as DoCueApplication
                        val repository = app.repository
                        val scheduler = ReminderScheduler(context)

                        val activeReminders = repository.getActiveRemindersOnce()
                        Log.d(TAG, "Found ${activeReminders.size} active reminders to reconcile")

                        scheduler.reconcile(activeReminders)

                        Log.d(TAG, "Reconciliation complete after boot")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reconciling reminders after boot", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
