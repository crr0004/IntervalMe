package io.github.crr0004.intervalme.database.routine

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface RoutineDao{
    @Insert
    fun insert(routineTableData: RoutineTableData) : Long

    @Insert
    fun insert(routineTableData: List<ExerciseData>) : Array<Long>

    @Insert
    fun insert(exerciseData: ExerciseData) : Long

    @Query("select * from Routine where id = :id")
    fun getSyncRoutineTableById(id: Long) : RoutineTableData

    @Query("select * from Exercise where id = :id")
    fun getSyncExerciseById(id: Long) : ExerciseData

    @Query("select * from Exercise")
    fun getSyncAllExercise() : Array<ExerciseData>

    @Query("select * from Exercise where routineId = :routineId")
    fun getSyncExercisesWithRoutineId(routineId: Long) : Array<ExerciseData>

    @Update
    fun update(routineTableData: RoutineTableData)

    @Update
    fun update(routineTableData: ArrayList<ExerciseData>)

    @Query("delete from Exercise where id = :id")
    fun deleteExercise(id: Long)

    @Query("select COUNT(id) from (select id from Routine UNION ALL select id from Exercise)")
    fun getAllRoutineAndExerciseCount(): LiveData<Int>

    @Query("select COUNT(id) from (select id from Routine UNION ALL select id from Exercise)")
    fun getSyncAllRoutineAndExerciseCount() : Int

    @Query("delete from Routine where id = :id")
    fun deleteRoutine(id: Long)

    @Query("delete from Exercise where routineId = :routineId")
    fun deleteExercisesInRoutine(routineId: Long)

    @Update
    fun update(exerciseData: ExerciseData)


}