package com.example.docue.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY reminderTimeMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE reminderTimeMillis >= :startMillis AND reminderTimeMillis <= :endMillis AND isEnabled = 1 ORDER BY reminderTimeMillis ASC")
    fun getRemindersBetween(startMillis: Long, endMillis: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND (expirationTimeMillis IS NULL OR expirationTimeMillis > :nowMillis) ORDER BY reminderTimeMillis ASC")
    fun getActiveReminders(nowMillis: Long = System.currentTimeMillis()): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND (expirationTimeMillis IS NULL OR expirationTimeMillis > :nowMillis) ORDER BY reminderTimeMillis ASC")
    suspend fun getActiveRemindersOnce(nowMillis: Long = System.currentTimeMillis()): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("SELECT * FROM reminders")
    suspend fun getAllOnce(): List<ReminderEntity>

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
}
