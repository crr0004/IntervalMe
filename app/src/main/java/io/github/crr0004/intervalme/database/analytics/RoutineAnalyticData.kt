package io.github.crr0004.intervalme.database.analytics

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import io.github.crr0004.intervalme.database.routine.RoutineTableData
import java.util.*

@Entity(tableName = "RoutineAnalytic")
data class RoutineAnalyticData(@PrimaryKey(autoGenerate = true) var id: Long=0,
                                var description: String = "",
                                var lastModified: Date = Date()
){
    constructor(routineSetData: RoutineSetData): this(){
        this.description = routineSetData.description
    }
    constructor(routineTableData: RoutineTableData): this(){
        this.description = routineTableData.description
    }
}