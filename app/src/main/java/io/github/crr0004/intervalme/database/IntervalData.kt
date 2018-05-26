package io.github.crr0004.intervalme.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by crr00 on 24-Apr-18.
 */
@Entity(tableName = "Interval")
data class IntervalData(@PrimaryKey(autoGenerate = true) var id: Long=0,
                        @ColumnInfo(name = "label") var label: String? = "",
                        @ColumnInfo(name = "group") var group: UUID = UUID.randomUUID(),
                        var ownerOfGroup: Boolean = true,
                        var lastModified: Date = Date(),
                        var duration: Long = -1,
                        var runningDuration: Long = 0
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
                returnValue[i] = IntervalData(label = UUID.randomUUID().toString())
                returnValue[i]!!.duration= (Math.random()*100L).toLong()
            }

            return returnValue
        }
        fun generate(amount: Int, intervalParent: IntervalData?): Array<IntervalData?> {
            val returnValue = arrayOfNulls<IntervalData>(amount)
            for(i in 0 until returnValue.size){
                returnValue[i] = IntervalData(label = UUID.randomUUID().toString(), group = intervalParent!!.group, ownerOfGroup = false)
                returnValue[i]!!.duration= (Math.random()*100L).toLong()
            }

            return returnValue
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one. Implementations must fulfil the following
     * requirements:
     *
     * * Reflexive: for any non-null reference value x, x.equals(x) should return true.
     * * Symmetric: for any non-null reference values x and y, x.equals(y) should return true if and only if y.equals(x) returns true.
     * * Transitive:  for any non-null reference values x, y, and z, if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) should return true
     * * Consistent:  for any non-null reference values x and y, multiple invocations of x.equals(y) consistently return true or consistently return false, provided no information used in equals comparisons on the objects is modified.
     *
     * Note that the `==` operator in Kotlin code is translated into a call to [equals] when objects on both sides of the
     * operator are not null.
     */
    override fun equals(other: Any?): Boolean {
        return if(other is IntervalData){
            (this.duration == other.duration
                    && this.ownerOfGroup == other.ownerOfGroup
                    && this.group == other.group
                    && this.label == other.label
                    && this.lastModified == other.lastModified
                    && this.runningDuration == other.runningDuration)
        }else {
            super.equals(other)
        }
    }

    /**
     * Returns a string representation of the object.
     */
    override fun toString(): String {
        return "$id;$label;$duration;$ownerOfGroup;$group"
    }
}