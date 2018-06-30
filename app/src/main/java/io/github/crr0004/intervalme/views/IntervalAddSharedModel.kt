package io.github.crr0004.intervalme.views

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRepository
import java.util.*

class IntervalAddSharedModel(val mApplication: Application): AndroidViewModel(mApplication){

    private val mIntervalToEdit: MutableLiveData<IntervalData> = MutableLiveData()
    private var mStartingInterval: IntervalData? = null
    private val mRepo = IntervalRepository(mApplication.applicationContext)
    var isInEditMode: Boolean = false
    private var mIntervalToEditGroup: IntervalData? = null
    val intervalToEditGroup: IntervalData?
        get() {return mIntervalToEditGroup}
    val intervalToEdit: IntervalData
    get() {
        if(mIntervalToEdit.value == null){
            mIntervalToEdit.value = IntervalData()
        }
        return mIntervalToEdit.value!!
    }

    fun resetChanges(){
        mIntervalToEdit.postValue(mStartingInterval)
    }

    fun getIntervalToEdit(): MutableLiveData<IntervalData> {
        return mIntervalToEdit
    }

    fun setIntervalToEdit(intervalData: IntervalData){
        mIntervalToEdit.postValue(intervalData)
        mStartingInterval = IntervalData.forceCopy(intervalData)
    }

    fun setIntervalToEdit(id: Long, lifecycleOwner: LifecycleOwner){
        mRepo.get(id).observe(lifecycleOwner, Observer {
            if(it != null && mIntervalToEdit.value == null) {
                this.setIntervalToEdit(it)
                this.isInEditMode = true
            }
        })
    }

    fun setIntervalToEditGroup(interval: IntervalData?) {
        mIntervalToEditGroup = interval
    }

    /**
     * Commits the interval being edited to the database
     * If no group has been selected then the interval will become a group
     */
    fun commit() {
        if(mIntervalToEditGroup == null){
            mIntervalToEdit.value?.ownerOfGroup = true
            if(!isInEditMode) {
                // Don't want to insert a blank interval
                if(mIntervalToEdit.value != null) {
                    mIntervalToEdit.value?.group = UUID.randomUUID()
                    // If the interval is null, this will cause one to be generated through the getter
                    mRepo.insert(intervalToEdit)
                }
            }else{
                mRepo.update(intervalToEdit)
            }
        }else{
            if(!isInEditMode){
                mRepo.insertIntervalIntoGroup(mIntervalToEdit.value!!, mIntervalToEditGroup!!.group)
            }else{
                if(mIntervalToEdit.value!!.group != mIntervalToEditGroup!!.group){
                    mRepo.moveIntervalToGroup(mIntervalToEdit.value!!, mIntervalToEditGroup!!.group)
                }else{
                    mRepo.update(mIntervalToEdit.value!!)
                }
            }
        }
    }

}