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
data class IntervalData(@PrimaryKey(autoGenerate = true) var id: Long?,
                        @ColumnInfo(name = "label") var label: String?,
                        @ColumnInfo(name = "group") var group: UUID,
                        var ownerOfGroup: UUID?,
                        var lastModified: Date,
                        var duration: Long
) {
    constructor():this(id=null,label=null,group=UUID.randomUUID(),ownerOfGroup = null,lastModified = Date(), duration=0)
}