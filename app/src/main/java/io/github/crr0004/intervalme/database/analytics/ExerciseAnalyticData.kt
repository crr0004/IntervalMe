package io.github.crr0004.intervalme.database.analytics

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import io.github.crr0004.intervalme.database.routine.ExerciseData
import java.util.*

@Entity(tableName = "ExerciseAnalytic")
data class ExerciseAnalyticData(@PrimaryKey(autoGenerate = true) var id: Long=0,
                                var routineId: Long = 0,
                                var description: String = "",
                                var lastModified: Date = Date(),
                                var value0: String = "",
                                var value1: String = "",
                                var value2: String = "",
                                var isDone: Boolean = true
                               ){
    constructor(exercise: ExerciseData): this(){
        this.routineId = exercise.routineId
        this.description = exercise.description
        this.value0 = exercise.value0
        this.value1 = exercise.value1
        this.value2 = exercise.value2
    }
}