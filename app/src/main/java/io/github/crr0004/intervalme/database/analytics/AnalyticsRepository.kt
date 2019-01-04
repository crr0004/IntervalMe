package io.github.crr0004.intervalme.database.analytics

import android.arch.lifecycle.LiveData
import android.content.Context
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import java.util.concurrent.Executor

class AnalyticsRepository(mContext: Context) : AnalyticsDataSourceI {

    private var mdb: IntervalMeDatabase? = null
    private var mExecutor: Executor = ThreadPerTaskExecutor()
    private val mAnalyticsDao: AnalyticsDao

    init {
        mdb = IntervalMeDatabase.getInstance(mContext)
        mAnalyticsDao = mdb!!.intervalAnalyticsDao()
    }

    override fun intervalFinish(interval: IntervalData) {
        mExecutor.execute {
            mAnalyticsDao.insert(IntervalAnalyticsData(interval))
        }
    }

    override fun intervalFinishWithProperties(interval: IntervalData, groupProperties: IntervalRunProperties) {
        mExecutor.execute {
            mAnalyticsDao.insert(IntervalAnalyticsData(interval, groupProperties))
        }
    }

    fun getAllIntervals(): LiveData<Array<IntervalAnalyticsData>>{
        return mAnalyticsDao.getAll()
    }

    override fun exerciseFinished(exerciseData: ExerciseData) {

    }

    override fun routineFinished(routine: RoutineSetData) {

    }

    override fun removeLastFinishedExercise(exerciseData: ExerciseData) {

    }

    override fun removeLastFinishedRoutine(routine: RoutineSetData) {

    }

    internal inner class ThreadPerTaskExecutor : Executor {
        override fun execute(r: Runnable) {
            Thread(r).start()
        }
    }

}