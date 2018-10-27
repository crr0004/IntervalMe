package io.github.crr0004.intervalme

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineDao
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import io.github.crr0004.intervalme.database.routine.RoutineTableData
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class RoutineDaoTest {

    private lateinit var mDao: RoutineDao

    @Before
    fun setup(){
        IntervalMeDatabase.USING_TEMP_DATABASE = true
        mDao = IntervalMeDatabase.getInstance(InstrumentationRegistry.getContext())!!.routineDao()
    }

    @After
    fun tearDown(){
        IntervalMeDatabase.destroyInstance()
    }

    @Test
    fun insert(){
        val exercises = arrayListOf(
                ExerciseData(description = "Squat",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""))
        val routineSetData = RoutineSetData(routineId = 1, description = "")
        routineSetData.exercises.addAll(exercises)
        val routineId = mDao.insert(RoutineTableData(0, routineSetData.description))
        Assert.assertEquals(1, routineId)
        val ids = mDao.insert(routineSetData.exercises)
        ids.forEach {
            Assert.assertTrue("Exercises didn't return a proper ID", it > 0)
        }

    }

    @Test
    fun getAllRoutineAndExerciseCountTest(){
        val exercises = arrayListOf(
                ExerciseData(description = "Squat",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""))
        val routineSetData = RoutineSetData(routineId = 1, description = "")
        routineSetData.exercises.addAll(exercises)
        mDao.insert(RoutineTableData(0, routineSetData.description))
        mDao.insert(RoutineTableData(0, routineSetData.description))
        mDao.insert(RoutineTableData(0, routineSetData.description))
        mDao.insert(routineSetData.exercises)
        val count = mDao.getSyncAllRoutineAndExerciseCount()
        Assert.assertEquals(8, count)
    }

    @Test
    fun getRoutineSetData(){
        val exercises = arrayListOf(
                ExerciseData(description = "Squat",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""),
                ExerciseData(description = "Dead lift",
                        lastModified = Date(),
                        routineId = 1,
                        value0 = "",
                        value1 = "",
                        value2 = ""))
        val routineSetData = RoutineSetData(routineId = 1, description = "")
        routineSetData.exercises.addAll(exercises)
        mDao.insert(RoutineTableData(0, routineSetData.description))
        mDao.insert(routineSetData.exercises)
        //val routine = mDao.getSyncRoutineSetById(1)

    }
}