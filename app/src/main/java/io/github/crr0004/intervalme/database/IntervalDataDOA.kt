package io.github.crr0004.intervalme.database

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

    @Delete
    fun delete(vararg interval: IntervalData): Int

    @Query("select * from Interval")
    fun getAll(): Array<IntervalData>

    @Query("select * from Interval where id = :arg0")
    fun get(id: Long): IntervalData

    @Query("select * from Interval where Interval.`group` = :arg0")
    fun getAllOfGroup(group: UUID): Array<IntervalData>

    @Query("select * from Interval where `group` = :arg0 AND NOT ownerOfGroup")
    fun getAllOfGroupWithoutOwner(group: UUID): Array<IntervalData>

    @Query("select * from Interval where `group` = :arg0 AND ownerOfGroup")
    fun getOwnerOfGroup(group: UUID): IntervalData

    @Query("select * from Interval where ownerOfGroup")
    fun getGroupOwners(): Array<IntervalData>

    @Query("select * from Interval where id = :arg0 AND ownerOfGroup")
    fun getGroupOwnerWithID(id: Long): IntervalData

    //We minus 1 because offset is zero index
    @Query("select * from Interval where ownerOfGroup order by id limit 1 offset :arg0-1")
            /**
             * @param offset The non-zero index of the group
             */
    fun getGroupByOffset(offset: Long): IntervalData

    @Query("select * from Interval where (NOT ownerOfGroup) AND `group` = :arg1 order by id limit 1 offset :arg0-1")
    fun getChildOfGroupByOffset(offset: Long, group: UUID): IntervalData

    @Query("delete from Interval")
    fun deleteAll()
}