package io.github.crr0004.intervalme.database

import android.arch.persistence.room.TypeConverter
import java.util.*

/**
 * Created by crr00 on 24-Apr-18.
 */
class IntervalTypeConverters {
    @TypeConverter
    fun fromDate(date: Date): Long{
        return date.time
    }

    @TypeConverter
    fun toDate(time: Long): Date {
        return Date(time)
    }

    @TypeConverter
    fun toUUID(value: String?): UUID {
        return UUID.fromString(value?: "00000000-0000-0000-0000-000000000000")
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String{
        return uuid?.toString() ?: "00000000-0000-0000-0000-000000000000"
    }
}