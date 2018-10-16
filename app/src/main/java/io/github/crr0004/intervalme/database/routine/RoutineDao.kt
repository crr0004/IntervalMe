package io.github.crr0004.intervalme.database.routine

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface RoutineDao{
    @Insert
    fun insert(routineTableData: RoutineTableData) : Long

    @Insert
    fun insert(routineTableData: List<ExerciseData>) : Array<Long>

    @Query("select * from Routine where id = :id")
    fun getSyncRoutineTableById(id: Long) : RoutineTableData

    @Query("select * from Exercise where id = :id")
    fun getSyncExerciseById(id: Long) : ExerciseData

    @Query("select * from Exercise")
    fun getSyncAllExercise() : Array<ExerciseData>

    @Query("select * from Exercise where routineId = :routineId")
    fun getSyncExercisesWithRoutineId(routineId: Long) : Array<ExerciseData>


}