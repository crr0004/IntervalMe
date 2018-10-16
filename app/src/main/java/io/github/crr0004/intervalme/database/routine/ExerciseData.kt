package io.github.crr0004.intervalme.database.routine

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import io.github.crr0004.intervalme.database.IntervalData
import java.util.*

/**
 * Represents a single exercise that belongs to a routine
 */
@Entity(tableName = "Exercise",
        foreignKeys = [(ForeignKey(entity = RoutineTableData::class, parentColumns = arrayOf("id"), childColumns = arrayOf("routineId")))])
data class ExerciseData(@PrimaryKey(autoGenerate = true) var id: Long = 0,
                        var routineId: Long = 0,
                        var description: String = "",
                        var lastModified: Date = Date(),
                        var value0: String = "",
                        var value1: String = "",
                        var value2: String = "") {

}
