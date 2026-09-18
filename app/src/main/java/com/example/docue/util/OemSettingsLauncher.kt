package com.example.docue.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

private const val TAG = "OemSettingsLauncher"

enum class ManufacturerFamily {
    XIAOMI,
    OPPO,
    VIVO,
    HUAWEI,
    SAMSUNG,
    GOOGLE,
    GENERIC
}

object OemSettingsLauncher {

    val manufacturerFamily: ManufacturerFamily
        get() = detectManufacturerFamily()

    val manufacturerDisplayName: String
        get() = normalizeManufacturerName()

    fun launchAutoStartSettings(context: Context): Boolean {
        val intent = createAutoStartIntent(context)
        return launchSafely(context, intent)
    }

    fun launchBatterySettings(context: Context): Boolean {
        val intent = createBatteryIntent(context)
        return launchSafely(context, intent)
    }

    fun launchAppInfoSettings(context: Context): Boolean {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return launchSafely(context, intent)
    }

    private fun detectManufacturerFamily(): ManufacturerFamily {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") ->
                ManufacturerFamily.XIAOMI
            manufacturer.contains("oppo") || manufacturer.contains("oneplus") || manufacturer.contains("realme") ->
                ManufacturerFamily.OPPO
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") ->
                ManufacturerFamily.VIVO
            manufacturer.contains("huawei") || manufacturer.contains("honor") ->
                ManufacturerFamily.HUAWEI
            manufacturer.contains("samsung") ->
                ManufacturerFamily.SAMSUNG
            manufacturer.contains("google") || manufacturer.contains("pixel") ->
                ManufacturerFamily.GOOGLE
            else ->
                ManufacturerFamily.GENERIC
        }
    }

    private fun normalizeManufacturerName(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") -> "Xiaomi"
            manufacturer.contains("redmi") -> "Xiaomi (Redmi)"
            manufacturer.contains("poco") -> "Xiaomi (POCO)"
            manufacturer.contains("oppo") -> "OPPO"
            manufacturer.contains("oneplus") -> "OnePlus"
            manufacturer.contains("realme") -> "Realme"
            manufacturer.contains("vivo") -> "vivo"
            manufacturer.contains("iqoo") -> "iQOO"
            manufacturer.contains("huawei") -> "Huawei"
            manufacturer.contains("honor") -> "Honor"
            manufacturer.contains("samsung") -> "Samsung"
            manufacturer.contains("google") || manufacturer.contains("pixel") -> "Google"
            manufacturer.contains("motorola") -> "Motorola"
            manufacturer.contains("asus") -> "ASUS"
            manufacturer.contains("nothing") -> "Nothing"
            else -> Build.MANUFACTURER.ifBlank { "Unknown" }
        }
    }

    private fun createAutoStartIntent(context: Context): Intent? {
        return when (manufacturerFamily) {
            ManufacturerFamily.XIAOMI -> {
                try {
                    Intent().apply {
                        setClassName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                } catch (_: Exception) {
                    try {
                        Intent().apply {
                            setClassName(
                                "com.miui.securitycenter",
                                "com.miui.powercenter.PowerSettings"
                            )
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    } catch (_: Exception) {
                        createAppInfoIntent(context)
                    }
                }
            }
            ManufacturerFamily.HUAWEI -> {
                try {
                    Intent().apply {
                        setClassName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                } catch (_: Exception) {
                    createAppInfoIntent(context)
                }
            }
            ManufacturerFamily.OPPO -> {
                try {
                    Intent().apply {
                        setClassName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.startupapp.StartupAppListActivity"
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                } catch (_: Exception) {
                    try {
                        Intent().apply {
                            setClassName(
                                "com.oppo.safe",
                                "com.oppo.safe.permission.startup.StartupAppListActivity"
                            )
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    } catch (_: Exception) {
                        createAppInfoIntent(context)
                    }
                }
            }
            ManufacturerFamily.VIVO -> {
                try {
                    Intent().apply {
                        setClassName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                } catch (_: Exception) {
                    createAppInfoIntent(context)
                }
            }
            ManufacturerFamily.SAMSUNG -> {
                createAppInfoIntent(context)
            }
            ManufacturerFamily.GOOGLE -> {
                createAppInfoIntent(context)
            }
            ManufacturerFamily.GENERIC -> {
                createAppInfoIntent(context)
            }
        }
    }

    private fun createBatteryIntent(context: Context): Intent {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                createAppInfoIntent(context)
            }
        } catch (_: Exception) {
            createAppInfoIntent(context)
        }
    }

    private fun createAppInfoIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun launchSafely(context: Context, intent: Intent?): Boolean {
        if (intent == null) return false
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to launch intent: ${e.message}")
            false
        }
    }
}
