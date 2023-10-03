package com.nhlynn.news_compose.utils

import java.lang.Exception
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun dateFormatter(inputDate: String?): String {
    val inputFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val outputFormat = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.getDefault())
    return try {
        val dateTime = OffsetDateTime.parse(inputDate, inputFormat)
        dateTime.format(outputFormat)
    } catch (e: Exception) {
        ""
    }
}