package com.example.docue.scheduler

import com.example.docue.data.local.ReminderEntity
import com.example.docue.data.local.RepeatType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object TriggerCalculator {

    fun calculateNextTriggerMillis(reminder: ReminderEntity): Long? {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now()
        val timeOfReminder = Instant.ofEpochMilli(reminder.reminderTimeMillis)
            .atZone(zone)
            .toLocalTime()

        reminder.expirationTimeMillis?.let { expMillis ->
            val expDateTime = Instant.ofEpochMilli(expMillis).atZone(zone).toLocalDateTime()
            if (now.isAfter(expDateTime)) return null
        }

        return when (reminder.repeatType) {
            RepeatType.NONE -> {
                val triggerMillis = reminder.reminderTimeMillis
                if (triggerMillis > System.currentTimeMillis()) triggerMillis else null
            }

            RepeatType.DAILY -> {
                var candidate = now.toLocalDate().atTime(timeOfReminder)
                if (candidate.isBefore(now) || candidate.isEqual(now)) {
                    candidate = candidate.plusDays(1)
                }
                candidate.atZone(zone).toInstant().toEpochMilli()
            }

            RepeatType.WEEKLY -> {
                val originalDate = Instant.ofEpochMilli(reminder.reminderTimeMillis)
                    .atZone(zone).toLocalDate()
                val targetDayOfWeek = originalDate.dayOfWeek

                var candidate = now.toLocalDate()
                    .with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
                    .atTime(timeOfReminder)

                if (candidate.isBefore(now) || candidate.isEqual(now)) {
                    candidate = candidate.plusWeeks(1)
                }
                candidate.atZone(zone).toInstant().toEpochMilli()
            }

            RepeatType.WEEKDAYS -> {
                var candidate = now.toLocalDate().atTime(timeOfReminder)
                if (candidate.isBefore(now) || candidate.isEqual(now)) {
                    candidate = candidate.plusDays(1)
                }
                while (candidate.dayOfWeek == DayOfWeek.SATURDAY || candidate.dayOfWeek == DayOfWeek.SUNDAY) {
                    candidate = candidate.plusDays(1)
                }
                candidate.atZone(zone).toInstant().toEpochMilli()
            }

            RepeatType.WEEKENDS -> {
                var candidate = now.toLocalDate().atTime(timeOfReminder)
                if (candidate.isBefore(now) || candidate.isEqual(now)) {
                    candidate = candidate.plusDays(1)
                }
                while (candidate.dayOfWeek != DayOfWeek.SATURDAY && candidate.dayOfWeek != DayOfWeek.SUNDAY) {
                    candidate = candidate.plusDays(1)
                }
                candidate.atZone(zone).toInstant().toEpochMilli()
            }

            RepeatType.CUSTOM_DAYS -> {
                val targetDays = reminder.customDays
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .map { DayOfWeek.of(it) }
                    .toSet()

                if (targetDays.isEmpty()) return null

                var candidate = now.toLocalDate().atTime(timeOfReminder)
                var found = false
                var attempts = 0
                while (!found && attempts < 8) {
                    if (candidate.dayOfWeek in targetDays && candidate.isAfter(now)) {
                        found = true
                    } else {
                        candidate = candidate.plusDays(1)
                        attempts++
                    }
                }

                if (found) candidate.atZone(zone).toInstant().toEpochMilli() else null
            }
        }
    }

    fun isExpired(reminder: ReminderEntity, nowMillis: Long = System.currentTimeMillis()): Boolean {
        return reminder.expirationTimeMillis != null && reminder.expirationTimeMillis <= nowMillis
    }
}
