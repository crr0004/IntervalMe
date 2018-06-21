package io.github.crr0004.intervalme.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "IntervalRunProperties",
        foreignKeys = [(ForeignKey(entity = IntervalData::class, parentColumns = arrayOf("id"), childColumns = arrayOf("intervalId")))]
)
data class IntervalRunProperties(@PrimaryKey(autoGenerate = true) var id: Long=0,
                                 @ColumnInfo(name = "intervalId") var intervalId: Long = 0,
                                 @ColumnInfo(name = "loops") var loops: Int = 0
){
    constructor() : this(loops = 0){}
}