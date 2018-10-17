package io.github.crr0004.intervalme.routine

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private var mRepo = RoutineRepo(application)
    var mRoutineToEdit: MutableLiveData<RoutineSetData> = MutableLiveData()
    private var mInEditMode: Boolean = false
    val routineToEdit: RoutineSetData
        get() {
            if(mRoutineToEdit.value == null){
                mInEditMode = false
                mRoutineToEdit.value = RoutineSetData(0, "")

            }
            return mRoutineToEdit.value!!
        }

    fun setRoutineToEdit(routineEditId: Long) {
        mRoutineToEdit = mRepo.getRoutineSetById(routineEditId)
        mInEditMode = true
    }

    fun commit() {
        if(mInEditMode){
            // We commit an existing routine
        }else{
            mRepo.insert(mRoutineToEdit)
        }
    }

    fun setRepo(repo: RoutineRepo) {
        mRepo = repo
    }


}