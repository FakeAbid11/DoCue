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

private const val TAG = "TimeChangeReceiver"

class TimeChangeReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Time change received: $action")

        when (action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                val pendingResult = goAsync()

                scope.launch {
                    try {
                        val app = context.applicationContext as DoCueApplication
                        val repository = app.repository
                        val scheduler = ReminderScheduler(context)

                        val activeReminders = repository.getActiveRemindersOnce()
                        Log.d(TAG, "Reconciling ${activeReminders.size} active reminders after time change")

                        scheduler.reconcile(activeReminders)

                        Log.d(TAG, "Reconciliation complete after time change")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reconciling reminders after time change", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
