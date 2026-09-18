package com.example.docue.domain.model

import com.example.docue.data.local.ActionType
import com.example.docue.data.local.RepeatType

data class Reminder(
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
