package io.github.crr0004.intervalme

import android.arch.lifecycle.MutableLiveData
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.routine.*
import org.junit.Assert
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.Executor

@RunWith(AndroidJUnit4::class)
class RoutineRepoTest{

    private lateinit var mRepo: RoutineRepo

    @Before
    fun setup(){
        IntervalMeDatabase.USING_TEMP_DATABASE = true
        mRepo = RoutineRepo(InstrumentationRegistry.getContext())
        mRepo.executor = SynchronousExecutor()
    }

    @After
    fun tearDown(){
        IntervalMeDatabase.destroyInstance()
    }

    @Test
    fun getRoutineSetData(){
        val exercises = arrayListOf(
                ExerciseData(description = "Squat",
                        id = 1,
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        id = 2,
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""))
        val routineSetData = RoutineSetData(routineId = 1, description = "")
        routineSetData.exercises.addAll(exercises)
        Log.d("Test_RP", "Inserting routineSetData")
        mRepo.insert(routineSetData)
        Log.d("Test_RP", "Inserted routineSetData")
        val data = TestLiveData<RoutineSetData>()
        mRepo.getRoutineSetById(1, data)
        Log.d("Test_RP", "Fetched routineSetData")
        Assert.assertNotNull(data.value)
        Assert.assertEquals(1, data.value!!.routineId)
        exercises.forEachIndexed { index, exerciseData ->
            Assert.assertEquals(exerciseData, data.value!!.exercises[index])
        }
    }

    internal inner class SynchronousExecutor : Executor {
        override fun execute(r: Runnable) {
            Log.d("Test_RP", "Execute called $r")
            r.run()
        }
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