package io.github.crr0004.intervalme.interval

import android.animation.ObjectAnimator
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.RecyclerView
import android.util.Log
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
import kotlinx.android.synthetic.main.interval_group.view.*
import kotlinx.android.synthetic.main.interval_single_clock.view.*
import java.util.*
import kotlin.collections.HashMap

class IntervalRecyclerAdapter(val mHost: IntervalListActivity) : RecyclerView.Adapter<IntervalViewHolder>(), IntervalControllerFacade.IntervalControllerDataSourceI, IntervalRecyclerViewHolderActionsI {

    var mInEditMode: Boolean = false
    private var mRecyclerView: RecyclerView? = null
    private var mIntervalProperties: HashMap<Long, IntervalRunProperties> = HashMap()
    private val mCheckedItems: SparseBooleanArray = SparseBooleanArray()
    private val mExpandedGroups: SparseBooleanArray = SparseBooleanArray()
    var mGroups: ArrayList<IntervalData>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, pos: Int): IntervalViewHolder {
        val interval = mGroups!![pos]
        return if(interval.ownerOfGroup){
            val view = LayoutInflater.from(p0.context).inflate(R.layout.interval_group, p0, false)
            IntervalViewHolder(view, this)
        }else{
            val view = LayoutInflater.from(p0.context).inflate(R.layout.interval_single_clock, p0, false)
            IntervalClockViewHolder(view, this)
        }
    }

    override fun getItemViewType(pos: Int): Int {
        val interval = mGroups!![pos]
        return if(interval.ownerOfGroup){
            0
        }else{
            1
        }
    }

    override fun getItemCount(): Int {
        return mGroups?.size ?: 0
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    private fun getGroupFromInterval(pos: Int, intervalData: IntervalData) : IntervalData{
        return mGroups!![(pos-intervalData.groupPosition).toInt()]
    }

    override fun onBindViewHolder(p0: IntervalViewHolder, pos: Int) {
        val data = mGroups!![pos]
        if(getItemViewType(pos) == 1)
            (p0 as IntervalClockViewHolder).bind(data, getGroupFromInterval(pos, data))
        else
            p0.bind(data)
    }

    fun setProperty(intervalId: Long, intervalRunProperties: IntervalRunProperties) {
        mIntervalProperties[intervalId] = intervalRunProperties
    }

    fun isGroupExpanded(i: Int): Boolean {
        return false
    }

    fun getItemAtPosition(keyAt: Int): IntervalData {
        return mGroups!![keyAt]
    }

    override fun setItemChecked(adapterPosition: Int, checked: Boolean) {
        mCheckedItems.put(adapterPosition, checked)
    }

    fun startAllIntervals() {

    }

    private fun getGroup(groupPosition: Int) : IntervalData?{
        return mGroups!!.find{
            it.ownerOfGroup && it.groupPosition.toInt() == groupPosition
        }
    }

    override fun facadeGetGroup(groupPosition: Int): IntervalData {
        return getGroup(groupPosition)!!
    }

    override fun facadeGetGroupSize(groupPosition: Int): Int {
        val group = getGroup(groupPosition)!!
        return mGroups!!.count { !it.ownerOfGroup && it.group == group.group }
    }

    override fun facadeGetChild(groupPosition: Int, index: Int): IntervalData {
        val group = getGroup(groupPosition)!!
        return mGroups!!.find {
            !it.ownerOfGroup && it.group == group.group && it.groupPosition.toInt() == index
        }!!
    }

    override fun facadeGetIDFromPosition(groupPosition: Int): UUID {
        return getGroup(groupPosition)!!.group
    }

    override fun getGroupProperties(groupPosition: Long): IntervalRunProperties? {
        return null
    }

    override fun isInEditMode(): Boolean {
        return mInEditMode
    }

    override fun launchAddInEditMode(childOfInterval: IntervalData) {
        mHost.launchAddInEditMode(childOfInterval)
    }

    override fun itemChecked(intervalClockViewHolder: IntervalClockViewHolder): Boolean {
        return mCheckedItems.get(intervalClockViewHolder.adapterPosition, false)
    }

    override fun deleteChild(childOfInterval: IntervalData) {
        mHost.deleteChild(childOfInterval)
    }

    override fun deleteGroup(intervalData: IntervalData) {
        mHost.deleteGroupMoveChildrenToETC(intervalData)
    }

    fun getCheckedItems(): SparseBooleanArray {
        return mCheckedItems
    }

    fun clearCheckedItems() {
        mCheckedItems.clear()
    }

    override fun getPropertiesFor(intervalData: IntervalData): IntervalRunProperties? {
        return mIntervalProperties[intervalData.id]
    }

    override fun toggleGroupExpanded(intervalViewHolder: IntervalViewHolder){
        mExpandedGroups.put(intervalViewHolder.adapterPosition, !mExpandedGroups[intervalViewHolder.adapterPosition, false])
    }
    override fun shouldShow(intervalClockViewHolder: IntervalClockViewHolder): Boolean{
        val data = mGroups!![intervalClockViewHolder.adapterPosition]
        return mExpandedGroups[(intervalClockViewHolder.adapterPosition-data.groupPosition-1).toInt(), false]
    }

    private fun getGroupPos(group: UUID): Int {
        var i = -1
        mGroups!!.filterIndexed { index, it ->
            i = index
           it.ownerOfGroup && it.group == group
        }
        return i
    }

    override fun groupChangedAt(adapterPosition: Int){
        if(getItemViewType(adapterPosition) == 0)
            notifyItemRangeChanged(adapterPosition+1, facadeGetGroupSize(mGroups!![adapterPosition].groupPosition.toInt()))
    }

    override fun groupExpanded(adapterPosition: Int): Boolean{
        return mExpandedGroups[adapterPosition, false]
    }

    override fun onViewRecycled(holder: IntervalViewHolder) {
        Log.d("IRA", "Recycled " + holder.data?.label)
        super.onViewRecycled(holder)
    }

    override fun getItemId(position: Int): Long {
        return mGroups!![position].id
    }
}

interface IntervalRecyclerViewHolderActionsI{
    fun isInEditMode(): Boolean
    fun itemChecked(intervalClockViewHolder: IntervalClockViewHolder): Boolean
    fun setItemChecked(adapterPosition: Int, checked: Boolean)
    fun launchAddInEditMode(childOfInterval: IntervalData)
    fun deleteChild(childOfInterval: IntervalData)
    fun deleteGroup(intervalData: IntervalData)
    fun getPropertiesFor(intervalData: IntervalData): IntervalRunProperties?
    fun toggleGroupExpanded(intervalViewHolder: IntervalViewHolder)
    fun shouldShow(intervalClockViewHolder: IntervalClockViewHolder): Boolean
    fun groupChangedAt(adapterPosition: Int)
    fun groupExpanded(adapterPosition: Int): Boolean
}

open class IntervalViewHolder(v: View, val mHost: IntervalRecyclerViewHolderActionsI) : RecyclerView.ViewHolder(v) {
    protected var mData: IntervalData? = null
    val data: IntervalData?
    get() {return mData}
    open fun bind(intervalData: IntervalData, expanded: Boolean = false){
        Log.d("IRA", "Binding $adapterPosition")
        this.mData = intervalData
        itemView.findViewById<TextView>(R.id.intervalGroupNameTxt).text = intervalData.label
        itemView.findViewById<TextView>(R.id.intervalGroupPos).text = intervalData.groupPosition.toString()
        itemView.setTag(R.id.id_interval_view_interval, intervalData)
        val editButton = itemView.findViewById<AppCompatImageButton>(R.id.clockGroupEditButton)
        val deleteButton = itemView.findViewById<AppCompatImageButton>(R.id.clockGroupDeleteButton)
        IntervalControllerFacade.instance.setUpGroupOrder(intervalData.groupPosition.toInt(), itemView.context)
        itemView.intervalGroupLoops.text = (mHost.getPropertiesFor(intervalData)?.loops ?: "").toString()
        if(mHost.isInEditMode()){
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
        }else{
            editButton.visibility = View.INVISIBLE
            deleteButton.visibility = View.INVISIBLE
        }
        editButton.setOnClickListener {
            mHost.launchAddInEditMode(intervalData)
        }
        deleteButton.setOnClickListener{
            mHost.deleteGroup(intervalData)
        }
        if(mHost.groupExpanded(adapterPosition)) {
            itemView.clockGroupMoreButton.rotation = 180f
        }else{
            itemView.clockGroupMoreButton.rotation = 0f
        }
        itemView.clockGroupMoreButton.setOnSystemUiVisibilityChangeListener {
            Log.d("IRA", "Clock more button at $adapterPosition changed visibility")
        }
        itemView.clockGroupMoreButton.setOnClickListener {view ->
            mHost.toggleGroupExpanded(this)
            if(mHost.groupExpanded(adapterPosition)) {
                ObjectAnimator.ofFloat(view, "rotation", 0f, 180f).apply {
                    duration = 150
                    start()
                }
                /*
                AnimationUtils.loadAnimation(view.context, R.anim.rotate_180_degrees).also {
                    view.startAnimation(it)
                }
                */
            }else{
                ObjectAnimator.ofFloat(view, "rotation", 180f, 0f).apply {
                    duration = 150
                    start()
                }
            }

            mHost.groupChangedAt(adapterPosition)
        }
        itemView.setOnClickListener {
            //mHost.toggleGroupExpanded(this)
            //mHost.groupChangedAt(adapterPosition)
        }
    }

    open fun unbind(data: IntervalData){
        itemView.clockGroupDeleteButton.setOnClickListener(null)
        itemView.clockGroupEditButton.setOnClickListener(null)
        itemView.clockGroupMoreButton.setOnClickListener(null)
        itemView.setOnClickListener(null)
        mData = null
    }
}

open class IntervalClockViewHolder(v: View, host: IntervalRecyclerViewHolderActionsI) : IntervalViewHolder(v, host) {
    fun bind(intervalData: IntervalData, groupFromInterval: IntervalData) {
        mData = intervalData
        val clockView = itemView.findViewById<IntervalClockView>(R.id.intervalClockView)
        val editButton = itemView.findViewById<AppCompatImageButton>(R.id.clockSingleEditButton)
        val deleteButton = itemView.findViewById<AppCompatImageButton>(R.id.clockSingleDeleteButton)
        val checkBox = itemView.findViewById<CheckBox>(R.id.clockEditCheckbox)
        itemView.findViewById<TextView>(R.id.clockLabelTxt).text = intervalData.label
        IntervalControllerFacade.instance.connectClockView(clockView, groupFromInterval.groupPosition.toInt(), intervalData)
        if(mHost.isInEditMode()){
            checkBox.visibility = View.VISIBLE
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
        }else{
            checkBox.visibility = View.INVISIBLE
            editButton.visibility = View.INVISIBLE
            deleteButton.visibility = View.INVISIBLE
        }
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            mHost.setItemChecked(adapterPosition, isChecked)
        }
        checkBox.isChecked = mHost.itemChecked(this)

        editButton.setOnClickListener {
            mHost.launchAddInEditMode(intervalData)
        }
        deleteButton.setOnClickListener {
            mHost.deleteChild(intervalData)
        }
        if(mHost.shouldShow(this)){
            itemView.visibility = View.VISIBLE
            itemView.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        }else{
            itemView.layoutParams.height = 0
            itemView.visibility = View.GONE
        }
    }

    override fun unbind(data: IntervalData) {
        itemView.clockSingleEditButton.setOnClickListener(null)
        itemView.clockSingleDeleteButton.setOnClickListener(null)
        itemView.clockEditCheckbox.setOnCheckedChangeListener(null)
        mData = null
    }
}
