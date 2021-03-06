package io.github.crr0004.intervalme.database

import android.annotation.SuppressLint
import android.arch.lifecycle.ComputableLiveData
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.InvalidationTracker
import android.arch.persistence.room.RoomSQLiteQuery
import android.content.Context
import java.util.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class IntervalRepository(mContext: Context) {

    private var mdb: IntervalMeDatabase? = null
    private var mIntervalDao: IntervalDataDAO? = null
    private var mPropertiesDao: IntervalRunPropertiesDOA? = null
    private val mIntervalChildrenCache: HashMap<UUID, ArrayList<IntervalData>> = HashMap()
    private val mIntervalGroupsCache: HashMap<Long, IntervalData> = HashMap()
    private var mExecutor: Executor = ThreadPerTaskExecutor()
    var executor: Executor
        set(value) {mExecutor = value}
        get() {return mExecutor}
    private var mGroupCount: Long = 0L

    init {
        mdb = IntervalMeDatabase.getInstance(mContext)
        mIntervalDao = mdb!!.intervalDataDao()
        mPropertiesDao = mdb!!.propertiesDao()
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
    val intervalDao: IntervalDataDAO
    get() {return mIntervalDao!!}

    fun setExecutorToSync(){
        mExecutor = SynchronousExecutor()
    }

    fun buildGroupAndChildOffsetCache() {
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
            if(intervalInput.ownerOfGroup){
                intervalInput.groupPosition = mIntervalDao!!.getGroupOwnersCount()
            }
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

    fun updateIntervalToGroup(interval: IntervalData) {
        mExecutor.execute {
            // Move all the children in the current group up
            mIntervalDao!!.shuffleChildrenInGroupUpFrom(interval.groupPosition, interval.group)
            interval.ownerOfGroup = true
            interval.group = UUID.randomUUID()
            interval.groupPosition = mIntervalDao!!.getGroupOwnersCount()
            mIntervalDao!!.update(interval)
        }
    }

    fun moveChildIntervalAboveChild(interval: IntervalData, moveIntervalAbove: IntervalData) {
        executor.execute {
            mIntervalDao!!.shuffleChildrenInGroupUpFrom(interval.groupPosition, interval.group)

            // Make room in the group for the incoming interval
            // -1 from groupPosition so it gets moved as well
            mIntervalDao!!.shuffleChildrenDownFrom(moveIntervalAbove.groupPosition - 1, moveIntervalAbove.group)

            interval.group = moveIntervalAbove.group
            // Put the interval into the spot above the dropped onto item
            interval.groupPosition = moveIntervalAbove.groupPosition
            mIntervalDao!!.update(interval)
        }
    }

    fun moveIntervalToGroup(interval: IntervalData, groupUUID: UUID) {
        executor.execute {
            if(interval.ownerOfGroup) {
                mIntervalDao!!.shuffleGroupsUpFrom(interval.groupPosition)
            }else{
                mIntervalDao!!.shuffleChildrenInGroupUpFrom(interval.groupPosition, interval.group)
            }
            interval.group = groupUUID
            interval.ownerOfGroup = false
            interval.groupPosition = mIntervalDao!!.getChildSizeOfGroup(groupUUID)
            mIntervalDao!!.update(interval)
        }
    }

    fun insertIntervalIntoGroup(interval: IntervalData, group: UUID) {
        executor.execute {
            // Fixes the group positions in the old group
            // This will have a net affect of nothing if it's the same group
            mIntervalDao!!.shuffleChildrenInGroupUpFrom(interval.groupPosition, interval.group)
            interval.ownerOfGroup = false
            interval.group = group
            interval.groupPosition = mIntervalDao!!.getChildSizeOfGroup(group)
            mIntervalDao!!.insert(interval)
        }
    }
    fun insertIntervalIntoGroup(children: Array<IntervalData?>, group: UUID) {
        executor.execute {
            val groupPosStart = mIntervalDao!!.getChildSizeOfGroup(group)
            children.forEachIndexed { index, intervalData ->
                // Fixes the group positions in the old group
                if(intervalData != null) {
                    if(intervalData.group != group)
                        mIntervalDao!!.shuffleChildrenInGroupUpFrom(intervalData.groupPosition, intervalData.group)
                    intervalData.ownerOfGroup = false
                    intervalData.groupPosition = groupPosStart + index
                    intervalData.group = group
                }
            }
            mIntervalDao!!.insert(children)
        }
    }

    fun deleteGroupAndMoveChildrenToGroup(intervalData: IntervalData, group: UUID) {
        executor.execute {
            val childrenToMove = mIntervalDao!!.getAllOfGroupWithoutOwner(intervalData.group)
            var groupPosStart = mIntervalDao!!.getChildSizeOfGroup(group)
            if(groupPosStart < 1) groupPosStart = 0
            mPropertiesDao!!.deleteByIntervalId(intervalData.id)
            mIntervalDao!!.delete(intervalData)
            mIntervalDao!!.shuffleGroupsUpFrom(intervalData.groupPosition)
            childrenToMove.forEachIndexed { index, child ->
                child.ownerOfGroup = false // probably don't need it but eh
                child.groupPosition = groupPosStart+index
                child.group = group
                mIntervalDao!!.update(child)
            }

        }
    }

    fun moveOrphanedChildrenToGroup(group: UUID) {
        executor.execute {
            var etcGroup = mIntervalDao!!.getGroupByUUID(group)
            val startingGroupPos: Long
            if(etcGroup == null){
                // Create etcgroup
                etcGroup = IntervalData(label = "ETC", group = group, ownerOfGroup = true, groupPosition = mIntervalDao!!.getGroupOwnersCount())
                mIntervalDao!!.insert(etcGroup)
                startingGroupPos = 0
            }else{
                startingGroupPos = mIntervalDao!!.getChildSizeOfGroup(group)
            }
            val intervalsWithoutGroup = mIntervalDao!!.getIntervalsWithoutGroups()
            intervalsWithoutGroup.forEachIndexed { index, intervalData ->
                intervalData.ownerOfGroup = false
                intervalData.group = group
                intervalData.groupPosition = startingGroupPos+index
                mIntervalDao!!.update(intervalData)
            }
        }
    }

    fun moveIntervalGroupAboveGroup(interval: IntervalData, intervalData: IntervalData) {
        executor.execute {
            mIntervalDao!!.shuffleGroupsUpFrom(interval.groupPosition)

            // Make room in the group for the incoming interval
            // -1 from groupPosition so it gets moved as well
            mIntervalDao!!.shuffleGroupsDownFrom(intervalData.groupPosition - 1)

            // Put the interval into the spot above the dropped onto item
            interval.groupPosition = intervalData.groupPosition
            mIntervalDao!!.update(interval)
        }
    }

    fun getAllIntervalProperties(): LiveData<Array<IntervalRunProperties>> {
        return mPropertiesDao!!.getAll()
    }

    fun execute(function: () -> Unit) {
        executor.execute(function)
    }

    fun startExecuteQueue() {
        executor = QueueExecutor()
    }

    fun runQueue() {
        (executor as QueueExecutor).runQueue()
        executor = ThreadPerTaskExecutor()
    }

    fun getPropertiesOfInterval(id: Long): LiveData<IntervalRunProperties> {
        return mPropertiesDao!!.getForInterval(id)
    }

    fun update(intervalProperty: IntervalRunProperties) {
        executor.execute {mPropertiesDao!!.update(intervalProperty)}
    }

    fun insert(intervalInput: IntervalData, intervalRunProperties: IntervalRunProperties) {
        mExecutor.execute {
            if(intervalInput.ownerOfGroup){
                intervalInput.groupPosition = mIntervalDao!!.getGroupOwnersCount()
            }
            val id = mIntervalDao!!.insert(intervalInput)
            @Synchronized
            intervalInput.id = id
            intervalRunProperties.intervalId = id
            mPropertiesDao!!.insert(intervalRunProperties)
        }
    }

    fun insert(intervalToEditProperties: IntervalRunProperties) {
        mExecutor.execute {
            mPropertiesDao!!.insert(intervalToEditProperties)
        }
    }

    fun deleteChild(childOfInterval: IntervalData) {
        mExecutor.execute {
            mIntervalDao!!.shuffleChildrenInGroupUpFrom(childOfInterval.groupPosition, childOfInterval.group)
            mPropertiesDao!!.deleteByIntervalId(childOfInterval.id)
            mIntervalDao!!.delete(childOfInterval)
        }
    }

    @SuppressLint("RestrictedApi")
    fun getAllGroupsAsHashMap(): LiveData<ArrayList<IntervalData>> {
        return object : ComputableLiveData<ArrayList<IntervalData>>(){
            private var intervalObserver: InvalidationTracker.Observer? = null

            override fun compute(): ArrayList<IntervalData> {
                val groupsStatement = RoomSQLiteQuery.acquire(
                        "select * from Interval where ownerOfGroup order by groupPosition",
                        0
                )
                val allResults: ArrayList<IntervalData> = ArrayList()
                if(intervalObserver == null){
                    intervalObserver = object : InvalidationTracker.Observer("Interval"){
                        override fun onInvalidated(tables: MutableSet<String>) {
                            invalidate()
                        }
                    }
                    mdb?.invalidationTracker?.addWeakObserver(intervalObserver)
                }
                val groupCursorOut = mdb!!.query(groupsStatement)
                groupCursorOut.use {cursor ->
                    val cursorIndexOfId = cursor.getColumnIndexOrThrow("id")
                    val cursorIndexOfLabel = cursor.getColumnIndexOrThrow("label")
                    val cursorIndexOfGroup = cursor.getColumnIndexOrThrow("group")
                    // We know this interval is an owner of the group
                    //val cursorIndexOfOwnerOfGroup = groupCursor.getColumnIndexOrThrow("ownerOfGroup")
                    val cursorIndexOfLastModified = cursor.getColumnIndexOrThrow("lastModified")
                    val cursorIndexOfDuration = cursor.getColumnIndexOrThrow("duration")
                    val cursorIndexOfRunningDuration = cursor.getColumnIndexOrThrow("runningDuration")
                    val cursorIndexOfGroupPosition = cursor.getColumnIndexOrThrow("groupPosition")
                    allResults.ensureCapacity(cursor.count)
                    while(cursor.moveToNext()){
                        val group = cursor.getString(cursorIndexOfGroup)
                        val intervalGroup = IntervalData(
                                cursor.getLong(cursorIndexOfId),
                                cursor.getString(cursorIndexOfLabel),
                                UUID.fromString(cursor.getString(cursorIndexOfGroup)),
                                true,
                                Date(cursor.getLong(cursorIndexOfLastModified)),
                                cursor.getLong(cursorIndexOfDuration),
                                cursor.getLong(cursorIndexOfRunningDuration),
                                cursor.getLong(cursorIndexOfGroupPosition))
                        allResults.add(intervalGroup)
                        getAllOfGroupCursor(group, allResults)
                    }
                    groupsStatement.release()
                    return allResults
                }
            }
        }.liveData

    }

    @SuppressLint("RestrictedApi")
    private fun getAllOfGroupCursor(group: String, allResults: ArrayList<IntervalData>){
        val groupsStatement = RoomSQLiteQuery.acquire(
                "select * from Interval where not ownerOfGroup and `group` = ? order by groupPosition",
                1
        )
        groupsStatement.bindString(1, group)
        val groupCursor = mdb!!.query(groupsStatement)
        groupCursor.use {
            val cursorIndexOfId = groupCursor.getColumnIndexOrThrow("id")
            val cursorIndexOfLabel = groupCursor.getColumnIndexOrThrow("label")
            val cursorIndexOfGroup = groupCursor.getColumnIndexOrThrow("group")
            // We know this interval isn't an owner of the group
            //val cursorIndexOfOwnerOfGroup = groupCursor.getColumnIndexOrThrow("ownerOfGroup")
            val cursorIndexOfLastModified = groupCursor.getColumnIndexOrThrow("lastModified")
            val cursorIndexOfDuration = groupCursor.getColumnIndexOrThrow("duration")
            val cursorIndexOfRunningDuration = groupCursor.getColumnIndexOrThrow("runningDuration")
            val cursorIndexOfGroupPosition = groupCursor.getColumnIndexOrThrow("groupPosition")
            while(it.moveToNext()) {
                val data = IntervalData(
                        it.getLong(cursorIndexOfId),
                        it.getString(cursorIndexOfLabel),
                        UUID.fromString(it.getString(cursorIndexOfGroup)),
                        false,
                        Date(it.getLong(cursorIndexOfLastModified)),
                        it.getLong(cursorIndexOfDuration),
                        it.getLong(cursorIndexOfRunningDuration),
                        it.getLong(cursorIndexOfGroupPosition)
                )
                allResults.add(data)
            }
            groupsStatement.release()
            return
        }
    }

    fun fixGroupPositions() {
        mExecutor.execute{
            val groups = intervalDao.getGroupOwners()
            groups.forEachIndexed { i, it ->
                val groupMembers = intervalDao.getAllOfGroupWithoutOwner(it.group)
                groupMembers.forEachIndexed { index, intervalData ->
                    intervalData.groupPosition = (index).toLong()
                    intervalDao.update(intervalData)
                }
                it.groupPosition = i.toLong()
                intervalDao.update(it)
            }
        }
    }


    internal inner class QueueExecutor : Executor{
        @get:Synchronized
        private val queue = ArrayList<Runnable>()
        private var running = false
        override fun execute(p0: Runnable?) {
            if(running)
                throw RuntimeException("Trying to add runnable when queue is running")
            queue.add(p0!!)
        }

        fun runQueue(){
            Thread( {
                queue.forEachIndexed { _, runnable ->
                    runnable.run()
                }
            }, "Queue Runnable").start()
            running = true
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