package com.example.docue.ui.screens.settings

import android.app.Application
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.docue.ui.theme.ThemeMode
import com.example.docue.data.local.ReminderDao
import com.example.docue.scheduler.ReminderScheduler
import com.example.docue.util.OemSettingsLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReliabilityState(
    val notificationsEnabled: Boolean = true,
    val exactAlarmsAllowed: Boolean = true,
    val batteryOptimizationRestricted: Boolean = false
)

data class BackgroundProtectionState(
    val manufacturerDisplayName: String = "Unknown",
    val batteryOptimizationRestricted: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("docue_settings", 0)
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val dao: ReminderDao by lazy {
        com.example.docue.data.local.DoCueDatabase.getInstance(application).reminderDao()
    }
    private val scheduler by lazy { ReminderScheduler(application) }

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _reliabilityState = MutableStateFlow(loadReliabilityState())
    val reliabilityState: StateFlow<ReliabilityState> = _reliabilityState.asStateFlow()

    private val _backgroundProtectionState = MutableStateFlow(loadBackgroundProtectionState())
    val backgroundProtectionState: StateFlow<BackgroundProtectionState> = _backgroundProtectionState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun refreshReliabilityState() {
        _reliabilityState.value = loadReliabilityState()
        _backgroundProtectionState.value = loadBackgroundProtectionState()
    }

    fun openNotificationSettings() {
        val context = getApplication<Application>()
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openExactAlarmSettings() {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openBatteryOptimizationSettings() {
        val context = getApplication<Application>()
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openAutoStartSettings() {
        val context = getApplication<Application>()
        OemSettingsLauncher.launchAutoStartSettings(context)
    }

    fun openBatterySettings() {
        val context = getApplication<Application>()
        OemSettingsLauncher.launchBatterySettings(context)
    }

    fun openAppInfoSettings() {
        val context = getApplication<Application>()
        OemSettingsLauncher.launchAppInfoSettings(context)
    }

    fun deleteAllReminders() {
        viewModelScope.launch {
            val allReminders = withContext(Dispatchers.IO) { dao.getAllOnce() }
            allReminders.forEach { scheduler.cancel(it.id) }
            withContext(Dispatchers.IO) { dao.deleteAll() }
        }
    }

    private fun loadReliabilityState(): ReliabilityState {
        val context = getApplication<Application>()
        return ReliabilityState(
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            exactAlarmsAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            },
            batteryOptimizationRestricted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                !powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                false
            }
        )
    }

    private fun loadBackgroundProtectionState(): BackgroundProtectionState {
        val context = getApplication<Application>()
        return BackgroundProtectionState(
            manufacturerDisplayName = OemSettingsLauncher.manufacturerDisplayName,
            batteryOptimizationRestricted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                !powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                false
            }
        )
    }

    private fun loadThemeMode(): ThemeMode {
        val saved = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(saved!!)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }
}
