package com.example.docue.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val fullFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")

fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun LocalDateTime.formatFull(): String = format(fullFormatter)

fun LocalDate.formatDate(): String = format(dateFormatter)

fun LocalTime.formatTime(): String = format(timeFormatter)

fun Long.formatTimeFromMillis(): String {
    return toLocalDateTime().toLocalTime().formatTime()
}

fun Long.formatDateFromMillis(): String {
    return toLocalDateTime().toLocalDate().formatDate()
}

fun Long.formatFullFromMillis(): String {
    return toLocalDateTime().formatFull()
}
