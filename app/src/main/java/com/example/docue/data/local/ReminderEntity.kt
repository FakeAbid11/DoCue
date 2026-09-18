package com.example.docue.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActionType {
    SIMPLE,
    APP,
    LINK
}

enum class RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    WEEKDAYS,
    WEEKENDS,
    CUSTOM_DAYS
}

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val reminderTimeMillis: Long,
    val repeatType: RepeatType = RepeatType.NONE,
    val customDays: String = "",
    val actionType: ActionType = ActionType.SIMPLE,
    val targetPackage: String = "",
    val targetAppName: String = "",
    val targetUrl: String = "",
    val isEnabled: Boolean = true,
    val expirationTimeMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
