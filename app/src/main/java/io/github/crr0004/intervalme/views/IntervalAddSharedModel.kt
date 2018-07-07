package io.github.crr0004.intervalme.views

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRepository
import io.github.crr0004.intervalme.database.IntervalRunProperties
import java.util.*

class IntervalAddSharedModel(val mApplication: Application): AndroidViewModel(mApplication){

    private val mIntervalToEdit: MutableLiveData<IntervalData> = MutableLiveData()
    var mIntervalToEditProperties: MutableLiveData<IntervalRunProperties> = MutableLiveData()
    private var mStartingInterval: IntervalData? = null
    private val mRepo = IntervalRepository(mApplication.applicationContext)
    var isInEditMode: Boolean = false
    private var mIntervalToEditGroup: IntervalData? = null
    val intervalToEditProperties: IntervalRunProperties
    get() {
        if(mIntervalToEditProperties.value == null){
            mIntervalToEditProperties.value = IntervalRunProperties(intervalId = intervalToEdit.id)
        }
        return mIntervalToEditProperties.value!!
    }
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
        mRepo.getPropertiesOfInterval(id).observe(lifecycleOwner, Observer<IntervalRunProperties> {
            if(it != null && mIntervalToEditProperties.value == null){
                mIntervalToEditProperties.value = it
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
        // TODO: Take care of case with runproperties where we edit an interval with a property that doesn't exist
        if(mIntervalToEditGroup == null){
            mIntervalToEdit.value?.ownerOfGroup = true
            if(!isInEditMode) {
                // If the interval is null, insert will cause one to be generated through the getter
                // Don't want to insert a blank interval
                if(mIntervalToEdit.value != null) {
                    mIntervalToEdit.value?.group = UUID.randomUUID()
                    mRepo.insert(intervalToEdit, intervalToEditProperties)
                }
            }else{
                mRepo.update(intervalToEdit)
                if(intervalToEditProperties.id < 1) {
                    mRepo.insert(intervalToEditProperties)
                }else{
                    mRepo.update(intervalToEditProperties)
                }
            }
        }else{
            if(!isInEditMode){
                mRepo.insertIntervalIntoGroup(mIntervalToEdit.value!!, mIntervalToEditGroup!!.group)
            }else{
                if(mIntervalToEdit.value!!.group != mIntervalToEditGroup!!.group){
                    mRepo.moveIntervalToGroup(mIntervalToEdit.value!!, mIntervalToEditGroup!!.group)
                    mRepo.update(intervalToEditProperties)
                }else{
                    mRepo.update(mIntervalToEdit.value!!)
                    if(intervalToEditProperties.id < 1) {
                        mRepo.insert(intervalToEditProperties)
                    }else{
                        mRepo.update(intervalToEditProperties)
                    }
                }
            }
        }
    }

}