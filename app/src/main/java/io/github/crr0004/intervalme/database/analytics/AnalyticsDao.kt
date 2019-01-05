package io.github.crr0004.intervalme.database.analytics

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface AnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(intervalAnalyticsData: IntervalAnalyticsData): Long

    @Query("select * from IntervalAnalytics where id = :id")
    fun get(id: Long): LiveData<IntervalAnalyticsData>

    @Query("select * from IntervalAnalytics where id = :id")
    fun syncGet(id: Long): IntervalAnalyticsData

    @Query("select * from IntervalAnalytics ORDER BY lastModified ASC")
    fun getAllIntervals(): LiveData<Array<IntervalAnalyticsData>>

    @Query("select * from RoutineAnalytic ORDER BY lastModified ASC")
    fun getAllRoutines(): LiveData<Array<RoutineAnalyticData>>

    @Query("select * from ExerciseAnalytic ORDER BY lastModified ASC")
    fun getAllExercise(): LiveData<Array<ExerciseAnalyticData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(intervalAnalyticsData: ExerciseAnalyticData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(routineAnalyticData: RoutineAnalyticData)

    @Query("select * from ExerciseAnalytic where routineId = :routineId AND description = :description AND value0 = :value0 AND value1 = :value1 AND value2 = :value2 ORDER BY lastModified DESC LIMIT 1")
    fun syncGetMatchingExercise(routineId: Long, description: String, value0: String, value1: String, value2: String): ExerciseAnalyticData?

    @Delete
    fun delete(exercise: ExerciseAnalyticData)

    @Delete
    fun delete(routine: RoutineAnalyticData)

    @Query("select * from RoutineAnalytic where description = :description ORDER BY lastModified DESC LIMIT 1")
    fun syncGetMatchRoutine(description: String): RoutineAnalyticData?

}