package io.github.crr0004.intervalme.database.routine

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import java.util.concurrent.Executor

open class RoutineRepo(mContext: Context) {
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
            mDao.insert(RoutineTableData(0, routineToEdit.description))
            mDao.insert(routineToEdit.exercises)
        }
    }

    fun getRoutineSetById(routineId: Long, routineSetData: LiveData<RoutineSetData>? = null) : MutableLiveData<RoutineSetData>{
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

    internal inner class ThreadPerTaskExecutor : Executor {
        override fun execute(r: Runnable) {
            Thread(r).start()
        }
    }


}