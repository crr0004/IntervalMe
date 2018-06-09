package io.github.crr0004.intervalme

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.util.Log
import android.util.SparseBooleanArray
import android.view.DragEvent.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import android.widget.ExpandableListView.getPackedPositionForChild
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.views.IntervalClockView
import kotlinx.android.synthetic.main.interval_single_clock.view.*
import java.util.*


/**
 * Created by crr00 on 24-Apr-18.
 */
class IntervalListAdapter
        constructor(private val mHostActivity: IntervalListActivity, private val mHost: ExpandableListView):
        BaseExpandableListAdapter(), DragDropAnimationController.DragDropViewSource<IntervalData> {

    private var mdb: IntervalMeDatabase? = null
    private var mIntervalDao: IntervalDataDOA? = null
    private val mCachedViews: HashMap<Long, View> = HashMap()
    val mCachedControllers: HashMap<Long, IntervalController> = HashMap()
    public val mChecked = SparseBooleanArray()
    public var mInEditMode: Boolean = false

    init {
        mdb = IntervalMeDatabase.getInstance(mHostActivity.applicationContext)
        mIntervalDao = mdb!!.intervalDataDao()
    }

    override fun getAdapter(): ExpandableListAdapter {
        return this
    }

    override fun swapItems(a: IntervalData, b: IntervalData) {
        val aPos = a.groupPosition
        var bPos = b.groupPosition
        val aGroup = a.group
        val bGroup = b.group

        if(aPos == bPos){
            bPos++
        }

        b.groupPosition = aPos
        a.groupPosition = bPos

        b.group = aGroup
        a.group = bGroup

        if(aGroup == bGroup) {
            mCachedControllers[b.id]!!.setNextInterval(mCachedControllers[a.id])
        }

        mIntervalDao!!.update(a)
        mIntervalDao!!.update(b)

        this.notifyDataSetChanged()
    }

    override fun getGroup(groupPosition: Int): IntervalData {
        val intervalDataParent = mIntervalDao?.getGroupByOffset((groupPosition+1).toLong())
        return intervalDataParent!!
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        //TODO("implement this properly") //To change body of created functions use File | Settings | File Templates.
        return true
    }


    override fun hasStableIds(): Boolean {
        //The ids of the data will be not be consistent across changes
        return true
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var toReturn: View? = null


        //Top null check for cached view
        if(convertView?.id == R.layout.interval_group)
            toReturn = convertView

        if (toReturn == null) {
            val infalInflater = mHostActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            toReturn = infalInflater.inflate(R.layout.interval_group, parent, false)
        }

        val intervalData = getGroup(groupPosition)
        toReturn!!.findViewById<TextView>(R.id.intervalGroupNameTxt).text = intervalData.label ?: "Interval not found"
        toReturn.setTag(R.id.id_interval_view_interval, intervalData)
        val editButton = toReturn.findViewById<AppCompatImageButton>(R.id.clockGroupEditButton)

        if(mInEditMode){
            editButton.visibility = View.VISIBLE
        }else{
            editButton.visibility = View.INVISIBLE
        }

        editButton.setOnClickListener {
            mHostActivity.launchAddInEditMode(intervalData)
        }

        toReturn.setOnDragListener { v, event ->
            val eventType = event.action
            val interval: IntervalData = event.localState as IntervalData

            when(eventType){
                ACTION_DRAG_ENTERED -> {
                    true
                }
                ACTION_DROP -> {
                    val groupUUID = intervalData.group
                    if(interval.group != groupUUID) {
                        moveIntervalToGroup(interval, groupUUID)
                        mHost.expandGroup(groupPosition)
                    }
                    true
                }else -> {

                true
                }
            }
        }

        //toReturn.setOnLongClickListener(intervalLongClickListener)

        val groupChildren = mIntervalDao!!.getAllOfGroupWithoutOwner(intervalData.group).reversed()
        groupChildren.forEachIndexed{ index, childInterval ->
            var childAboveController: IntervalController? = null
            if(index > 0) {
                val childAbove = groupChildren[index-1]
                childAboveController =  mCachedControllers[childAbove.id]
            }
            val childController = if(mCachedControllers[childInterval.id] == null) {
                IntervalController(null, childInterval, childAboveController, applicationContext = this.mHostActivity.applicationContext)
            }else{
                mCachedControllers[childInterval.id]
            }
            mCachedControllers[childInterval.id] = childController!!
        }

        return toReturn
    }

    private fun moveIntervalToGroup(interval: IntervalData, groupUUID: UUID) {
        mIntervalDao!!.shuffleChildrenInGroupUpFrom(interval.groupPosition, interval.group)
        interval.group = groupUUID
        interval.groupPosition = mIntervalDao!!.getChildSizeOfGroup(groupUUID) + 1
        mIntervalDao!!.update(interval)
        notifyDataSetChanged()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val intervalDataParent = getGroup(groupPosition)
        return mIntervalDao!!.getAllOfGroupWithoutOwner(intervalDataParent.group).size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val intervalDataParent = getGroup(groupPosition)
        return mIntervalDao!!.getChildOfGroupByOffset(
                (childPosition+ 1).toLong(),
                intervalDataParent.group)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return getGroup(groupPosition).id
    }

    /**
     * @see DataSetObservable.notifyChanged
     */
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()

    }



    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var toReturn: View? = null
        val childOfInterval = getChild(groupPosition, childPosition) as IntervalData
        var previousInterval: IntervalData? = null

        if(childPosition > 0) {
            previousInterval = getChild(groupPosition, childPosition - 1) as IntervalData
        }

        //toReturn = mCachedViews[childOfInterval.id]

        //Top null check for cached view
        if(convertView?.id == R.layout.interval_single_clock)
            toReturn = convertView
        // Look for our mController that may have been forward init
        var controller: IntervalController? = mCachedControllers[childOfInterval.id]
        // If we're using an existing mController we must make sure to release properly before re-init

        // second null check for using a converted view
        if (toReturn == null) {
            val inflater = mHostActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            //Passing null as root until I figure it out. Passing parent causes a crash
            toReturn = inflater.inflate(R.layout.interval_single_clock, null)
        }

        val clockView = toReturn!!.findViewById<IntervalClockView>(R.id.intervalClockView)
        val editButton = toReturn.findViewById<AppCompatImageButton>(R.id.clockSingleEditButton)
        val deleteButton = toReturn.findViewById<AppCompatImageButton>(R.id.clockSingleDeleteButton)
        val checkBox = toReturn.findViewById<CheckBox>(R.id.clockEditCheckbox)

        toReturn.findViewById<TextView>(R.id.clockLabelTxt)?.text = childOfInterval.label
        toReturn.findViewById<TextView>(R.id.clockLabelPos)?.text = childOfInterval.groupPosition.toString()

        if(mInEditMode){
            toReturn.findViewById<View>(R.id.clockEditCheckbox).visibility = View.VISIBLE
            toReturn.findViewById<View>(R.id.clockSingleEditButton).visibility = View.VISIBLE
            toReturn.findViewById<View>(R.id.clockSingleDeleteButton).visibility = View.VISIBLE
        }else{
            toReturn.findViewById<View>(R.id.clockEditCheckbox).visibility = View.INVISIBLE
            toReturn.findViewById<View>(R.id.clockSingleEditButton).visibility = View.INVISIBLE
            toReturn.findViewById<View>(R.id.clockSingleDeleteButton).visibility = View.INVISIBLE
        }

        toReturn.setOnLongClickListener {
            //mInEditMode = !mInEditMode
            //this.notifyDataSetChanged()
            toReturn.startDrag(ClipData.newPlainText("",""), View.DragShadowBuilder(toReturn), childOfInterval, View.DRAG_FLAG_GLOBAL)
            true
        }

        toReturn.setOnDragListener { v, event ->
            val eventType = event.action
            val interval = event.localState as IntervalData
            v.pivotX = 0f
            v.pivotY = v.height.toFloat()

            when(eventType){
                ACTION_DRAG_ENTERED -> {
                    val set = AnimatorSet()
                    set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                                    1f, 0.8f))
                    set.duration = v.resources.getInteger(
                            android.R.integer.config_mediumAnimTime).toLong()
                    set.interpolator = DecelerateInterpolator()
                    set.start()

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate()

                }
                ACTION_DRAG_EXITED -> {
                    val set = AnimatorSet()
                    set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                            0.8f, 1f))
                    set.duration = v.resources.getInteger(
                            android.R.integer.config_mediumAnimTime).toLong()
                    set.interpolator = DecelerateInterpolator()
                    set.start()
                }
                ACTION_DROP -> {
                    /**
                     * First we remove interval from it's current group
                     * by shuffling everything up from it's current groupPosition
                     *
                     * Then we want to drop it into a new position.
                     * So we need to move everything bellow the new position down
                     */
                    val set = AnimatorSet()
                    set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                            0.8f, 1f))
                    set.duration = v.resources.getInteger(
                            android.R.integer.config_mediumAnimTime).toLong()
                    set.interpolator = DecelerateInterpolator()
                    set.start()
                    val intervalData: IntervalData = v.intervalClockView.mController!!.mChildOfInterval

                    mIntervalDao!!.shuffleChildrenInGroupUpFrom(interval.groupPosition, interval.group)

                    // Make room in the group for the incoming interval
                    // -1 from groupPosition so it gets moved as well
                    mIntervalDao!!.shuffleChildrenDownFrom(intervalData.groupPosition-1, intervalData.group)

                    interval.group = intervalData.group
                    // Put the interval into the spot above the dropped onto item
                    interval.groupPosition = intervalData.groupPosition
                    mIntervalDao!!.update(interval)
                    this.notifyDataSetChanged()
                    //swapItems(interval, intervalData)
                }
                ACTION_DRAG_ENDED -> {
                    if(v.scaleY < 1f){
                        val set = AnimatorSet()
                        set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                                0.8f, 1f))
                        set.duration = v.resources.getInteger(
                                android.R.integer.config_mediumAnimTime).toLong()
                        set.interpolator = DecelerateInterpolator()
                        set.start()
                    }
                }
                else -> {

                }

            }
            true
        }

        // Controller hasn't been forward cached so create it
        if(controller == null) {
            controller = IntervalController(clockView, childOfInterval, applicationContext = this.mHostActivity.applicationContext)
        }else {
            // We need to tell the other mController to disconnect from the clock
            clockView.mController?.disconnectFromViews()
            controller.disconnectFromViews()
            controller.connectNewClockView(clockView)
            clockView.mController = controller
        }
        // Ensure controller is up to date
        controller.mChildOfInterval = childOfInterval

        if(childPosition > 0)
            mCachedControllers[previousInterval!!.id]!!.setNextInterval(controller)

        //mCachedViews[childOfInterval.id] = toReturn
        mCachedControllers[childOfInterval.id] = controller

        checkBox.setOnClickListener {
            val flatListPosition = mHost.getFlatListPosition(getPackedPositionForChild(groupPosition, childPosition))
            setItemChecked(flatListPosition, checkBox.isChecked)
            //mChecked.put(flatListPosition, checkBox.isChecked)
            Log.d("ILA", "Checked: " + checkBox.isChecked)
            //checkBox.toggle()
        }

        editButton.setOnClickListener {
            mHostActivity.launchAddInEditMode(childOfInterval)
        }
        deleteButton.setOnClickListener {
            mCachedControllers.remove(childOfInterval.id)
            mIntervalDao!!.delete(childOfInterval)
            notifyDataSetChanged()
        }
        // Ensures when we move items around, the next intervals are getting updated
        if(isLastChild){
            controller.setNextInterval(null)
        }

        return toReturn
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        val childOfInterval = getChild(groupPosition,childPosition) as IntervalData
        return childOfInterval.id
    }

    override fun getGroupCount(): Int {
        return mIntervalDao!!.getGroupOwners().size
    }

    fun updateInterval(id: Long) {
        val updatedInterval = mIntervalDao?.get(id)
        if(updatedInterval != null) {
            mCachedControllers[id]?.refreshInterval(updatedInterval)
            mCachedControllers[id]?.stopAndRefreshClock()
            mCachedViews[id]?.findViewById<TextView>(R.id.clockLabelTxt)?.text = updatedInterval.label
        }
    }

    fun startAllIntervals() {
        mCachedControllers.forEach { id, controller ->
            controller.startClockAsNew()
        }
    }

    fun setItemChecked(keyAt: Int, b: Boolean) {
        if(b) {
            mChecked.put(keyAt, b)
        }else{
            mChecked.delete(keyAt)
        }
    }
}