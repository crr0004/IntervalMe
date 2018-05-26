package io.github.crr0004.intervalme

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import org.junit.After
import org.junit.Assert
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
        mDb = IntervalMeDatabase.getTemporaryInstance(context)
        mIntervalDao = mDb!!.intervalDataDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        mDb!!.close()
    }

    @Test
    fun writeIntervalTest(){
        val interval = IntervalData(label = "test")
        val id: Long
        id = mIntervalDao!!.insert(interval)
        val retrievedInterval: IntervalData? = mIntervalDao!!.get(id)
        Assert.assertEquals(interval.group, retrievedInterval?.group)
    }

    @Test
    fun writeManyTest(){
        val intervals = IntervalData.generate(10)
        val ids = mIntervalDao?.insert(intervals)
        val retrievedIntervals = mIntervalDao?.getAll()
        //This only works by comparing groups because they are all top level.
        //Child intervals have the same group as parent so wouldn't work
        for(i in 0 until intervals.size){
            Assert.assertEquals("Write many failed on i: $i",
                    retrievedIntervals!![i].group, intervals[i]!!.group)
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
        Assert.assertTrue(retrievedInterval.label == retrievedIntervalAgain.label)
    }

    /**
     * Testing that you can get children of a groups by the index in the group
     * I.E the 2nd child in a group
     */
    @Test
    fun getGroupChildByIndex(){

        /*
        groupPosition: Int, childPosition: Int

        val intervalDataParent = mIntervalDao?.getGroupOwners()?.get(groupPosition)
        val childrenOfGroup = mIntervalDao?.getAllOfGroupWithoutOwner(intervalDataParent?.group ?: UUID.fromString("00000000-0000-0000-0000-000000000000"))
        */

        //Generate a bunch of input
        val intervalInput = IntervalData.generate(10)
        mIntervalDao!!.insert(intervalInput)
        mIntervalDao!!.insert(IntervalData.generate(3, intervalInput[0]))

        val intervalTestInput = IntervalData.generate(3)
        mIntervalDao!!.insert(intervalTestInput)

        //What we're testing against
        val intervalTestChildInput = IntervalData.generate(3, intervalInput[3])
        for((index, id) in mIntervalDao!!.insert(intervalTestChildInput).withIndex()){
            intervalTestChildInput[index]!!.id = id
        }
        mIntervalDao!!.insert(IntervalData.generate(3, intervalInput[5]))

        var intervalTestChildOut = mIntervalDao!!.getChildOfGroupByOffset(
                1, intervalInput[3]!!.group)
        Assert.assertEquals(intervalTestChildOut.id, intervalTestChildInput[0]!!.id)

        intervalTestChildOut = mIntervalDao!!.getChildOfGroupByOffset(
                2, intervalInput[3]!!.group)
        Assert.assertEquals(intervalTestChildOut.id, intervalTestChildInput[1]!!.id)

        intervalTestChildOut = mIntervalDao!!.getChildOfGroupByOffset(
                3, intervalInput[3]!!.group)
        Assert.assertEquals(intervalTestChildOut.id, intervalTestChildInput[2]!!.id)
    }

    /**
     * Testing that you can a group by the index
     * This needs to account for that IDs don't represent the index
     */
    @Test
    fun getGroupByIndex(){
        val intervalInput = IntervalData.generate(10)
        mIntervalDao!!.insert(intervalInput)
        mIntervalDao!!.insert(IntervalData.generate(3, intervalInput[0]))
        //Retrieve the 14,15,16 interval by index
        val intervalTestInput = IntervalData.generate(3)
        mIntervalDao!!.insert(intervalTestInput)
        mIntervalDao!!.insert(IntervalData.generate(3, intervalInput[3]))
        mIntervalDao!!.insert(IntervalData.generate(3, intervalInput[5]))

        var intervalTestOut = mIntervalDao!!.getGroupByOffset(11)
        Assert.assertEquals(intervalTestOut.group, intervalTestInput[0]!!.group)
        intervalTestOut = mIntervalDao!!.getGroupByOffset(12)
        Assert.assertEquals(intervalTestOut.group, intervalTestInput[1]!!.group)
        intervalTestOut = mIntervalDao!!.getGroupByOffset(13)
        Assert.assertEquals(intervalTestOut.group, intervalTestInput[2]!!.group)

    }

}