package io.github.crr0004.intervalme.interval

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseBooleanArray
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.CheckBox
import android.widget.TextView
import io.github.crr0004.intervalme.BuildConfig
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
    private val intervalViewHolderOnDragListener = View.OnDragListener { v, event ->
        val eventType = event.action
        val intervalBeingDragged = event.localState as IntervalData
        Log.d("IRA", "Event type: $eventType")
        v.pivotX = 0f
        v.pivotY = v.height.toFloat()

        when (eventType) {
            DragEvent.ACTION_DRAG_STARTED -> {
                true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                val set = AnimatorSet()
                set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                        1f, 0.8f))
                set.duration = v.resources.getInteger(
                        android.R.integer.config_mediumAnimTime).toLong()
                set.interpolator = DecelerateInterpolator()
                set.start()

                // Invalidate the view to force a redraw in the new tint
                v.invalidate()
                true

            }
            DragEvent.ACTION_DRAG_EXITED -> {
                val set = AnimatorSet()
                set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                        0.8f, 1f))
                set.duration = v.resources.getInteger(
                        android.R.integer.config_mediumAnimTime).toLong()
                set.interpolator = DecelerateInterpolator()
                set.start()
                true
            }
            DragEvent.ACTION_DROP -> {
                val set = AnimatorSet()
                set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                        0.8f, 1f))
                set.duration = v.resources.getInteger(
                        android.R.integer.config_mediumAnimTime).toLong()
                set.interpolator = DecelerateInterpolator()
                set.start()
                val adapterPosition = mRecyclerView!!.getChildAdapterPosition(v)
                val intervalDroppedOn = mGroups!![adapterPosition]!!
                if(intervalDroppedOn.ownerOfGroup){
                    mHost.moveIntervalToGroup(intervalBeingDragged, intervalDroppedOn.group)
                }else{
                    mHost.moveChildIntervalAboveChild(intervalBeingDragged, intervalDroppedOn)
                }
                /*
            val packedPos = mHost.getExpandableListPosition(mHost.getPositionForView(v))
            val intervalDroppedOn = if (v.id == R.id.interval_group) {
                val groupPos = ExpandableListView.getPackedPositionGroup(packedPos)
                getGroup(groupPos)
            } else {
                val childPos = ExpandableListView.getPackedPositionChild(packedPos)
                val groupPos = ExpandableListView.getPackedPositionGroup(packedPos)
                getChild(groupPos, childPos)
            }


            /**
             * If the interval being dropped on is a group then we need
             * to change the behaviour if the interval being dragged is itself a group
             */
            if (intervalDroppedOn.ownerOfGroup) {
                val groupUUID = intervalDroppedOn.group
                if (intervalBeingDragged.group != groupUUID && !intervalBeingDragged.ownerOfGroup) {
                    mHostActivity.moveIntervalToGroup(intervalBeingDragged, groupUUID)
                    mHost.expandGroup(intervalDroppedOn.groupPosition.toInt())
                } else if (intervalBeingDragged.ownerOfGroup && intervalDroppedOn.ownerOfGroup) {
                    // We've dropped one group on top of another
                    mIntervalsList?.clear()
                    mHostActivity.moveIntervalGroupAboveGroup(intervalBeingDragged, intervalDroppedOn)
                }

                // We don't want to be able to move a group onto a child
            } else if (!intervalBeingDragged.ownerOfGroup) {

                mHostActivity.moveChildIntervalAboveChild(intervalBeingDragged, intervalDroppedOn)
            } else {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(mHostActivity, "Undefined behaviour", Toast.LENGTH_SHORT).show()
                }
            }
            */
                // this.notifyDataSetChanged()
                //swapItems(interval, intervalData)
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                if (v.scaleY < 1f) {
                    val set = AnimatorSet()
                    set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                            0.8f, 1f))
                    set.duration = v.resources.getInteger(
                            android.R.integer.config_mediumAnimTime).toLong()
                    set.interpolator = DecelerateInterpolator()
                    set.start()
                }
                v.invalidate()
                true
            }
            else -> {
                false
            }
        }
    }
    var mGroups: ArrayList<IntervalData>? = null
    set(value) {
        field = value
        IntervalControllerFacade.instance.reset()
        notifyDataSetChanged()
    }


    override fun onFailedToRecycleView(holder: IntervalViewHolder): Boolean {
        Log.e("IRA", "Failed to recycle " + holder.data)
        return super.onFailedToRecycleView(holder)
    }

    override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): IntervalViewHolder {
        Log.i("IRA", "Creating view holder for view type $viewType")
        return if(viewType == 0){
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

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
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

    override fun getGroupFromInterval(intervalData: IntervalData, adapterPosition: Int): IntervalData {
        return mGroups!![(adapterPosition-intervalData.groupPosition).toInt()]
    }

    override fun onBindViewHolder(p0: IntervalViewHolder, pos: Int) {
        val data = mGroups!![pos]
        p0.bind(data)
    }

    fun setProperty(intervalId: Long, intervalRunProperties: IntervalRunProperties) {
        mIntervalProperties[intervalId] = intervalRunProperties
    }

    fun isGroupExpanded(i: Int): Boolean {
        return mExpandedGroups[i, false]
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
        val group = getGroup(groupPosition)!!.group
        var child = mGroups!!.find {
            !it.ownerOfGroup && it.group == group && it.groupPosition.toInt() == index
        }
        if(child == null && BuildConfig.DEBUG){
            Log.d("IRA", "A facade has tried to get a child in group $group at pos $index but it wasn't found")
            child = mGroups!![1]
        }
        return child!!
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

    fun getGroupPos(group: UUID): Int {
        var i = -1
        mGroups!!.filterIndexed { index, it ->
            i = index
           it.ownerOfGroup && it.group == group
        }
        return i
    }

    override fun groupChildrenChangedAt(adapterPosition: Int){
        if(getItemViewType(adapterPosition) == 0)
            notifyItemRangeChanged(adapterPosition+1, facadeGetGroupSize(mGroups!![adapterPosition].groupPosition.toInt()))
    }
    fun groupChangedAt(adapterPosition: Int){
        IntervalControllerFacade.instance.forceGroupRefresh(mGroups!![adapterPosition])
        if(getItemViewType(adapterPosition) == 0)
            notifyItemRangeChanged(adapterPosition, facadeGetGroupSize(mGroups!![adapterPosition].groupPosition.toInt()))
    }

    override fun groupExpanded(adapterPosition: Int): Boolean{
        return mExpandedGroups[adapterPosition, false]
    }

    override fun getItemId(position: Int): Long {
        return mGroups!![position].id
    }

    override fun getDragListener(): View.OnDragListener {
        return intervalViewHolderOnDragListener
    }

    override fun onViewRecycled(holder: IntervalViewHolder) {
        holder.unbind()
        super.onViewRecycled(holder)
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
    fun groupChildrenChangedAt(adapterPosition: Int)
    fun groupExpanded(adapterPosition: Int): Boolean
    fun getDragListener(): View.OnDragListener
    fun getGroupFromInterval(intervalData: IntervalData, adapterPosition: Int): IntervalData
}



open class IntervalViewHolder(v: View, val mHost: IntervalRecyclerViewHolderActionsI) : RecyclerView.ViewHolder(v) {
    protected var mData: IntervalData? = null
    val data: IntervalData?
    get() {return mData}
    @SuppressLint("InlinedApi")
    open fun bind(intervalData: IntervalData, expanded: Boolean = false){
        //Log.d("IRA", "Binding $adapterPosition")
        this.mData = intervalData
        if(!intervalData.ownerOfGroup)
            Log.e("IRA", "Binding a interval as a group when it is not a group owner $intervalData")
        itemView.intervalGroupNameTxt.text = intervalData.label
        itemView.intervalGroupPos.text = intervalData.groupPosition.toString()
        //itemView.setTag(R.id.id_interval_view_interval, intervalData)

        val editButton = itemView.findViewById<AppCompatImageButton>(R.id.clockGroupEditButton)
        val deleteButton = itemView.findViewById<AppCompatImageButton>(R.id.clockGroupDeleteButton)

        if(!IntervalControllerFacade.instance.isGroupSetUp(group = intervalData.group))
            IntervalControllerFacade.instance.setUpGroupOrder(intervalData.groupPosition.toInt(), itemView.context)
        val properties = mHost.getPropertiesFor(intervalData)
        if(properties == null){
            itemView.intervalGroupLoops.visibility = View.GONE
            itemView.intervalGroupLoopsLbl.visibility = View.GONE
        }

        itemView.intervalGroupLoops.text = (properties?.loops ?: "").toString()

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

        itemView.clockGroupMoreButton.setOnClickListener {view ->
            mHost.toggleGroupExpanded(this)
            toggleMoreButton(view)
            mHost.groupChildrenChangedAt(adapterPosition)
        }
        itemView.setOnClickListener {
            mHost.toggleGroupExpanded(this)
            toggleMoreButton(itemView.clockGroupMoreButton)
            mHost.groupChildrenChangedAt(adapterPosition)
        }
        itemView.setOnDragListener(mHost.getDragListener())
        itemView.setOnLongClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(null, View.DragShadowBuilder(it), intervalData, View.DRAG_FLAG_GLOBAL)
            } else {
                it.startDrag(null, View.DragShadowBuilder(it), intervalData, View.DRAG_FLAG_GLOBAL)
            }
        }
    }

    private fun toggleMoreButton(view: View){
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
    }

    open fun unbind(){
        itemView.clockGroupDeleteButton.setOnClickListener(null)
        itemView.clockGroupEditButton.setOnClickListener(null)
        itemView.clockGroupMoreButton.setOnClickListener(null)
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
        itemView.setOnDragListener(null)
        mData = null
    }
}

open class IntervalClockViewHolder(v: View, host: IntervalRecyclerViewHolderActionsI) : IntervalViewHolder(v, host) {
    override fun bind(intervalData: IntervalData, expanded: Boolean) {
        val groupFromInterval = mHost.getGroupFromInterval(intervalData, adapterPosition)
        mData = intervalData
        if(itemView.id == R.id.interval_single_clock)
            Log.e("IRA", "Binding a clock view holder with the wrong layout")
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
        itemView.setOnDragListener(mHost.getDragListener())
        itemView.setOnLongClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(null, View.DragShadowBuilder(it), intervalData, View.DRAG_FLAG_GLOBAL)
            } else {
                it.startDrag(null, View.DragShadowBuilder(it), intervalData, View.DRAG_FLAG_GLOBAL)
            }
        }
    }

    override fun unbind() {
        itemView.clockSingleEditButton.setOnClickListener(null)
        itemView.clockSingleDeleteButton.setOnClickListener(null)
        itemView.clockEditCheckbox.setOnCheckedChangeListener(null)
        itemView.setOnLongClickListener(null)
        itemView.setOnDragListener(null)
        mData = null
    }
}
