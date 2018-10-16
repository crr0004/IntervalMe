package io.github.crr0004.intervalme.routine

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val mRepo = RoutineRepo(application)
    var mRoutineToEdit: LiveData<RoutineSetData>? = null
    private var mInEditMode: Boolean = false

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


}