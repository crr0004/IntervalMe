package io.github.crr0004.intervalme

import android.arch.lifecycle.MutableLiveData
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import org.junit.After
import org.junit.Assert
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
        mRepo.insert(routineSetData)
        val data = TestLiveData<RoutineSetData>()
        mRepo.getRoutineSetById(1, data)
        Assert.assertNotNull(data.value)
        Assert.assertEquals(1, data.value!!.routineId)
        exercises.forEachIndexed { index, exerciseData ->
            Assert.assertEquals(exerciseData, data.value!!.exercises[index])
        }
    }

    @Test
    fun update(){
        val exercises = arrayListOf(
                ExerciseData(description = "Squat",
                        id = 1,
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "1",
                        value1 = "2",
                        value2 = "3"),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        id = 2,
                        routineId = 1,
                        value0 = "4",
                        value1 = "5",
                        value2 = "6"))
        val routineSetData = RoutineSetData(routineId = 1, description = "Routine1")
        routineSetData.exercises.addAll(exercises)
        mRepo.insert(routineSetData)
        val data = TestLiveData<RoutineSetData>()
        mRepo.getRoutineSetById(1, data)
        Assert.assertNotNull(data.value)
        data.value!!.description = "Routine2"
        data.value!!.exercises[0].value0 = "7"
        mRepo.update(data)

        val data2 = TestLiveData<RoutineSetData>()
        mRepo.getRoutineSetById(1, data2)
        Assert.assertEquals(1, data2.value!!.routineId)
        Assert.assertEquals("Routine2", data2.value!!.description)
        Assert.assertEquals("7", data2.value!!.exercises[0].value0)
    }

    @Test
    fun delete(){
        val exercises = arrayListOf(
                ExerciseData(description = "Squat",
                        id = 1,
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "1",
                        value1 = "2",
                        value2 = "3"),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        id = 2,
                        routineId = 1,
                        value0 = "4",
                        value1 = "5",
                        value2 = "6"))
        val routineSetData = RoutineSetData(routineId = 1, description = "Routine1")
        routineSetData.exercises.addAll(exercises)
        mRepo.insert(routineSetData)
        val data = TestLiveData<RoutineSetData>()
        mRepo.getRoutineSetById(1, data)
        Assert.assertNotNull(data.value)
        mRepo.deleteExercise(exercises[0].id)
        val data2 = TestLiveData<RoutineSetData>()
        mRepo.getRoutineSetById(1, data2)
        Assert.assertNotEquals(data.value!!.exercises[0], data2.value!!.exercises[0])
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

        override fun postValue(value: T) {
            super.postValue(value)
            mData = value
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