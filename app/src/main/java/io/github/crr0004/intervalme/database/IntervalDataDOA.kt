package io.github.crr0004.intervalme.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.ABORT
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import java.util.*

/**
 * Created by crr00 on 24-Apr-18.
 */
@Dao
interface IntervalDataDOA {

    @Insert(onConflict = ABORT)
    fun insert(interval: IntervalData) : Long

    @Insert(onConflict = REPLACE)
    fun insert(intervals: Array<IntervalData?>) : List<Long>

    @Update
    fun updateThese(vararg intervals: IntervalData)

    @Update
    fun update(interval: IntervalData)

    @Delete
    fun delete(vararg interval: IntervalData): Int

    @Query("select * from Interval")
    fun getAll(): Array<IntervalData>

    @Query("select * from Interval where id = :id")
    fun get(id: Long): IntervalData

    @Query("select * from Interval where id = :id")
    fun getLive(id: Long): LiveData<IntervalData>

    @Query("select * from Interval where Interval.`group` = :group")
    fun getAllOfGroup(group: UUID): Array<IntervalData>

    @Query("select * from Interval where `group` = :group AND NOT ownerOfGroup order by groupPosition")
    fun getAllOfGroupWithoutOwner(group: UUID): Array<IntervalData>

    @Query("select * from Interval where `group` = :group AND ownerOfGroup")
    fun getOwnerOfGroup(group: UUID): IntervalData

    @Query("select * from Interval where ownerOfGroup")
    fun getGroupOwners(): Array<IntervalData>

    @Query("select * from Interval where ownerOfGroup")
    fun getGroupOwnersLive(): LiveData<Array<IntervalData>>

    @Query("select * from Interval where id = :id AND ownerOfGroup")
    fun getGroupOwnerWithID(id: Long): IntervalData

    //We minus 1 because offset is zero index
    @Query("select * from Interval where ownerOfGroup order by id limit 1 offset :offset-1")
            /**
             * @param offset The non-zero index of the group
             */
    fun getGroupByOffset(offset: Long): IntervalData

    @Query("select * from Interval where (NOT ownerOfGroup) AND `group` = :group order by groupPosition limit 1 offset :offset-1")
    fun getChildOfGroupByOffset(offset: Long, group: UUID): IntervalData

    @Query("update Interval set groupPosition = groupPosition - 1 where `group` = :group AND groupPosition > :from AND NOT ownerOfGroup")
    fun shuffleChildrenInGroupUpFrom(from: Long, group: UUID)

    @Query("select COUNT(id) from Interval WHERE (NOT ownerOfGroup) AND `group` = :group")
    fun getChildSizeOfGroup(group: UUID): Long

    @Query("delete from Interval")
    fun deleteAll()

    @Query("update Interval set groupPosition = groupPosition + 1 where `group` = :group AND groupPosition > :from AND NOT ownerOfGroup")
    fun shuffleChildrenDownFrom(from: Long, group: UUID)

    @Query("select * from Interval where `group` = :group AND ownerOfGroup")
    fun getGroupByUUID(group: UUID): IntervalData?

    @Query("select * from Interval where NOT ownerOfGroup AND `group` NOT IN (select `group` from Interval WHERE ownerOfGroup)")
    fun getIntervalsWithoutGroups(): Array<IntervalData>

    @Query("select * from Interval where lastModified >= :since ORDER BY groupPosition")
    fun getModifiedIntervalsSince(since: Date): Array<IntervalData>

    @Query("select COUNT(id) from Interval where `group` = :group AND NOT ownerOfGroup")
    fun getGroupCount(group: UUID): Int

    @Query("select COUNT(id) from Interval where ownerOfGroup")
    fun getGroupOwnersCount(): Long

    @Query("select * from Interval ORDER BY groupPosition")
    fun getAllLive() : LiveData<Array<IntervalData>>

    @Query("select * from Interval WHERE `group` = :group AND ownerOfGroup UNION select * from Interval WHERE `group` = :group AND NOT ownerOfGroup ORDER BY groupPosition")
    fun getAllOfGroupLive(group: UUID): LiveData<Array<IntervalData>>

    @Query("select COUNT(id) from Interval WHERE (NOT ownerOfGroup) AND `group` = :group")
    fun getChildSizeOfGroupLive(group: UUID): LiveData<Long>

    @Query("select COUNT(id) from Interval where ownerOfGroup")
    fun getGroupsCountLive(): LiveData<Long>
}