package io.github.crr0004.intervalme.regressions

import android.app.PendingIntent
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.contrib.ViewPagerActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.v4.app.TaskStackBuilder
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.RoutineListActivityTest
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import io.github.crr0004.intervalme.routine.*
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class RoutineTemplateCopyingDoneStatus: ActivityTestRule<RoutineManageActivity>(
        RoutineManageActivity::class.java, true, false) {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupDB() {
            IntervalMeDatabase.USING_TEMP_DATABASE = true
            //IntervalMeDatabase.getTemporaryInstance()?.intervalDataDao()?.insert(IntervalData.generate(5))
        }
    }

    @get:Rule
    var mActivityRule: ActivityTestRule<RoutineManageActivity> = this
    @Mock
    private lateinit var mMockRepo: RoutineRepo

    @Before
    fun setup() {
        RoutineViewModel.repoOverride = mMockRepo


    }



    @Test
    fun copyingTemplateCopiesDoneStatus(){
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
                        value2 = "502kg",
                        isDone = true),
                ExerciseData(description = "Dead lift2",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "12",
                        value1 = "62",
                        value2 = "1002kg",
                        isDone = true)
        ), isTemplate = true, isDone = true)
        val list = ArrayList<RoutineSetData>()
        list.add(routine2)
        val liveData = RoutineListActivityTest.TestLiveData<ArrayList<RoutineSetData>>()
        liveData.postValue(list)
        Mockito.`when`(mMockRepo.getAllRoutines(anyString(), any())).thenReturn(liveData)
        val liveDataCount = RoutineListActivityTest.TestLiveData<Int>()
        liveDataCount.postValue(6)
        Mockito.`when`(mMockRepo.getAllRoutineAndExerciseCount()).thenReturn(liveDataCount)
        //this.activityIntent.
        this.launchActivity(null)

        Espresso.onView(ViewMatchers.withId(R.id.container)).perform(ViewPagerActions.scrollToPage(1))
        onView(withId(R.id.routineManageTemplateRecycler)).perform(
                RecyclerViewActions.scrollToPosition<RoutineTemplateHolder>(0)
        )
        onView(allOf(withId(R.id.routineListGroupCopyBtn))).check(matches(isDisplayed())).perform(click())
        onView(allOf(withId(R.id.routineEditCommitBtn))).check(matches(isDisplayed())).perform(click())

        val routineBeingUpdated = ArgumentCaptor.forClass(MutableLiveData<RoutineSetData>().javaClass)
        Mockito.verify(mMockRepo).insert(routineBeingUpdated.capture())
        assertThat(routineBeingUpdated.value.value, notNullValue())
        assertThat(routineBeingUpdated.value.value!!.isDone, `is`(eq(false)))
        routineBeingUpdated.value.value!!.exercises.forEach {
            assertThat(it.isDone, `is`(eq(false)))
        }

    }
}