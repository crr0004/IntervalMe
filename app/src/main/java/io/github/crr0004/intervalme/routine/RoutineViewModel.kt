package io.github.crr0004.intervalme.routine

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        var repoOverride: RoutineRepo? = null
    }

    private var mRepo = RoutineRepo(application)
    var mRoutineToEdit: MutableLiveData<RoutineSetData> = MutableLiveData()
    private val mExercisesToBeDeleted: ArrayList<ExerciseData> = ArrayList(1)
    private var mInEditMode: Boolean = false

    private lateinit var mRoutineAddedListener: (RoutineSetData) -> Unit

    val routineToEdit: RoutineSetData
        get() {
            if(mRoutineToEdit.value == null){
                mInEditMode = false
                mRoutineToEdit.value = RoutineSetData(0, "", isTemplate = false)

            }
            return mRoutineToEdit.value!!
        }

    init {
        if(repoOverride != null)
            mRepo = repoOverride!!
    }

    fun setRoutineToEdit(routineEditId: Long) {
        mRoutineToEdit = mRepo.getRoutineSetById(routineEditId)
        mInEditMode = true
    }

    fun commit() {
        if(mInEditMode){
            // We commit an existing routine
            mExercisesToBeDeleted.forEach {
                mRepo.deleteExercise(it.id)
            }
            mRepo.update(mRoutineToEdit)
        }else{
            mRepo.insert(mRoutineToEdit)
        }
    }

    fun setRepo(repo: RoutineRepo) {
        mRepo = repo
    }

    fun deleteExerciseAt(pos: Int) {
        mExercisesToBeDeleted.add(mRoutineToEdit.value!!.exercises[pos])
        mRoutineToEdit.value!!.exercises.removeAt(pos)
    }

    fun getAllRoutines() : LiveData<ArrayList<RoutineSetData>> {
        return mRepo.getAllRoutines()
    }

    fun getAllRoutineAndExerciseCount() : LiveData<Int>{
        return mRepo.getAllRoutineAndExerciseCount()
    }



    fun setOnRoutineAddedListener(arg : ((RoutineSetData) -> Unit)) {
        mRoutineAddedListener = arg
    }

    fun deleteRoutine(routineData: RoutineSetData) {
        mRepo.deleteRoutineById(routineData.routineId)
    }

    fun update(exerciseData: ExerciseData) {
        mRepo.update(exerciseData)
    }
}