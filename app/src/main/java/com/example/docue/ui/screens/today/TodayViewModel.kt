package com.example.docue.ui.screens.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.docue.DoCueApplication
import com.example.docue.data.local.ReminderEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as DoCueApplication
    private val repository = app.repository
    private val scheduler = app.scheduler

    private val zone = ZoneId.systemDefault()
    private val todayStart = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
    private val todayEnd = LocalDate.now().atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()

    val todayReminders: StateFlow<List<ReminderEntity>> =
        repository.getRemindersBetween(todayStart, todayEnd)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val greeting: String
        get() {
            val hour = LocalTime.now().hour
            return when {
                hour < 12 -> "Good morning"
                hour < 17 -> "Good afternoon"
                else -> "Good evening"
            }
        }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            scheduler.cancel(id)
            repository.deleteReminderById(id)
        }
    }
}
