package com.example.docue.ui.screens.create

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppPickerUiState(
    val installedApps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true
)

class AppPickerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppPickerUiState())
    val uiState: StateFlow<AppPickerUiState> = _uiState.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        val pm = getApplication<Application>().packageManager
        val selfPackage = getApplication<Application>().packageName

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(
                    PackageManager.MATCH_DEFAULT_ONLY.toLong()
                )
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        val apps = resolveInfos.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val packageName = activityInfo.packageName
            if (packageName == selfPackage) return@mapNotNull null

            val appName = try {
                resolveInfo.loadLabel(pm).toString()
            } catch (e: Exception) {
                Log.w("AppPickerVM", "Failed to load label for $packageName", e)
                activityInfo.loadLabel(pm).toString().ifBlank { packageName }
            }
            val icon = try {
                resolveInfo.loadIcon(pm)
            } catch (_: Exception) {
                null
            }

            AppInfo(
                packageName = packageName,
                appName = appName,
                icon = icon
            )
        }.sortedBy { it.appName.lowercase() }

        _uiState.update {
            it.copy(installedApps = apps, isLoading = false)
        }
    }
}
