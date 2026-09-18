package com.example.docue.ui.screens.create

import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.docue.DoCueApplication
import com.example.docue.data.local.ActionType
import com.example.docue.data.local.ReminderEntity
import com.example.docue.data.local.RepeatType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class CreateReminderUiState(
    val title: String = "",
    val notes: String = "",
    val reminderDate: LocalDate = LocalDate.now(),
    val reminderTime: LocalTime = LocalTime.now().plusHours(1).withMinute(0),
    val repeatType: RepeatType = RepeatType.NONE,
    val customDays: Set<DayOfWeek> = emptySet(),
    val actionType: ActionType = ActionType.SIMPLE,
    val targetPackage: String = "",
    val targetAppName: String = "",
    val targetUrl: String = "",
    val targetAppAvailable: Boolean = true,
    val expirationDate: LocalDate? = null,
    val hasExpiration: Boolean = false,
    val isEditing: Boolean = false,
    val editingReminderId: Long = 0,
    val titleError: String? = null,
    val actionError: String? = null,
    val urlError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

class CreateReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DoCueApplication
    private val repository = app.repository
    private val scheduler = app.scheduler

    private val _uiState = MutableStateFlow(CreateReminderUiState())
    val uiState: StateFlow<CreateReminderUiState> = _uiState.asStateFlow()

    fun loadReminder(reminderId: Long) {
        viewModelScope.launch {
            val reminder = repository.getReminderById(reminderId) ?: return@launch
            val reminderInstant = Instant.ofEpochMilli(reminder.reminderTimeMillis)
            val reminderDateTime = reminderInstant.atZone(ZoneId.systemDefault()).toLocalDateTime()

            _uiState.update {
                it.copy(
                    title = reminder.title,
                    notes = reminder.notes,
                    reminderDate = reminderDateTime.toLocalDate(),
                    reminderTime = reminderDateTime.toLocalTime(),
                    repeatType = reminder.repeatType,
                    customDays = parseCustomDays(reminder.customDays),
                    actionType = reminder.actionType,
                    targetPackage = reminder.targetPackage,
                    targetAppName = reminder.targetAppName,
                    targetUrl = reminder.targetUrl,
                    hasExpiration = reminder.expirationTimeMillis != null,
                    expirationDate = reminder.expirationTimeMillis?.let { millis ->
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    },
                    isEditing = true,
                    editingReminderId = reminder.id
                )
            }
            checkTargetAppAvailability()
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updateDate(date: LocalDate) {
        _uiState.update { it.copy(reminderDate = date) }
    }

    fun updateTime(time: LocalTime) {
        _uiState.update { it.copy(reminderTime = time) }
    }

    fun updateRepeatType(type: RepeatType) {
        _uiState.update { it.copy(repeatType = type) }
    }

    fun toggleCustomDay(day: DayOfWeek) {
        _uiState.update {
            val newDays = if (day in it.customDays) it.customDays - day else it.customDays + day
            it.copy(customDays = newDays)
        }
    }

    fun updateActionType(type: ActionType) {
        _uiState.update { it.copy(actionType = type, actionError = null, urlError = null) }
    }

    fun updateTargetApp(packageName: String, appName: String) {
        _uiState.update { it.copy(targetPackage = packageName, targetAppName = appName, actionError = null) }
    }

    fun updateTargetUrl(url: String) {
        _uiState.update { it.copy(targetUrl = url, urlError = null) }
    }

    fun updateHasExpiration(has: Boolean) {
        _uiState.update {
            it.copy(
                hasExpiration = has,
                expirationDate = if (has) it.expirationDate ?: it.reminderDate else null
            )
        }
    }

    fun updateExpirationDate(date: LocalDate) {
        _uiState.update { it.copy(expirationDate = date) }
    }

    fun clearTargetApp() {
        _uiState.update { it.copy(targetPackage = "", targetAppName = "", targetAppAvailable = true, actionError = null) }
    }

    fun isTargetAppAvailable(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        return try {
            getApplication<Application>().packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun checkTargetAppAvailability() {
        val pkg = _uiState.value.targetPackage
        if (pkg.isNotBlank()) {
            val available = isTargetAppAvailable(pkg)
            _uiState.update { it.copy(targetAppAvailable = available) }
        }
    }

    fun deleteReminder() {
        val state = _uiState.value
        if (!state.isEditing) return

        viewModelScope.launch {
            scheduler.cancel(state.editingReminderId)
            repository.deleteReminderById(state.editingReminderId)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun saveReminder() {
        val state = _uiState.value

        val titleTrimmed = state.title.trim()
        if (titleTrimmed.isBlank()) {
            _uiState.update { it.copy(titleError = "Please enter a title.") }
            return
        }

        when (state.actionType) {
            ActionType.APP -> {
                if (state.targetPackage.isBlank()) {
                    _uiState.update { it.copy(actionError = "Choose an app to open.") }
                    return
                }
            }
            ActionType.LINK -> {
                val urlTrimmed = state.targetUrl.trim()
                if (urlTrimmed.isBlank()) {
                    _uiState.update { it.copy(urlError = "Enter a link to open.") }
                    return
                }
                val parsed = try {
                    Uri.parse(urlTrimmed)
                } catch (_: Exception) {
                    null
                }
                if (parsed == null || parsed.scheme == null ||
                    (parsed.scheme != "http" && parsed.scheme != "https" && parsed.scheme != "intent")
                ) {
                    _uiState.update { it.copy(urlError = "Enter a valid web link.") }
                    return
                }
            }
            ActionType.SIMPLE -> {}
        }

        if (state.repeatType == RepeatType.CUSTOM_DAYS && state.customDays.isEmpty()) {
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val dateTime = state.reminderDate.atTime(state.reminderTime)
            val timeMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val expirationMillis = if (state.hasExpiration && state.expirationDate != null) {
                state.expirationDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                null
            }

            val customDaysStr = if (state.repeatType == RepeatType.CUSTOM_DAYS) {
                state.customDays.sortedBy { it.value }.joinToString(",") { it.value.toString() }
            } else ""

            val now = System.currentTimeMillis()

            if (state.isEditing) {
                scheduler.cancel(state.editingReminderId)

                val existing = repository.getReminderById(state.editingReminderId)
                val updatedReminder = ReminderEntity(
                    id = state.editingReminderId,
                    title = titleTrimmed,
                    notes = state.notes.trim(),
                    reminderTimeMillis = timeMillis,
                    repeatType = state.repeatType,
                    customDays = customDaysStr,
                    actionType = state.actionType,
                    targetPackage = state.targetPackage,
                    targetAppName = state.targetAppName,
                    targetUrl = state.targetUrl.trim(),
                    isEnabled = existing?.isEnabled ?: true,
                    expirationTimeMillis = expirationMillis,
                    createdAtMillis = existing?.createdAtMillis ?: now,
                    updatedAtMillis = now
                )
                repository.updateReminder(updatedReminder)

                if (updatedReminder.isEnabled) {
                    scheduler.schedule(updatedReminder)
                }
            } else {
                val newReminder = ReminderEntity(
                    title = titleTrimmed,
                    notes = state.notes.trim(),
                    reminderTimeMillis = timeMillis,
                    repeatType = state.repeatType,
                    customDays = customDaysStr,
                    actionType = state.actionType,
                    targetPackage = state.targetPackage,
                    targetAppName = state.targetAppName,
                    targetUrl = state.targetUrl.trim(),
                    isEnabled = true,
                    expirationTimeMillis = expirationMillis,
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
                val id = repository.insertReminder(newReminder)
                val savedReminder = newReminder.copy(id = id)

                scheduler.schedule(savedReminder)
            }

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun parseCustomDays(daysStr: String): Set<DayOfWeek> {
        if (daysStr.isBlank()) return emptySet()
        return daysStr.split(",").mapNotNull { str ->
            str.trim().toIntOrNull()?.let { DayOfWeek.of(it) }
        }.toSet()
    }
}
