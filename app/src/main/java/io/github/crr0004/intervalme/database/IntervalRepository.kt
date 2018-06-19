package io.github.crr0004.intervalme.database

import android.arch.lifecycle.LiveData
import android.content.Context
import java.util.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class IntervalRepository {

    private var mdb: IntervalMeDatabase? = null
    private var mIntervalDao: IntervalDataDOA? = null
    private val mIntervalChildrenCache: HashMap<UUID, ArrayList<IntervalData>> = HashMap()
    private val mIntervalGroupsCache: HashMap<Long, IntervalData> = HashMap()
    private var mExecutor: Executor = ThreadPerTaskExecutor()
    public var executor: Executor
        set(value) {mExecutor = value}
        get() {return mExecutor}
    private var mGroupCount: Long = 0L

    constructor(mContext: Context){
        mdb = IntervalMeDatabase.getInstance(mContext)
        mIntervalDao = mdb!!.intervalDataDao()
    }

    private val buildGroupAndChildOffsetCacheRunnable = {
        mGroupCount = mIntervalDao!!.getGroupOwnersCount()
        val groups = mIntervalDao!!.getGroupOwners()
        groups.forEachIndexed { index, intervalGroup ->
            mIntervalGroupsCache[index.toLong()] = intervalGroup
            val children = mIntervalDao!!.getAllOfGroupWithoutOwner(intervalGroup.group)
            mIntervalChildrenCache[intervalGroup.group] = ArrayList(children.size)
            children.forEachIndexed { childIndex, interval ->
                mIntervalChildrenCache[intervalGroup.group]!!.add(childIndex, interval)
            }
        }
    }

    fun setExecutorToSync(){
        mExecutor = SynchronousExecutor()
    }

    fun buildGroupAndChildOffsetCache(async: Boolean = true){
        mExecutor.execute(buildGroupAndChildOffsetCacheRunnable)
    }

    fun getGroupByOffset(offset: Long): IntervalData{
        val returnInterval = mIntervalGroupsCache[offset-1]



        return returnInterval!!
    }

    fun getChildOfGroupByOffset(offset: Long, group: UUID): IntervalData{
        val intervals = mIntervalChildrenCache[group]
        var interval: IntervalData? = null
        if(intervals != null && intervals.size > offset-1){
            interval = intervals[offset.toInt()-1]
        }

        return interval!!
    }

    fun release(){
        mIntervalDao = null
        mdb = null
    }

    fun insert(intervalInput: Array<IntervalData?>, listToReturn: ArrayList<Long> = ArrayList()): List<Long> {
        mExecutor.execute {
            val ids = mIntervalDao!!.insert(intervalInput)
            listToReturn.addAll(ids)
        }
         return listToReturn
    }

    fun insert(intervalInput: IntervalData) {
        mExecutor.execute {
           val id = mIntervalDao!!.insert(intervalInput)
            @Synchronized
            intervalInput.id = id
        }
    }

    fun update(interval: IntervalData){
        mExecutor.execute {
            mIntervalDao!!.update(interval)
        }
    }

    fun get(id: Long): LiveData<IntervalData> {
        return mIntervalDao!!.getLive(id)
    }

    fun deleteAll() {
        mIntervalDao!!.deleteAll()
    }






    fun getAllOfGroup(group: UUID): LiveData<Array<IntervalData>>{
        return mIntervalDao!!.getAllOfGroupLive(group)
    }

    fun getGroups(): LiveData<Array<IntervalData>> {
        return mIntervalDao!!.getGroupOwnersLive()
    }

    fun shuffleChildrenInGroupUpFrom(groupPosition: Long, group: UUID) {
        mExecutor.execute { mIntervalDao!!.shuffleChildrenInGroupUpFrom(groupPosition, group)}
    }

    fun shuffleGroupsUpFrom(groupPosition: Long){
       mExecutor.execute { mIntervalDao!!.shuffleGroupsUpFrom(groupPosition)}
    }

    fun getChildCountLive(groupUUID: UUID): LiveData<Long> {
        return mIntervalDao!!.getChildSizeOfGroupLive(groupUUID)
    }

    fun getGroupsSize(): LiveData<Long> {
        return mIntervalDao!!.getGroupsCountLive()
    }

    fun delete(intervalData: IntervalData) {
        mIntervalDao!!.delete(intervalData)
    }

    fun getGroupOwner(group: UUID): LiveData<IntervalData> {
        return mIntervalDao!!.getOwnerOfGroupLive(group)
    }

    fun shuffleChildrenDownFrom(pos: Long, group: UUID) {
        executor.execute { mIntervalDao!!.shuffleChildrenDownFrom(pos, group) }
    }

    fun moveIntervalAbove(interval: IntervalData, intervalData: IntervalData) {
        executor.execute {
            mIntervalDao!!.shuffleChildrenInGroupUpFrom(interval.groupPosition, interval.group)

            // Make room in the group for the incoming interval
            // -1 from groupPosition so it gets moved as well
            mIntervalDao!!.shuffleChildrenDownFrom(intervalData.groupPosition - 1, intervalData.group)

            interval.group = intervalData.group
            // Put the interval into the spot above the dropped onto item
            interval.groupPosition = intervalData.groupPosition
            mIntervalDao!!.update(interval)
        }
    }


    internal inner class ThreadPerTaskExecutor : Executor {
        override fun execute(r: Runnable) {
            Thread(r).start()
        }
    }

    internal inner class SynchronousExecutor : Executor {
        override fun execute(r: Runnable) {
            r.run()
        }
    }

}