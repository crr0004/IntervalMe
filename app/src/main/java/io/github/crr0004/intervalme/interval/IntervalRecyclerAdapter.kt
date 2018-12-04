package io.github.crr0004.intervalme.interval

import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.views.IntervalClockView
import java.util.*

class IntervalRecyclerAdapter(context: Context) : RecyclerView.Adapter<IntervalViewHolder>() {
    val mChecked: SparseBooleanArray = SparseBooleanArray()
    var mInEditMode: Boolean = false
    private var mIntervalProperties: HashMap<Long, IntervalRunProperties>? = null
    var groups: ArrayList<IntervalData>? = null

    override fun onCreateViewHolder(p0: ViewGroup, pos: Int): IntervalViewHolder {
        val interval = groups!![pos]
        return if(interval.ownerOfGroup){
            val view = LayoutInflater.from(p0.context).inflate(R.layout.interval_group, p0, false)
            IntervalViewHolder(view)
        }else{
            val view = LayoutInflater.from(p0.context).inflate(R.layout.interval_single_clock, p0, false)
            IntervalClockViewHolder(view)
        }
    }

    override fun getItemViewType(pos: Int): Int {
        val interval = groups!![pos]
        return if(interval.ownerOfGroup){
            0
        }else{
            1
        }
    }

    override fun getItemCount(): Int {
        return groups?.size ?: 0
    }

    override fun onBindViewHolder(p0: IntervalViewHolder, pos: Int) {
        p0.bind(groups!![pos])
    }

    fun setProperty(intervalId: Long, intervalRunProperties: IntervalRunProperties) {

    }

    fun isGroupExpanded(i: Int): Boolean {
        return false
    }

    fun getItemAtPosition(keyAt: Int): IntervalData {
        return groups!![0]
    }

    fun setItemChecked(keyAt: Int, b: Boolean) {

    }

    fun startAllIntervals() {

    }

}

open class IntervalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    open fun bind(intervalData: IntervalData){
        itemView.findViewById<TextView>(R.id.intervalGroupNameTxt).text = intervalData.label ?: "Interval not found"
        itemView.findViewById<TextView>(R.id.intervalGroupPos).text = intervalData.groupPosition.toString()
        itemView.setTag(R.id.id_interval_view_interval, intervalData)
        val editButton = itemView.findViewById<AppCompatImageButton>(R.id.clockGroupEditButton)
        val deleteButton = itemView.findViewById<AppCompatImageButton>(R.id.clockGroupDeleteButton)
    }

    open fun unbind(data: IntervalData){

    }
}

open class IntervalClockViewHolder(v: View) : IntervalViewHolder(v) {
    override fun bind(intervalData: IntervalData) {
        val clockView = itemView.findViewById<IntervalClockView>(R.id.intervalClockView)
        val editButton = itemView.findViewById<AppCompatImageButton>(R.id.clockSingleEditButton)
        val deleteButton = itemView.findViewById<AppCompatImageButton>(R.id.clockSingleDeleteButton)
        val checkBox = itemView.findViewById<CheckBox>(R.id.clockEditCheckbox)
        itemView.findViewById<TextView>(R.id.clockLabelTxt).text = intervalData.label
    }

    override fun unbind(data: IntervalData) {
        super.unbind(data)
    }
}
