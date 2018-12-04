package io.github.crr0004.intervalme.interval

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties

class IntervalRecyclerAdapter(context: Context) : RecyclerView.Adapter<IntervalViewHolder>() {
    val groupCount: Int = 0
    val mChecked: SparseBooleanArray = SparseBooleanArray()
    var mInEditMode: Boolean = false

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): IntervalViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBindViewHolder(p0: IntervalViewHolder, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setProperty(intervalId: Long, intervalRunProperties: IntervalRunProperties) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun isGroupExpanded(i: Int): Boolean {
        return false
    }

    fun getItemAtPosition(keyAt: Int): IntervalData {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setItemChecked(keyAt: Int, b: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun startAllIntervals() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setGroup(groupPosition: Long, it: Array<IntervalData>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class IntervalViewHolder(v: View) : RecyclerView.ViewHolder(v) {

}
