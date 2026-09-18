package com.example.docue.ui.screens.share

import android.app.Application
import android.content.Intent
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

enum class QuickTimeOption {
    TONIGHT, TOMORROW, CUSTOM
}

data class ShareUiState(
    val sharedText: String = "",
    val extractedUrl: String = "",
    val title: String = "",
    val notes: String = "",
    val quickTimeOption: QuickTimeOption = QuickTimeOption.TONIGHT,
    val customDate: LocalDate = LocalDate.now().plusDays(1),
    val customTime: LocalTime = LocalTime.of(20, 0),
    val hasUrl: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class ShareViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DoCueApplication
    private val repository = app.repository
    private val scheduler = app.scheduler

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    fun handleShareIntent(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        if (sharedText.isBlank()) {
            _uiState.update { it.copy(error = "Nothing to save.") }
            return
        }

        val url = extractUrl(sharedText)
        val hasUrl = url.isNotBlank()

        val title = if (hasUrl) {
            "Check this later"
        } else {
            sharedText.take(50)
        }

        _uiState.update {
            it.copy(
                sharedText = sharedText,
                extractedUrl = url,
                title = title,
                hasUrl = hasUrl
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, error = null) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun selectQuickTime(option: QuickTimeOption) {
        _uiState.update { it.copy(quickTimeOption = option) }
    }

    fun updateCustomDate(date: LocalDate) {
        _uiState.update { it.copy(customDate = date) }
    }

    fun updateCustomTime(time: LocalTime) {
        _uiState.update { it.copy(customTime = time) }
    }

    fun saveReminder() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a title.") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val (date, time) = when (state.quickTimeOption) {
                    QuickTimeOption.TONIGHT -> {
                        val tonightTime = LocalTime.of(22, 0)
                        if (LocalTime.now().isAfter(tonightTime)) {
                            LocalDate.now().plusDays(1) to tonightTime
                        } else {
                            LocalDate.now() to tonightTime
                        }
                    }
                    QuickTimeOption.TOMORROW -> {
                        LocalDate.now().plusDays(1) to LocalTime.of(10, 0)
                    }
                    QuickTimeOption.CUSTOM -> {
                        state.customDate to state.customTime
                    }
                }

                val dateTime = date.atTime(time)
                val timeMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                if (timeMillis <= System.currentTimeMillis()) {
                    _uiState.update { it.copy(isSaving = false, error = "Please select a future time.") }
                    return@launch
                }

                val actionType = if (state.hasUrl) ActionType.LINK else ActionType.SIMPLE
                val url = if (state.hasUrl) state.extractedUrl.trim() else ""

                val newReminder = ReminderEntity(
                    title = state.title.trim(),
                    notes = state.notes.trim(),
                    reminderTimeMillis = timeMillis,
                    repeatType = RepeatType.NONE,
                    actionType = actionType,
                    targetUrl = url,
                    isEnabled = true
                )

                val id = repository.insertReminder(newReminder)
                val savedReminder = newReminder.copy(id = id)

                scheduler.schedule(savedReminder)

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Failed to save. Please try again.") }
            }
        }
    }

    private fun extractUrl(text: String): String {
        val urlPattern = Regex("""(https?://[^\s<>"{}|\\^`\[\]]+)""")
        val match = urlPattern.find(text)
        return match?.value?.trim() ?: ""
    }
}