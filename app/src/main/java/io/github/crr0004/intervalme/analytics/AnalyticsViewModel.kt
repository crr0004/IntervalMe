package io.github.crr0004.intervalme.analytics

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsData
import io.github.crr0004.intervalme.database.analytics.AnalyticsRepository
import io.github.crr0004.intervalme.database.analytics.ExerciseAnalyticData
import io.github.crr0004.intervalme.database.analytics.RoutineAnalyticData

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val analyticsRepository = AnalyticsRepository(application)

    fun getAllIntervals(): LiveData<Array<IntervalAnalyticsData>>{
        return analyticsRepository.getAllIntervals()
    }

    fun getAllRoutines(): LiveData<Array<RoutineAnalyticData>>{
        return analyticsRepository.getAllRoutines()
    }

    fun getAllExercise(): LiveData<Array<ExerciseAnalyticData>>{
        return analyticsRepository.getAllExercise()
    }
}