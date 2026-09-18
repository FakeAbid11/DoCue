package com.example.docue.scheduler

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.docue.DoCueApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "ExactAlarmPermReceiver"
private const val DIAGNOSTIC_TAG = "DoCueDiagnostic"

class ExactAlarmPermissionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) return

        Log.d(TAG, "Exact alarm permission state changed")
        Log.d(DIAGNOSTIC_TAG, "EXACT_ALARM_PERMISSION_CHANGED")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        Log.d(DIAGNOSTIC_TAG, "EXACT_ALARM_STATE: canScheduleExact=$canScheduleExact")

        if (canScheduleExact) {
            val pendingResult = goAsync()

            scope.launch {
                try {
                    val app = context.applicationContext as DoCueApplication
                    val repository = app.repository
                    val scheduler = ReminderScheduler(context)

                    val activeReminders = repository.getActiveRemindersOnce()
                    Log.d(TAG, "Permission granted, rescheduling ${activeReminders.size} active reminders")
                    Log.d(DIAGNOSTIC_TAG, "EXACT_ALARM_RESCHEDULE: rescheduling ${activeReminders.size} reminders")

                    scheduler.reconcile(activeReminders)

                    Log.d(TAG, "Reschedule complete after permission grant")
                    Log.d(DIAGNOSTIC_TAG, "EXACT_ALARM_RESCHEDULE_COMPLETE")
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling after permission change", e)
                    Log.e(DIAGNOSTIC_TAG, "EXACT_ALARM_RESCHEDULE_ERROR: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
