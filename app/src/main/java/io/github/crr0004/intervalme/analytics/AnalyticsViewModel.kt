package io.github.crr0004.intervalme.analytics

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsData
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsRepository

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val intervals = IntervalAnalyticsRepository(application)

    fun getAll(): LiveData<Array<IntervalAnalyticsData>>{
        return intervals.getAll()
    }
}