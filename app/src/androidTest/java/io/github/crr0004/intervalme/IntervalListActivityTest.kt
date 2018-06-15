package io.github.crr0004.intervalme

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.Intents.intending
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import io.github.crr0004.intervalme.CustomViewActionsMatchers.Companion.itemFollows
import io.github.crr0004.intervalme.CustomViewActionsMatchers.Companion.withIntervalData
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.views.IntervalViewModel
import junit.framework.Assert.assertEquals
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
public class IntervalListActivityTest : ActivityTestRule<IntervalListActivity>(IntervalListActivity::class.java) {
    private var mIntervalDao: IntervalDataDOA? = null
    private var mDb: IntervalMeDatabase? = null
    private val mTestIntervalSize = 11
    private val mIntervalParent = IntervalData.generate(1)[0]
    private val mSecondIntervalParent = IntervalData.generate(1)[0]
    private val mTestIntervals = IntervalData.generate(mTestIntervalSize, mIntervalParent)
    private lateinit var mIds: List<Long>

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupDB(){
            IntervalMeDatabase.USING_TEMP_DATABSE = true
            //IntervalMeDatabase.getTemporaryInstance()?.intervalDataDao()?.insert(IntervalData.generate(5))
        }
    }

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()


        val context = InstrumentationRegistry.getTargetContext()
        mDb = IntervalMeDatabase.getInstance(context)
        mIntervalDao = mDb!!.intervalDataDao()
        mIntervalParent!!.groupPosition = 0
        mSecondIntervalParent!!.groupPosition = 1
        mIntervalDao?.insert(mIntervalParent)
        mIntervalDao?.insert(mSecondIntervalParent)
        mIds = mIntervalDao?.insert(mTestIntervals)!!
    }

    override fun afterActivityFinished() {
        super.afterActivityFinished()

        //IntervalMeDatabase.destroyInstance()
    }

    @Before
    fun setup() {
        Intents.init()

    }

    @After
    fun testDone(){
        Intents.release()

    }

    @get:Rule
    public var mActivityRule: ActivityTestRule<IntervalListActivity> = this

    @Test
    public fun addButtonGoesToAddActivity(){
        onView(withId(R.id.action_goto_add)).perform(click())
        intended(hasComponent(IntervalAddActivity::class.java.name))
    }

    @Test
    @SmallTest
    fun parentAndChildDisplayed(){
        onData(allOf(instanceOf(IntervalData::class.java), equalTo(mIntervalParent)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .perform(click()) // We have to click here to expand the list
                .check(matches(isDisplayed()))

        onData(allOf((instanceOf(IntervalData::class.java)), withIntervalData(mTestIntervals[mTestIntervalSize-1]!!)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))

        onData(allOf((
                instanceOf(IntervalData::class.java)), withIntervalData(mTestIntervals[0]!!)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))
                //.check(matches(equalTo(mTestIntervals[0]!!)))
    }

    @Test
    fun editButtonGoesToEditActivity(){
        onData(allOf(instanceOf(IntervalData::class.java), equalTo(mIntervalParent)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))
                .perform(click())
        onView(withId(R.id.action_edit_items)).check(matches(isDisplayed())).perform(click())
        onData(allOf((
                instanceOf(IntervalData::class.java)), withIntervalData(mTestIntervals[0]!!)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .onChildView(withId(R.id.clockSingleEditButton))
                .check(matches(isDisplayed()))
                .perform(click())
        intended(allOf(
                hasComponent(IntervalAddActivity::class.java.name),
                hasExtra(IntervalAddActivity.EDIT_MODE_FLAG_INTERVAL_ID, mIds[0]),
                hasExtra(IntervalAddActivity.EDIT_MODE_FLAG_ID, true)
        ))

    }

    @Test
    fun swapItems(){
        // Check first group and expand it

        val thread = Thread.currentThread()
        val model = ViewModelProviders.of(this.activity).get(IntervalViewModel::class.java)
        // We do this so we can wait until all the data is done loading
        model.getGroups().observe(this.activity, Observer {
            if(it != null && it.isNotEmpty()) {
                model.getAllOfGroup(it[it.size - 1].group).observe(this.activity, Observer {
                    if(it != null)
                        synchronized(thread, {(thread as java.lang.Object).notify()})
                })
            }
        })
        synchronized(thread, {(thread as java.lang.Object).wait()})
        onData(allOf(instanceOf(IntervalData::class.java), equalTo(mIntervalParent)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))
                .perform(click())

        var interval1 = mIntervalDao!!.get(mIds[0])
        var interval2 = mIntervalDao!!.get(mIds[1])

        onView(withId(R.id.intervalsExpList)).perform(CustomViewActionsMatchers.swapIntervalListAdapterItems(interval1, interval2))

        interval1 = mIntervalDao!!.get(mIds[0])
        interval2 = mIntervalDao!!.get(mIds[1])

        assertEquals(interval1.groupPosition, 1)
        assertEquals(interval2.groupPosition, 0)

        onView(withId(R.id.intervalsExpList))
                .check(itemFollows(0, interval2, interval1))
    }

    @Test
    fun editItemsToNewGroup(){
        // Check the two parent groups
        onData(allOf(instanceOf(IntervalData::class.java), equalTo(mIntervalParent)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))
                .perform(click())
        onData(allOf(instanceOf(IntervalData::class.java), equalTo(mSecondIntervalParent)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))
                .perform(click())



        // Click edit on one the child intervals
        var intervalToEdit = mIntervalDao!!.get(mIds[mTestIntervalSize/2])
        onView(withId(R.id.action_edit_items)).check(matches(isDisplayed())).perform(click())
        onData(allOf((
                instanceOf(IntervalData::class.java)), withIntervalData(intervalToEdit)))
                .onChildView(withId(R.id.clockSingleEditButton))
                .check(matches(isDisplayed()))
                .perform(click())
        onView(withId(R.id.intervalParentTxt)).check(matches(withText(intervalToEdit.group.toString())))
        onView(withId(R.id.intervalNameTxt)).check(matches(withText(intervalToEdit.label.toString())))

        // Update the group value and come back to ListActivity
        onView(withId(R.id.clockSampleRecycleList))
                .perform(actionOnItemAtPosition<IntervalSimpleGroupAdapter.SimpleGroupViewHolder>(mSecondIntervalParent!!.groupPosition.toInt(), click()))
        onView(withId(R.id.intervalAddBtn)).perform(click())
        onView(withId(R.id.goToListBtn)).perform(click())

        // Check intent has come back correctly
        intending(allOf(
                hasExtra(IntervalAddActivity.EDIT_MODE_FLAG_INTERVAL_ID, intervalToEdit.id)
        ))

        // close the first group
        onData(allOf(instanceOf(IntervalData::class.java), equalTo(mIntervalParent)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))
                .perform(click())

        intervalToEdit = mIntervalDao!!.get(intervalToEdit.id)
        onView(withId(R.id.intervalsExpList)).perform(CustomViewActionsMatchers.invalidateAdapter())
        Assert.assertEquals(intervalToEdit.group, mSecondIntervalParent.group)

        // Check the edited interval is still on screen
        onData(allOf((
                instanceOf(IntervalData::class.java)), withIntervalData(intervalToEdit)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))
    }


}