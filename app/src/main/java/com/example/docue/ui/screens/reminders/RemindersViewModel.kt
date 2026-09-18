package com.example.docue.ui.screens.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.docue.DoCueApplication
import com.example.docue.data.local.ReminderEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DoCueApplication
    private val repository = app.repository
    private val scheduler = app.scheduler

    val allReminders: StateFlow<List<ReminderEntity>> =
        repository.allReminders
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            scheduler.cancel(id)
            repository.deleteReminderById(id)
        }
    }

    fun toggleEnabled(reminder: ReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            repository.updateReminder(updated)

            if (updated.isEnabled) {
                scheduler.schedule(updated)
            } else {
                scheduler.cancel(updated.id)
            }
        }
    }
}
