package io.github.crr0004.intervalme.database.analytics

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface IntervalAnalyticsDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(intervalAnalyticsData: IntervalAnalyticsData): Long

    @Query("select * from IntervalAnalytics where id = :id")
    fun get(id: Long): LiveData<IntervalAnalyticsData>

    @Query("select * from IntervalAnalytics where id = :id")
    fun syncGet(id: Long): IntervalAnalyticsData
}