package com.example.docue.data.repository

import com.example.docue.data.local.ReminderDao
import com.example.docue.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    fun getRemindersBetween(startMillis: Long, endMillis: Long): Flow<List<ReminderEntity>> {
        return reminderDao.getRemindersBetween(startMillis, endMillis)
    }

    fun getActiveReminders(): Flow<List<ReminderEntity>> {
        return reminderDao.getActiveReminders()
    }

    suspend fun getActiveRemindersOnce(): List<ReminderEntity> {
        return reminderDao.getActiveRemindersOnce()
    }

    suspend fun getReminderById(id: Long): ReminderEntity? {
        return reminderDao.getReminderById(id)
    }

    suspend fun insertReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }
}
