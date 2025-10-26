package com.medicationadherence.app.data.local.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Type converters for LocalDateTime
 */
class DateTimeConverters {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { 
            try {
                LocalDateTime.parse(it)
            } catch (e: Exception) {
                // Handle space-separated datetime format (e.g., "2025-10-26 16:33:56")
                try {
                    val normalized = it.replace(" ", "T")
                    LocalDateTime.parse(normalized)
                } catch (e2: Exception) {
                    null
                }
            }
        }
    }
}

/**
 * Type converters for List<String>
 */
class ListConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Json.decodeFromString(value)
    }
}
