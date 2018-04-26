package io.github.crr0004.intervalme.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.time.Duration
import java.util.*

/**
 * Created by crr00 on 24-Apr-18.
 */
@Entity(tableName = "Interval")
data class IntervalData(@PrimaryKey(autoGenerate = true) var id: Long=0,
                        @ColumnInfo(name = "label") var label: String?,
                        @ColumnInfo(name = "group") var group: UUID = UUID.randomUUID(),
                        var ownerOfGroup: Boolean = true,
                        var lastModified: Date = Date(),
                        var duration: Long = -1
) {
    constructor():this(label="")

    /**
     * Sets the passed interval as a child of this interval.
     * Sets this interval as the owner of group
     */
    fun setAsParentOf(interval: IntervalData){
        interval.group = this.group
    }

    companion object {
        fun generate(amount: Int): Array<IntervalData?>{
            val returnValue = arrayOfNulls<IntervalData>(amount)
            for(i in 0 until returnValue.size){
                returnValue[i] = IntervalData()
                returnValue[i]!!.duration= (Math.random()*Long.MAX_VALUE).toLong()
            }

            return returnValue
        }
    }

}