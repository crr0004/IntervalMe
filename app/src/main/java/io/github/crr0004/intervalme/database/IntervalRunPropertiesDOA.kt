package io.github.crr0004.intervalme.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface IntervalRunPropertiesDOA {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(property: IntervalRunProperties) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(properties: Array<IntervalRunProperties?>) : List<Long>

    @Update
    fun update(property: IntervalRunProperties)

    @Query("select * from IntervalRunProperties")
    fun getAll(): LiveData<Array<IntervalRunProperties>>

    @Query("select * from IntervalRunProperties")
    fun getAllSync(): Array<IntervalRunProperties>

    @Query("select * from IntervalRunProperties where id = :id")
    fun get(id: Long): LiveData<IntervalRunProperties>

    @Query("select * from IntervalRunProperties where intervalId = :id")
    fun getForInterval(id: Long): LiveData<IntervalRunProperties>
}
