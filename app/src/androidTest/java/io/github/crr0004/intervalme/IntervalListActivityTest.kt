package io.github.crr0004.intervalme

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.runner.RunWith
import android.view.View
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import android.widget.AdapterView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra
import android.support.test.filters.SmallTest
import org.hamcrest.Matchers.*


@RunWith(AndroidJUnit4::class)
public class IntervalListActivityTest : ActivityTestRule<IntervalListActivity>(IntervalListActivity::class.java) {
    private var mIntervalDao: IntervalDataDOA? = null
    private var mDb: IntervalMeDatabase? = null
    private val mTestIntervalSize = 10
    private val mIntervalParent = IntervalData.generate(1)[0]
    private val mTestIntervals = IntervalData.generate(mTestIntervalSize, mIntervalParent)
    private lateinit var mIds: List<Long>

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupDB(){
            IntervalListAdapter.IN_MEMORY_DB = true
            //IntervalMeDatabase.getTemporaryInstance()?.intervalDataDao()?.insert(IntervalData.generate(5))
        }
    }

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        val context = InstrumentationRegistry.getTargetContext()
        mDb = IntervalMeDatabase.getTemporaryInstance(context)
        mIntervalDao = mDb!!.intervalDataDao()
        mIntervalDao?.insert(mIntervalParent!!)
        mIds = mIntervalDao?.insert(mTestIntervals)!!
    }

    override fun afterActivityFinished() {
        super.afterActivityFinished()
        IntervalMeDatabase.destroyInstance()
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

        onData(allOf((instanceOf(IntervalData::class.java)), withChildName(mTestIntervals[mTestIntervalSize-1]!!)))
                .inAdapterView(withId(R.id.intervalsExpList))
                .check(matches(isDisplayed()))

        onData(allOf((
                instanceOf(IntervalData::class.java)), withChildName(mTestIntervals[0]!!)))
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
        onData(allOf((
                instanceOf(IntervalData::class.java)), withChildName(mTestIntervals[0]!!)))
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

    fun withChildName(intervalData: IntervalData): Matcher<Any> {
        checkNotNull(intervalData)
        return withChildName(equalTo(intervalData))
    }

    fun withChildName(intervalData: Matcher<IntervalData>): Matcher<Any> {
        checkNotNull(intervalData)
        // ChildStruct is the Class returned by BaseExpandableListAdapter.getChild()
        return object : BoundedMatcher<Any, IntervalData>(IntervalData::class.java) {

            public override fun matchesSafely(data: IntervalData): Boolean {
                return intervalData.matches(data)
            }

            override fun describeTo(description: Description) {
                intervalData.describeTo(description)
            }
        }
    }

    private fun withAdaptedData(dataMatcher: Matcher<Any>): Matcher<View> {
        return object : TypeSafeMatcher<View>() {

            @Override
            override fun describeTo(description: Description) {
                description.appendText("with class name: ")
                dataMatcher.describeTo(description)
            }

            @Override
            override fun matchesSafely(view: View): Boolean {
                if (view !is AdapterView<*>) {
                    return false
                }

                val adapter = (view as AdapterView<*>).adapter
                for (i in 0 until adapter.count) {
                    if (dataMatcher.matches(adapter.getItem(i))) {
                        return true
                    }
                }

                return false
            }
        }
    }
}