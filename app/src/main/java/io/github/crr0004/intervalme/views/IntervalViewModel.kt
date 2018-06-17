package io.github.crr0004.intervalme.views

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRepository
import java.util.*

class IntervalViewModel(val mApplication: Application): AndroidViewModel(mApplication) {
    private val mRepo: IntervalRepository = IntervalRepository(mApplication.applicationContext)

    fun getGroups(): LiveData<Array<IntervalData>> {
        return mRepo.getGroups()
    }

    fun getAllOfGroup(group: UUID): LiveData<Array<IntervalData>>{
        return mRepo.getAllOfGroup(group)
    }

    fun update(intervalData: IntervalData) {
        mRepo.update(intervalData)
    }

    fun get(id: Long): LiveData<IntervalData> {
        return mRepo.get(id)
    }

    fun shuffleChildrenInGroupUpFrom(groupPosition: Long, group: UUID) {
        mRepo.shuffleChildrenInGroupUpFrom(groupPosition, group)
    }

    fun shuffleGroupsUpFrom(groupPosition: Long){
        mRepo.shuffleGroupsUpFrom(groupPosition)
    }

    fun getChildSizeOfGroup(groupUUID: UUID): LiveData<Long> {
        return mRepo.getChildCountLive(groupUUID)
    }

    fun insert(interval: IntervalData) {
        mRepo.insert(interval)
    }

    fun getGroupsSize(): LiveData<Long> {
        return mRepo.getGroupsSize()
    }

    fun delete(intervalData: IntervalData) {
        mRepo.delete(intervalData)
    }
}