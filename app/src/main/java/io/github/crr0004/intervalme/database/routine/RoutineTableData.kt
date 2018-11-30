package io.github.crr0004.intervalme.database.routine

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
/**
 * Represents a routine
 */
@Entity(tableName = "Routine")
data class RoutineTableData (@PrimaryKey(autoGenerate = true) var id: Long = 0,
                             var description: String = "",
                             var isTemplate: Boolean = false,
                             var isDone: Boolean = false) {


    constructor(routine: RoutineSetData) : this(){
        this.id = routine.routineId
        this.description = routine.description
        this.isTemplate = routine.isTemplate
        this.isDone = routine.isDone
    }
}