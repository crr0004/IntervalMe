package io.github.crr0004.intervalme.database.analytics

import android.arch.lifecycle.LiveData
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineSetData

interface AnalyticsDataSourceI {
    fun intervalFinish(interval: IntervalData)
    fun intervalFinishWithProperties(interval: IntervalData, groupProperties: IntervalRunProperties)
    fun exerciseFinished(exerciseData: ExerciseData)
    fun routineFinished(routine: RoutineSetData)
    fun removeLastFinishedExercise(exerciseData: ExerciseData)
    fun removeLastFinishedRoutine(routine: RoutineSetData)
    fun getAllRoutines(): LiveData<Array<RoutineAnalyticData>>
    fun getAllExercise(): LiveData<Array<ExerciseAnalyticData>>
}