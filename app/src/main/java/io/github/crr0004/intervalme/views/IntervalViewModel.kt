package io.github.crr0004.intervalme.views

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDAO
import io.github.crr0004.intervalme.database.IntervalRepository
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsRepository
import java.util.*

class IntervalViewModel(val mApplication: Application): AndroidViewModel(mApplication){

    private val mRepo: IntervalRepository = IntervalRepository(mApplication.applicationContext)
    val mAnalyticsRepository: IntervalAnalyticsRepository = IntervalAnalyticsRepository(mApplication.applicationContext)

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

    fun getGroupOwner(group: UUID): LiveData<IntervalData> {
        return mRepo.getGroupOwner(group)
    }

    fun shuffleChildrenDownFrom(pos: Long, group: UUID) {
        mRepo.shuffleChildrenDownFrom(pos, group)
    }

    fun moveChildIntervalAboveChild(interval: IntervalData, intervalData: IntervalData) {
        mRepo.moveChildIntervalAboveChild(interval, intervalData)
    }

    fun moveIntervalToGroup(interval: IntervalData, groupUUID: UUID) {
        mRepo.moveIntervalToGroup(interval, groupUUID)
    }

    fun insertIntervalIntoGroup(interval: IntervalData, group: UUID) {
        mRepo.insertIntervalIntoGroup(interval, group)
    }

    fun insertIntervalIntoGroup(children: Array<IntervalData?>, group: UUID){
        mRepo.insertIntervalIntoGroup(children, group)
    }

    fun deleteGroupAndMoveChildrenToGroup(intervalData: IntervalData, group: UUID) {
        mRepo.deleteGroupAndMoveChildrenToGroup(intervalData, group)
    }

    fun moveOrphanedChildrenToGroup(group: UUID) {
        mRepo.moveOrphanedChildrenToGroup(group)
    }

    fun moveIntervalGroupAboveGroup(interval: IntervalData, intervalData: IntervalData) {
        mRepo.moveIntervalGroupAboveGroup(interval, intervalData)
    }

    fun getProperties(): LiveData<Array<IntervalRunProperties>> {
        return mRepo.getAllIntervalProperties()
    }

    fun execute(function: () -> Unit) {
        mRepo.execute(function)
    }

    fun getRepoIntervalDao(): IntervalDataDAO {
        return mRepo.intervalDao
    }

    fun startExecuteQueue() {
        mRepo.startExecuteQueue()
    }

    fun runQueue() {
        mRepo.runQueue()
    }
}