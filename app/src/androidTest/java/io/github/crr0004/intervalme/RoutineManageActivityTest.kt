package io.github.crr0004.intervalme

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.util.Log
import com.nhaarman.mockitokotlin2.isA
import io.github.crr0004.intervalme.CustomViewActionsMatchers.Companion.editRoutineItemViewHolderDescription
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import io.github.crr0004.intervalme.routine.RoutineManageActivity
import io.github.crr0004.intervalme.routine.RoutineManageBasicFragment
import io.github.crr0004.intervalme.routine.RoutineViewModel
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RoutineManageActivityTest : ActivityTestRule<RoutineManageActivity>(RoutineManageActivity::class.java) {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setupDB(){
            IntervalMeDatabase.USING_TEMP_DATABASE = true
            //IntervalMeDatabase.getTemporaryInstance()?.intervalDataDao()?.insert(IntervalData.generate(5))
        }
    }
    @get:Rule
    var mActivityRule: ActivityTestRule<RoutineManageActivity> = this
    @Mock
    private lateinit var mMockRepo: RoutineRepo

    @Before
    fun setup(){
        val viewModel = ViewModelProviders.of(this.activity).get(RoutineViewModel::class.java)
        viewModel.setRepo(mMockRepo)
    }

    @Test
    fun test(){
        `when`(mMockRepo.insert(isA<LiveData<RoutineSetData>>())).thenAnswer { it: InvocationOnMock ->
            Log.d("","")

        }
        onView(withId(R.id.routineEditCommitBtn))
                .check(matches(isDisplayed()))
                .perform(click())

    }

    @Test
    fun addItemButtonAddsExercise(){
        onView(withId(R.id.routineEditAddExerciseBtn))
                .check(matches(isDisplayed()))
                .perform(click())
        val exerciseData = ExerciseData(description = "squat", value0 = "1", value1 = "5", value2 = "50kg")
        onView(ViewMatchers.withId(R.id.routineManageBasicRecycler))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RoutineManageBasicFragment.RoutineManageBasicItemViewHolder>(0, editRoutineItemViewHolderDescription(exerciseData))
                )
        onView(withId(R.id.routineManageBasicDescriptionTxt))
                .check(matches(isDisplayed()))
                .perform(replaceText("Routine1"))
        `when`(mMockRepo.insert(isA<LiveData<RoutineSetData>>())).thenAnswer { it: InvocationOnMock ->
            val routine = it.getArgument<LiveData<RoutineSetData>>(0).value!!
            Assert.assertEquals("Routine1", routine.description)
            Assert.assertEquals(1, routine.exercises.size)
            // We do this because we can't exactly set the internal object to be the same in the action above
            exerciseData.lastModified = routine.exercises[0].lastModified
            Assert.assertEquals(exerciseData, routine.exercises[0])
        }
        onView(withId(R.id.routineEditCommitBtn))
                .check(matches(isDisplayed()))
                .perform(click())
    }
}