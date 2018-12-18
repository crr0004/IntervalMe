package io.github.crr0004.intervalme.interval

import android.os.Parcel
import android.os.Parcelable
import io.github.crr0004.intervalme.database.IntervalData
import java.util.*

class IntervalDataParcelable(val interval: IntervalData) : Parcelable{

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(interval.id)
        parcel.writeString(interval.label)
        parcel.writeString(interval.group.toString())
        parcel.writeInt(if(interval.ownerOfGroup) 1 else 0)
        parcel.writeLong(interval.lastModified.time)
        parcel.writeLong(interval.duration)
        parcel.writeLong(interval.runningDuration)
        parcel.writeLong(interval.groupPosition)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IntervalDataParcelable> {
        override fun createFromParcel(p: Parcel): IntervalDataParcelable {
            val interval = IntervalData()
            interval.id = p.readLong()
            interval.label = p.readString()
            interval.group = UUID.fromString(p.readString())
            interval.ownerOfGroup = p.readInt() > 0
            interval.lastModified = Date(p.readLong())
            interval.duration = p.readLong()
            interval.runningDuration = p.readLong()
            interval.groupPosition = p.readLong()
            return IntervalDataParcelable(interval)
        }

        override fun newArray(size: Int): Array<IntervalDataParcelable?> {
            throw UnsupportedOperationException("Can't create intervaldata array from a parcelable")
        }
    }
}