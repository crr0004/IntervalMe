package io.github.crr0004.intervalme

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/**
 * Created by crr00 on 24-Apr-18.
 */
@RunWith(AndroidJUnit4::class)
class IntervalDataDatabaseTest {
    private var mIntervalDao: IntervalDataDOA? = null
    private var mDb: IntervalMeDatabase? = null

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        mDb = Room.inMemoryDatabaseBuilder(context, IntervalMeDatabase::class.java).build()
        mIntervalDao = mDb!!.intervalDataDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        mDb!!.close()
    }

    @Test
    fun writeIntervalTest(){
        val interval = IntervalData()
        interval.label = "test"
        interval.duration = 10 // milliseconds
        mIntervalDao?.insert(interval)
        val retrivedInterval = mIntervalDao?.get(interval.id)
        assert(interval.duration == retrivedInterval?.duration)
    }

    @Test
    fun writeManyTest(){
        val intervals = IntervalData.generate(10)
        mIntervalDao?.insert(intervals)
        val retrievedIntervals = mIntervalDao?.getAll()
        for(unit in intervals){
            assert(retrievedIntervals!!.contains(unit))
        }
    }

    @Test
    fun updateTest(){
        val interval = IntervalData()
        interval.label = "test"
        interval.duration = 10 // milliseconds
        interval.id = mIntervalDao!!.insert(interval)
        val retrievedInterval = mIntervalDao!!.get(interval.id)
        retrievedInterval.label = "hello"
        mIntervalDao!!.updateThese(retrievedInterval)
        val retrievedIntervalAgain = mIntervalDao!!.get(interval.id)
        assert(retrievedInterval.label == retrievedIntervalAgain.label)
    }

}