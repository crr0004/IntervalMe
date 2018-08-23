package io.github.crr0004.intervalme.database.analytics

import android.content.Context
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import java.util.concurrent.Executor

class IntervalAnalyticsRepository(mContext: Context) : IntervalAnalyticsDataSourceI {
    private var mdb: IntervalMeDatabase? = null
    private var mExecutor: Executor = ThreadPerTaskExecutor()

    init {
        mdb = IntervalMeDatabase.getInstance(mContext)
    }

    internal inner class ThreadPerTaskExecutor : Executor {
        override fun execute(r: Runnable) {
            Thread(r).start()
        }
    }

}