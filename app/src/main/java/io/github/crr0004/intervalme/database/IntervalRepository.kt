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

    fun getGroupCount(): Long{
        return mGroupCount
    }

    fun getChildCount(group: UUID): Long{
        val intervals = mIntervalChildrenCache[group]
        return intervals?.size?.toLong() ?: 0L
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
        if(interval.ownerOfGroup){
            mIntervalGroupsCache[interval.groupPosition] = interval
        }else{
            mIntervalChildrenCache[interval.group]!![interval.groupPosition.toInt()] = interval
        }
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

    fun getAll(): LiveData<Array<IntervalData>> {
        return mIntervalDao!!.getAllLive()
    }

    fun getAllOfGroup(group: UUID): LiveData<Array<IntervalData>>{
        return mIntervalDao!!.getAllOfGroupLive(group)
    }

    fun getGroups(): LiveData<Array<IntervalData>> {
        return mIntervalDao!!.getGroupOwnersLive()
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