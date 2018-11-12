package io.github.crr0004.intervalme

import android.arch.lifecycle.MutableLiveData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import io.github.crr0004.intervalme.routine.RoutineListActivity
import io.github.crr0004.intervalme.routine.RoutineRecyclerAdapter
import io.github.crr0004.intervalme.routine.RoutineViewModel
import org.hamcrest.Matchers.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class RoutineListActivityTest : ActivityTestRule<RoutineListActivity>(RoutineListActivity::class.java, true, false) {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupDB() {
            IntervalMeDatabase.USING_TEMP_DATABASE = true
            //IntervalMeDatabase.getTemporaryInstance()?.intervalDataDao()?.insert(IntervalData.generate(5))
        }
    }

    @get:Rule
    var mActivityRule: ActivityTestRule<RoutineListActivity> = this
    @Mock
    private lateinit var mMockRepo: RoutineRepo

    @Before
    fun setup() {
        RoutineViewModel.repoOverride = mMockRepo


    }

    @After()
    fun done(){

    }

    private fun getAllStringValuesFromRoutine(routineSetData: RoutineSetData) : ArrayList<String>{
        val values = ArrayList<String>(1+4*routineSetData.exercises.size)
        values.add(routineSetData.description)
        routineSetData.exercises.forEach{ it ->
            values.add(it.description)
            values.add(it.value0)
            values.add(it.value1)
            values.add(it.value2)
        }
        return values
    }

    @Test
    fun showsRoutines(){
        val routine = RoutineSetData(1, "Routine1", arrayListOf(
                ExerciseData(description = "Squat",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "4",
                        value1 = "5",
                        value2 = "50kg"),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "1",
                        value1 = "6",
                        value2 = "100kg")
        ), isTemplate = false)
        val routine2 = RoutineSetData(1, "Routine2", arrayListOf(
                ExerciseData(description = "Squat2",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "42",
                        value1 = "52",
                        value2 = "502kg"),
                ExerciseData(description = "Dead lift2",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "12",
                        value1 = "62",
                        value2 = "1002kg")
        ), isTemplate = false)
        val list = ArrayList<RoutineSetData>()
        list.add(routine)
        list.add(routine2)
        val liveData = TestLiveData<ArrayList<RoutineSetData>>()
        liveData.postValue(list)
        `when`(mMockRepo.getAllRoutines()).thenReturn(liveData)
        val liveDataCount = TestLiveData<Int>()
        liveDataCount.postValue(6)
        `when`(mMockRepo.getAllRoutineAndExerciseCount()).thenReturn(liveDataCount)
        this.launchActivity(null)
        getAllStringValuesFromRoutine(routine).forEach {
            onView(withText(it)).check(matches(isDisplayed()))
        }
        onView(withId(R.id.routineRecyclerView)).perform(
                RecyclerViewActions.scrollToPosition<RoutineRecyclerAdapter.RoutineSetViewHolder>(1)
        )
        getAllStringValuesFromRoutine(routine2).forEach {
            onView(withText(it)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun deleteButtonRemovesRoutine(){
        Intents.init()
        val routine = RoutineSetData(1, "Routine1", arrayListOf(
                ExerciseData(description = "Squat",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "4",
                        value1 = "5",
                        value2 = "50kg"),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "1",
                        value1 = "6",
                        value2 = "100kg")
        ), isTemplate = false)
        val list = ArrayList<RoutineSetData>()
        list.add(routine)
        val liveData = TestLiveData<ArrayList<RoutineSetData>>()
        liveData.postValue(list)
        `when`(mMockRepo.getAllRoutines()).thenReturn(liveData)
        val liveDataCount = TestLiveData<Int>()
        liveDataCount.postValue(3)
        `when`(mMockRepo.getAllRoutineAndExerciseCount()).thenReturn(liveDataCount)
        `when`(mMockRepo.deleteRoutineById(routine.routineId)).then {
            list.removeAt(0)
            liveData.postValue(list)
            liveDataCount.postValue(0)
        }
        this.launchActivity(null)

        onView(allOf(withId(R.id.routineListGroupDeleteBtn), hasSibling(withText(routine.description))))
                .check(matches(isDisplayed()))
                .perform(click())
        onView(withText(routine.description)).check(doesNotExist())
    }

    internal class TestLiveData<T> : MutableLiveData<T>(){
        @Volatile
        private var mData: T? = null

        override fun postValue(value: T) {
            mData = value
            super.postValue(value)
        }

        override fun setValue(value: T) {
            super.setValue(value)
            mData = value
        }

        override fun getValue(): T? {
            return mData
        }
    }
}