package io.github.crr0004.intervalme

import android.arch.lifecycle.Observer
import android.os.Handler
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.IntervalRepository
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor

@RunWith(AndroidJUnit4::class)
class IntervalRepositoryTest: ActivityTestRule<IntervalListActivity>(IntervalListActivity::class.java) {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupDB(){
            IntervalMeDatabase.USING_TEMP_DATABASE = true
            //IntervalMeDatabase.getTemporaryInstance()?.intervalDataDao()?.insert(IntervalData.generate(5))
        }
        @AfterClass
        @JvmStatic
        fun destroyDB(){
            IntervalMeDatabase.destroyInstance()
        }
    }

    private var mRepo: IntervalRepository? = null
    @get:Rule
    public var mActivityRule: ActivityTestRule<IntervalListActivity> = this

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        mRepo = IntervalRepository(context)

    }

    @Test
    fun getGroupChildByIndex(){

        /*
        groupPosition: Int, childPosition: Int

        val intervalDataParent = mIntervalDao?.getGroupOwners()?.get(groupPosition)
        val childrenOfGroup = mIntervalDao?.getAllOfGroupWithoutOwner(intervalDataParent?.group ?: UUID.fromString("00000000-0000-0000-0000-000000000000"))
        */
        mRepo!!.setExecutorToSync()

        //Generate a bunch of input
        val intervalInput = IntervalData.generate(10)
        mRepo!!.insert(intervalInput)
        mRepo!!.insert(IntervalData.generate(3, intervalInput[0]))

        val intervalTestInput = IntervalData.generate(3)
        mRepo!!.insert(intervalTestInput)

        //What we're testing against
        val intervalTestChildInput = IntervalData.generate(3, intervalInput[3])
        for((index, id) in mRepo!!.insert(intervalTestChildInput).withIndex()){
            intervalTestChildInput[index]!!.id = id
        }
        mRepo!!.insert(IntervalData.generate(3, intervalInput[5]))


        mRepo!!.buildGroupAndChildOffsetCache()

        var intervalTestChildOut = mRepo!!.getChildOfGroupByOffset(
                1, intervalInput[3]!!.group)
        Assert.assertEquals(intervalTestChildOut.id, intervalTestChildInput[0]!!.id)

        intervalTestChildOut = mRepo!!.getChildOfGroupByOffset(
                2, intervalInput[3]!!.group)
        Assert.assertEquals(intervalTestChildOut.id, intervalTestChildInput[1]!!.id)

        intervalTestChildOut = mRepo!!.getChildOfGroupByOffset(
                3, intervalInput[3]!!.group)
        Assert.assertEquals(intervalTestChildOut.id, intervalTestChildInput[2]!!.id)
    }

    @Test
    fun getGroupByOffsetTest(){
        mRepo!!.setExecutorToSync()
        val intervalInput = IntervalData.generate(10)
        mRepo!!.insert(intervalInput)
        mRepo!!.insert(IntervalData.generate(3, intervalInput[0]))
        //Retrieve the 14,15,16 interval by index
        val intervalTestInput = IntervalData.generate(3)
        mRepo!!.insert(intervalTestInput)
        mRepo!!.insert(IntervalData.generate(3, intervalInput[3]))
        mRepo!!.insert(IntervalData.generate(3, intervalInput[5]))

        mRepo!!.buildGroupAndChildOffsetCache()

        var intervalTestOut = mRepo!!.getGroupByOffset(11)
        Assert.assertEquals(intervalTestOut.group, intervalTestInput[0]!!.group)
        intervalTestOut = mRepo!!.getGroupByOffset(12)
        Assert.assertEquals(intervalTestOut.group, intervalTestInput[1]!!.group)
        intervalTestOut = mRepo!!.getGroupByOffset(13)
        Assert.assertEquals(intervalTestOut.group, intervalTestInput[2]!!.group)
    }

    @Test
    fun getAllTest(){
        //mRepo!!.setExecutorToSync()
        val thread = Thread.currentThread()
        var observerAssertFunction = { it: Array<IntervalData>? ->

        }
        val observer = Observer<Array<IntervalData>> {
            observerAssertFunction(it)
            synchronized(thread) { (thread as java.lang.Object).notify() }
        }

        val intervals = IntervalData.generate(10)
        mRepo!!.insert(intervals)
        val children = IntervalData.generate(10, intervals[0])
        mRepo!!.insert(children)
        val retrievedInterval = mRepo!!.getAllOfGroup(intervals[0]!!.group)

        observerAssertFunction = { it: Array<IntervalData>? ->
            if(it != null && it.isNotEmpty()) {
                Assert.assertNotNull(intervals)
                Assert.assertTrue("intervals is empty", intervals.isNotEmpty())
                Assert.assertTrue(intervals[0] == it[0])
                val childIntervalsIterator = it.iterator().withIndex()
                childIntervalsIterator.next() //skip the first
                childIntervalsIterator.forEachRemaining {
                    Assert.assertEquals(it.value, children[it.index - 1])
                }
            }

        }

        retrievedInterval.observe(this.activity, observer)
        // wait for the observation to fire by blocking this thread and having the observer notify it
        // I KNOW IT'S WEIRD BUT TRUST ME IT WORKS
        synchronized(thread) {(thread as java.lang.Object).wait()}

        val moreChildren = IntervalData.generate(10, intervals[0], startGroupPosition = (children.last()!!.groupPosition+1).toInt())
        val evenMoreChildren = IntervalData.generate(10, intervals[1], startGroupPosition = (moreChildren.last()!!.groupPosition+1).toInt())
        observerAssertFunction = {
            if(it != null) {
                Assert.assertTrue(intervals[0] == it[0])
                val childIntervalsIterator = it.iterator().asSequence().drop(1+children.size).iterator().withIndex()
                childIntervalsIterator.forEachRemaining {value: IndexedValue<IntervalData> ->
                    Assert.assertEquals(value.value, moreChildren[value.index])
                }
            }
        }
        mRepo!!.insert(moreChildren)
        mRepo!!.insert(evenMoreChildren)
        synchronized(thread) {(thread as java.lang.Object).wait()}
        Handler(this.activity.mainLooper).post {retrievedInterval.removeObservers(this.activity)}


    }

    @Test
    fun insertTest(){
        mRepo!!.setExecutorToSync()
        val thread = Thread.currentThread()
        val interval = IntervalData.generate(1)[0]!!
        mRepo!!.insert(interval)
        val retrievedInterval = mRepo!!.get(interval.id)
        val observer = Observer<IntervalData> {
            if(it != null) {
                Assert.assertTrue(interval == it)
                synchronized(thread, { (thread as java.lang.Object).notify() })
            }
        }
        retrievedInterval.observe(this.activity, observer)
        synchronized(thread, {(thread as java.lang.Object).wait()})
    }

    private class RunOnThisExecutor(private val mHandler: IntervalRepositoryTest): Executor{
        override fun execute(p0: Runnable?) {
            p0?.run()
        }
    }

    @Test
    fun updateTest(){
        mRepo!!.executor = RunOnThisExecutor(this)

        var observationsFired = 0
        val interval = IntervalData.generate(1)[0]!!
        val thread = Thread.currentThread()
        mRepo!!.insert(interval)
        val retrievedInterval = mRepo!!.get(interval.id)
        val observer = Observer<IntervalData> {
            if(it != null) {
                Assert.assertTrue(interval == it)
                observationsFired++
            }
            synchronized(thread, {(thread as java.lang.Object).notify()})
        }
        retrievedInterval.observe(this.activity, observer)
        synchronized(thread, {(thread as java.lang.Object).wait()})


        interval.label = UUID.randomUUID().toString()
        mRepo!!.update(interval)
        synchronized(thread, {(thread as java.lang.Object).wait()})

        Assert.assertEquals(2, observationsFired)
//        retrievedInterval.removeObservers(this.activity)

    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        mRepo?.deleteAll()
        mRepo?.release()

        //
    }


}