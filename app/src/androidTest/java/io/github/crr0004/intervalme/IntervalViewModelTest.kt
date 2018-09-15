package io.github.crr0004.intervalme

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.IntervalRepository
import io.github.crr0004.intervalme.interval.IntervalListActivity
import io.github.crr0004.intervalme.interval.IntervalViewModel
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class IntervalViewModelTest: ActivityTestRule<IntervalListActivity>(IntervalListActivity::class.java) {
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
    fun getGroups(){
        val thread = Thread.currentThread()
        val intervalInput = IntervalData.generate(10000)
        mRepo!!.insert(intervalInput)
        mRepo!!.insert(IntervalData.generate(3, intervalInput[0]))
        mRepo!!.insert(IntervalData.generate(3, intervalInput[3]))
        mRepo!!.insert(IntervalData.generate(3, intervalInput[5]))
        val groups = ViewModelProviders.of(mActivityRule.activity).get(IntervalViewModel::class.java).getGroups()
        // Get livedata from our source (a db) initially returns a null value as it doesn't block
        Assert.assertNull(groups.value)
        groups.observe(this.activity, Observer {
            Assert.assertNotNull(it)
            Assert.assertEquals(it!!.size, intervalInput.size)
            it.forEachIndexed { index, intervalData ->
                Assert.assertEquals(intervalInput[index], intervalData)
            }
            groups.removeObservers(mActivityRule.activity)
            synchronized(thread, { (thread as java.lang.Object).notify() })
        })
        synchronized(thread, { (thread as java.lang.Object).wait() })

    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        mRepo?.deleteAll()
        mRepo?.release()

        //
    }
}