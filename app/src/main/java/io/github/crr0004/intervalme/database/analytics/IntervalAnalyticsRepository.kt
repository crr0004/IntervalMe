package io.github.crr0004.intervalme.database.analytics

import android.content.Context
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.IntervalRunProperties
import java.util.concurrent.Executor

class IntervalAnalyticsRepository(mContext: Context) : IntervalAnalyticsDataSourceI {


    private var mdb: IntervalMeDatabase? = null
    private var mExecutor: Executor = ThreadPerTaskExecutor()
    private val mIntervalAnalyticsDao: IntervalAnalyticsDao

    init {
        mdb = IntervalMeDatabase.getInstance(mContext)
        mIntervalAnalyticsDao = mdb!!.intervalAnalyticsDao()
    }

    override fun intevalFinish(interval: IntervalData) {
        mExecutor.execute {
            mIntervalAnalyticsDao.insert(IntervalAnalyticsData(interval))
        }
    }

    override fun intevalFinishWithProperties(interval: IntervalData, groupProperties: IntervalRunProperties) {
        mExecutor.execute {
            mIntervalAnalyticsDao.insert(IntervalAnalyticsData(interval, groupProperties))
        }
    }

    internal inner class ThreadPerTaskExecutor : Executor {
        override fun execute(r: Runnable) {
            Thread(r).start()
        }
    }

}