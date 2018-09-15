package io.github.crr0004.intervalme

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.intent.Intents
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsDao
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsData
import io.github.crr0004.intervalme.interval.IntervalListActivity
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class IntervalAnalyticsRepoTest : ActivityTestRule<IntervalListActivity>(IntervalListActivity::class.java) {

    //private lateinit var mRepo: IntervalAnalyticsRepository
    private lateinit var mdb: IntervalMeDatabase
    private lateinit var mIntervalAnalyticsDao: IntervalAnalyticsDao
    @get:Rule
    public var mActivityRule: ActivityTestRule<IntervalListActivity> = this

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupDB(){
            IntervalMeDatabase.USING_TEMP_DATABASE = true
            //IntervalMeDatabase.getTemporaryInstance()?.intervalDataDao()?.insert(IntervalData.generate(5))
        }
    }

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()


        val context = InstrumentationRegistry.getTargetContext()
        //mRepo = ViewModelProviders.of(this.activity).get(IntervalViewModel::class.java).mAnalyticsRepository
    }

    @Before
    fun setup() {
        Intents.init()
        mdb = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), IntervalMeDatabase::class.java).build()
        mIntervalAnalyticsDao = mdb.intervalAnalyticsDao()

    }

    @After
    fun testDone(){
        Intents.release()

    }

    @Test
    fun intervalAnalyticsClonesAndInserts(){
        val interval = IntervalData(1, "test", duration = 100L, groupPosition = 1)
        val id = mIntervalAnalyticsDao.insert(IntervalAnalyticsData(interval))
        Assert.assertTrue("Insert into interval analytics should return an id greater than 0", id > 0)
        val analyticsData = mIntervalAnalyticsDao.syncGet(id)
        Assert.assertEquals(id, analyticsData.id)
        Assert.assertEquals(interval.duration, analyticsData.duration)
        Assert.assertEquals(interval.group, analyticsData.group)
        Assert.assertEquals(interval.groupPosition, analyticsData.groupPosition)
        Assert.assertEquals(interval.label, analyticsData.label)
        Assert.assertEquals(interval.ownerOfGroup, analyticsData.ownerOfGroup)
    }

    @Test
    fun intervalAnalyticsWithRunPropertiesClonesAndInserts(){
        val interval = IntervalData(1, "test", duration = 100L, groupPosition = 1)
        val properties = IntervalRunProperties(intervalId = interval.id, loops = 10)
        val id = mIntervalAnalyticsDao.insert(IntervalAnalyticsData(interval, properties))
        Assert.assertTrue("Insert into interval analytics should return an id greater than 0", id > 0)
        val analyticsData = mIntervalAnalyticsDao.syncGet(id)
        Assert.assertEquals(id, analyticsData.id)
        Assert.assertEquals(properties.loops, analyticsData.loops)
    }

}