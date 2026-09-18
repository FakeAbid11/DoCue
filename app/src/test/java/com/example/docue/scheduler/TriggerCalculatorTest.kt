package com.example.docue.scheduler

import com.example.docue.data.local.ActionType
import com.example.docue.data.local.ReminderEntity
import com.example.docue.data.local.RepeatType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TriggerCalculatorTest {

    private val zone = ZoneId.systemDefault()

    private fun createReminder(
        id: Long = 1L,
        triggerTime: LocalDateTime = LocalDateTime.now().plusHours(1),
        repeatType: RepeatType = RepeatType.NONE,
        customDays: String = "",
        expirationMillis: Long? = null,
        isEnabled: Boolean = true
    ): ReminderEntity {
        val triggerMillis = triggerTime.atZone(zone).toInstant().toEpochMilli()
        return ReminderEntity(
            id = id,
            title = "Test",
            notes = "",
            reminderTimeMillis = triggerMillis,
            repeatType = repeatType,
            customDays = customDays,
            actionType = ActionType.SIMPLE,
            targetPackage = "",
            targetAppName = "",
            targetUrl = "",
            isEnabled = isEnabled,
            expirationTimeMillis = expirationMillis,
            createdAtMillis = System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    @Test
    fun `isExpired returns true when expiration is in the past`() {
        val pastTime = System.currentTimeMillis() - 1000
        val reminder = createReminder(expirationMillis = pastTime)
        assertTrue(TriggerCalculator.isExpired(reminder))
    }

    @Test
    fun `isExpired returns false when expiration is in the future`() {
        val futureTime = System.currentTimeMillis() + 100000
        val reminder = createReminder(expirationMillis = futureTime)
        assertFalse(TriggerCalculator.isExpired(reminder))
    }

    @Test
    fun `isExpired returns false when no expiration set`() {
        val reminder = createReminder(expirationMillis = null)
        assertFalse(TriggerCalculator.isExpired(reminder))
    }

    @Test
    fun `isExpired returns true at exact expiration time`() {
        val exactTime = System.currentTimeMillis()
        val reminder = createReminder(expirationMillis = exactTime)
        assertTrue(TriggerCalculator.isExpired(reminder))
    }

    @Test
    fun `one-time reminder with future trigger returns correct millis`() {
        val future = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
        val reminder = createReminder(triggerTime = future)
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNotNull(result)
        assertEquals(
            future.atZone(zone).toInstant().toEpochMilli(),
            result
        )
    }

    @Test
    fun `one-time reminder with past trigger returns null`() {
        val past = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0)
        val reminder = createReminder(triggerTime = past)
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNull(result)
    }

    @Test
    fun `daily reminder returns next day if current time passed today`() {
        val pastToday = LocalDateTime.now().minusHours(1).withMinute(0)
        val reminder = createReminder(triggerTime = pastToday, repeatType = RepeatType.DAILY)
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNotNull(result)
        val resultDate = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate()
        val expectedDate = LocalDate.now().plusDays(1)
        assertEquals(expectedDate, resultDate)
    }

    @Test
    fun `daily reminder returns same day if time is still ahead`() {
        val futureToday = LocalDateTime.now().plusHours(2).withMinute(0)
        val reminder = createReminder(triggerTime = futureToday, repeatType = RepeatType.DAILY)
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNotNull(result)
        val resultDate = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate()
        assertEquals(LocalDate.now(), resultDate)
    }

    @Test
    fun `weekdays reminder skips weekend to Monday`() {
        val reminder = createReminder(repeatType = RepeatType.WEEKDAYS)
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNotNull(result)
        val resultDay = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate().dayOfWeek
        assertTrue(resultDay != DayOfWeek.SATURDAY && resultDay != DayOfWeek.SUNDAY)
    }

    @Test
    fun `weekends reminder lands on Saturday or Sunday`() {
        val reminder = createReminder(repeatType = RepeatType.WEEKENDS)
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNotNull(result)
        val resultDay = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate().dayOfWeek
        assertTrue(resultDay == DayOfWeek.SATURDAY || resultDay == DayOfWeek.SUNDAY)
    }

    @Test
    fun `custom days with empty string returns null`() {
        val reminder = createReminder(repeatType = RepeatType.CUSTOM_DAYS, customDays = "")
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNull(result)
    }

    @Test
    fun `expired reminder returns null`() {
        val pastExpiration = System.currentTimeMillis() - 1000
        val future = LocalDateTime.now().plusDays(1)
        val reminder = createReminder(triggerTime = future, expirationMillis = pastExpiration)
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNull(result)
    }

    @Test
    fun `weekly reminder returns same day if time ahead`() {
        val futureToday = LocalDateTime.now().plusHours(2).withMinute(0)
        val reminder = createReminder(triggerTime = futureToday, repeatType = RepeatType.WEEKLY)
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNotNull(result)
        val resultDate = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate()
        assertEquals(LocalDate.now(), resultDate)
    }

    @Test
    fun `custom days with valid days returns future date`() {
        val tomorrow = LocalDate.now().plusDays(1).dayOfWeek.value
        val reminder = createReminder(repeatType = RepeatType.CUSTOM_DAYS, customDays = "$tomorrow")
        val result = TriggerCalculator.calculateNextTriggerMillis(reminder)
        assertNotNull(result)
        val resultDay = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate().dayOfWeek
        assertEquals(DayOfWeek.of(tomorrow), resultDay)
    }
}
