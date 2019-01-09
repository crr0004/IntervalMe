package io.github.crr0004.intervalme.routine

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.preference.PreferenceManager
import io.github.crr0004.intervalme.database.analytics.AnalyticsRepository
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineRepo
import io.github.crr0004.intervalme.database.routine.RoutineSetData

class RoutineViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    companion object {
        var repoOverride: RoutineRepo? = null
    }

    private var mRepo = RoutineRepo(mApplication)
    val mAnalyticsRepository: AnalyticsRepository = AnalyticsRepository(mApplication.applicationContext)
    private var mRoutineToEdit: MutableLiveData<RoutineSetData> = MutableLiveData()
    private val mExercisesToBeDeleted: ArrayList<ExerciseData> = ArrayList(1)
    private var mInEditMode: Boolean = false
    val isInEditMode: Boolean
    get() = mInEditMode

    private lateinit var mRoutineAddedListener: (RoutineSetData) -> Unit

    var routineToEdit: RoutineSetData
        get() {
            if(mRoutineToEdit.value == null){
                mInEditMode = false
                mRoutineToEdit.value = RoutineSetData(0, "", isTemplate = false, isDone = false)

            }
            return mRoutineToEdit.value!!
        }
    set(value) {
        mRoutineToEdit.postValue(value)
        mInEditMode = true
    }

    init {
        if(repoOverride != null)
            mRepo = repoOverride!!
    }

    fun setRoutineToEdit(routineEditId: Long) {
        mRoutineToEdit = mRepo.getRoutineSetById(routineEditId)
        mInEditMode = true
    }

    fun copyRoutineFromTemplate(routineData: RoutineSetData){
        val routine = RoutineSetData(routineData)
        routine.isTemplate = false
        routine.routineId = 0
        routine.exercises.forEach {
            it.id = 0
            it.routineId = 0
        }
        mRoutineToEdit.postValue(routine)
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

    fun getAllRoutines(getDoneRoutines: Boolean = false): LiveData<ArrayList<RoutineSetData>> {
        return if(getDoneRoutines){
            // Should we show the incomplete routine sets when showing the done routines
            val showInCompleteRoutines = PreferenceManager.getDefaultSharedPreferences(this.mApplication).getBoolean("ui_show_incomplete_routine_menu_items", false)
            if(showInCompleteRoutines)
                mRepo.getAllRoutines("select * from Routine where isTemplate = 0 or isDone = 1")
            else
                mRepo.getAllRoutines("select * from Routine where isTemplate = 0 and isDone = 1")
        }else{
            mRepo.getAllRoutines("select * from Routine where isTemplate = 0 and isDone = 0")
        }

    }

    fun deleteRoutine(routineData: RoutineSetData) {
        mRepo.deleteRoutineById(routineData.routineId)
    }

    fun update(exerciseData: ExerciseData) {
        mRepo.update(exerciseData)
    }

    fun getTemplateRoutines() : LiveData<ArrayList<RoutineSetData>> {
        return mRepo.getTemplateRoutines()
    }

    fun getRoutineLiveData(): LiveData<RoutineSetData> {
        return mRoutineToEdit
    }

    fun update(routine: RoutineSetData) {
        mRepo.update(routine)
    }

    fun copyRoutineToTemplate(routineToEdit: RoutineSetData) {
        val routineTemplate = RoutineSetData(routineToEdit)
        routineTemplate.isTemplate = true
        routineTemplate.isDone = false
        mRepo.insert(routineTemplate)
    }
}