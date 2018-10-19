package io.github.crr0004.intervalme

import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.replaceText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import io.github.crr0004.intervalme.routine.RoutineManageActivity
import io.github.crr0004.intervalme.routine.RoutineViewModel
import org.hamcrest.Matchers.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class RoutineManageEditActivityTest : ActivityTestRule<RoutineManageActivity>(RoutineManageActivity::class.java,true, false) {
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
        //mDao = IntervalMeDatabase.
        RoutineViewModel.repoOverride = mMockRepo
    }

    @Test
    fun launchesInEditMode(){
        val intent = Intent()
        intent.putExtra(RoutineManageActivity.routine_edit_id_key, 1L)

       `when`(mMockRepo.getRoutineSetById(anyLong(), eq(null))).thenReturn(MutableLiveData())

        this.launchActivity(intent)
        verify(mMockRepo).getRoutineSetById(1)
    }

    @Test
    fun adapterShowsData(){
        val intent = Intent()
        intent.putExtra(RoutineManageActivity.routine_edit_id_key, 1L)

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
        ))
        val liveData = MutableLiveData<RoutineSetData>()
        liveData.postValue(routine)
        `when`(mMockRepo.getRoutineSetById(eq(1L), eq(null))).thenReturn(liveData)

        this.launchActivity(intent)

        onView(withText("Squat")).check(matches(isDisplayed()))
        onView(withText("4")).check(matches(isDisplayed()))
        onView(withText("5")).check(matches(isDisplayed()))
        onView(withText("50kg")).check(matches(isDisplayed()))

        onView(withText("Dead lift")).check(matches(isDisplayed()))
        onView(withText("1")).check(matches(isDisplayed()))
        onView(withText("6")).check(matches(isDisplayed()))
        onView(withText("100kg")).check(matches(isDisplayed()))

        onView(withText("Routine1")).check(matches(isDisplayed()))



    }

    @Test
    fun editModelCommitsProperly(){
        val intent = Intent()
        intent.putExtra(RoutineManageActivity.routine_edit_id_key, 1L)

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
        ))
        val liveData = MutableLiveData<RoutineSetData>()
        liveData.postValue(routine)
        `when`(mMockRepo.getRoutineSetById(eq(1L), eq(null))).thenReturn(liveData)


        this.launchActivity(intent)
        onView(withText("Routine1"))
                .perform(replaceText("Routine2"))
        onView(withText("4"))
                .perform(replaceText("10"))
        onView(withId(R.id.routineEditCommitBtn)).perform(click())
        Assert.assertEquals("10", routine.exercises[0].value0)
        Assert.assertEquals("Routine2", routine.description)
        verify(mMockRepo).update(liveData)
    }

    internal class TestLiveData<T> : MutableLiveData<T>(){
        @Volatile
        private var mData: T? = null
        override fun setValue(value: T) {
            mData = value

        }

        override fun getValue(): T? {
            return mData
        }
    }

}