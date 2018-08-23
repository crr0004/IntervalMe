package io.github.crr0004.intervalme.database.analytics

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties
import java.util.*

/*
/**
 * Created by crr00 on 24-Apr-18.
 */
@Entity(tableName = "Interval")
data class IntervalData(@PrimaryKey(autoGenerate = true) var id: Long=0,
                        @ColumnInfo(name = "label") var label: String? = "",
                        @ColumnInfo(name = "group") var group: UUID = UUID.randomUUID(),
                        var ownerOfGroup: Boolean = true,
                        var lastModified: Date = Date(),
                        var duration: Long = 0,
                        var runningDuration: Long = 0,
                        var groupPosition: Long = 0
) {
 */
@Entity(tableName = "IntervalAnalytics")
data class IntervalAnalyticsData(@PrimaryKey(autoGenerate = true) var id: Long=0,
                                 var label: String? = "",
                                 var group: UUID = UUID.randomUUID(),
                                 var ownerOfGroup: Boolean = true,
                                 var lastModified: Date = Date(),
                                 var duration: Long = 0,
                                 var groupPosition: Long = 0,
                                 var loops: Int = -1) {
    constructor(interval: IntervalData) : this(){
        this.label = interval.label
        this.group = interval.group
        this.ownerOfGroup = interval.ownerOfGroup
        this.duration = interval.duration
        this.groupPosition = interval.groupPosition
    }
    constructor(interval: IntervalData, properties: IntervalRunProperties): this(interval){
        this.loops = properties.loops
    }
}