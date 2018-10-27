package io.github.crr0004.intervalme.database.routine

import android.annotation.SuppressLint
import android.arch.lifecycle.ComputableLiveData
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.InvalidationTracker
import android.arch.persistence.room.RoomSQLiteQuery
import android.content.Context
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import java.util.*
import java.util.concurrent.Executor

open class RoutineRepo(mContext: Context) {
    private var mDb = IntervalMeDatabase.getInstance(mContext)!!
    private var mDao: RoutineDao = IntervalMeDatabase.getInstance(mContext)!!.routineDao()
    private var mExecutor: Executor = ThreadPerTaskExecutor()
    var executor: Executor
        set(value) {mExecutor = value}
        get() {return mExecutor}

    open fun insert(routineToEdit: LiveData<RoutineSetData>?) {
        if(routineToEdit != null && routineToEdit.value != null){
            insert(routineToEdit.value!!)
        }
    }

    open fun insert(routineToEdit: RoutineSetData){
        mExecutor.execute {
            val id = mDao.insert(RoutineTableData(0, routineToEdit.description))
            routineToEdit.exercises.forEach {
                it.routineId = id
            }
            mDao.insert(routineToEdit.exercises)
        }
    }

    open fun getRoutineSetById(routineId: Long, routineSetData: LiveData<RoutineSetData>? = null) : MutableLiveData<RoutineSetData>{
        val data: MutableLiveData<RoutineSetData> = if(routineSetData == null)
            MutableLiveData()
        else
            routineSetData as MutableLiveData<RoutineSetData>

        mExecutor.execute {
            val routine = mDao.getSyncRoutineTableById(routineId)
            val exerciseData = mDao.getSyncExercisesWithRoutineId(routineId)
            val setData = RoutineSetData(routineId, routine.description)
            setData.exercises.addAll(exerciseData)
            data.postValue(setData)
        }
        return data
    }

    open fun update(routineToEdit: LiveData<RoutineSetData>){
        if(routineToEdit.value != null)
            update(routineToEdit.value!!)
    }

    open fun update(routineToEdit: RoutineSetData) {
        mExecutor.execute {
            mDao.update(RoutineTableData(routineToEdit))
            mDao.update(routineToEdit.exercises)
        }
    }

    open fun deleteExercise(id: Long) {
        mExecutor.execute {
            mDao.deleteExercise(id)
        }
    }

    fun getAllRoutineAndExerciseCount(): LiveData<Int> {
        return mDao.getAllRoutineAndExerciseCount()
    }

    @SuppressLint("RestrictedApi")
    fun  getAllRoutines(): LiveData<ArrayList<RoutineSetData>> {
        val routineSql = "select * from Routine"
        val routineStatement = RoomSQLiteQuery.acquire(routineSql, 0)

        return object : ComputableLiveData<ArrayList<RoutineSetData>>() {
            private var routineObserver: InvalidationTracker.Observer? = null
            private var exerciseObserver: InvalidationTracker.Observer? = null

            @SuppressLint("RestrictedApi")
            override fun compute(): ArrayList<RoutineSetData> {
                if (routineObserver == null) {
                    routineObserver = object : InvalidationTracker.Observer("Routine") {
                        override fun onInvalidated(tables: Set<String>) {
                            invalidate()
                        }
                    }
                    mDb.invalidationTracker.addWeakObserver(routineObserver!!)
                }
                if (exerciseObserver == null) {
                    exerciseObserver = object : InvalidationTracker.Observer("Exercise") {
                        override fun onInvalidated(tables: Set<String>) {
                            invalidate()
                        }
                    }
                    mDb.invalidationTracker.addWeakObserver(exerciseObserver!!)
                }
                val cursorOuter = mDb.query(routineStatement)
                cursorOuter.use { cursor ->
                    val _cursorIndexOfId = cursor.getColumnIndexOrThrow("id")
                    val _cursorIndexOfDesc = cursor.getColumnIndexOrThrow("description")
                    val result = ArrayList<RoutineSetData>(cursor.count)
                    var index = 0
                    while (cursor.moveToNext()) {
                        val item = RoutineSetData(
                                cursor.getLong(_cursorIndexOfId),
                                cursor.getString(_cursorIndexOfDesc))
                        item.exercises = getExerciseFromId(item.routineId)
                        result.add(item)
                        index++
                    }
                    routineStatement.release()
                    return result
                }
            }
        }.liveData
    }

    @SuppressLint("RestrictedApi")
    private fun getExerciseFromId(routineId: Long) : ArrayList<ExerciseData>{
        val exerciseSql = "select * from Exercise where routineId = ?"
        val exerciseStatement = RoomSQLiteQuery.acquire(exerciseSql, 1)
        exerciseStatement.bindLong(1, routineId)
        val cursorOuter = mDb.query(exerciseStatement)
        val results = ArrayList<ExerciseData>(cursorOuter.count)
        cursorOuter.use {cursor ->
            val _cursorIndexOfId = cursor.getColumnIndexOrThrow("id")
            val _cursorIndexOfRoutineId = cursor.getColumnIndexOrThrow("routineId")
            val _cursorIndexOfDescription = cursor.getColumnIndexOrThrow("description")
            val _cursorIndexOfLastModified = cursor.getColumnIndexOrThrow("lastModified")
            val _cursorIndexOfValue0 = cursor.getColumnIndexOrThrow("value0")
            val _cursorIndexOfValue1 = cursor.getColumnIndexOrThrow("value1")
            val _cursorIndexOfValue2 = cursor.getColumnIndexOrThrow("value2")
            var index = 0
            while(cursor.moveToNext()){
                val item = ExerciseData()
                item.id = cursor.getLong(_cursorIndexOfId)
                item.routineId = cursor.getLong(_cursorIndexOfRoutineId)
                item.description = cursor.getString(_cursorIndexOfDescription)
                val dateString = cursor.getLong(_cursorIndexOfLastModified)
                item.lastModified = Date(dateString)
                item.value0 = cursor.getString(_cursorIndexOfValue0)
                item.value1 = cursor.getString(_cursorIndexOfValue1)
                item.value2 = cursor.getString(_cursorIndexOfValue2)
                results.add(item)
                index++
            }
        }
        exerciseStatement.release()
        return results
    }

    internal inner class ThreadPerTaskExecutor : Executor {
        override fun execute(r: Runnable) {
            Thread(r).start()
        }
    }


}