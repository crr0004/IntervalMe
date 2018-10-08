package io.github.crr0004.intervalme.interval

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.util.Log
import android.util.SparseBooleanArray
import android.view.DragEvent
import android.view.DragEvent.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import android.widget.ExpandableListView.getPackedPositionForChild
import io.github.crr0004.intervalme.BuildConfig
import io.github.crr0004.intervalme.DragDropAnimationController
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDAO
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.views.IntervalClockView
import java.util.*


/**
 * Created by crr00 on 24-Apr-18.
 */
class IntervalListAdapter
        constructor(private val mHostActivity: IntervalListActivity, private val mHost: ExpandableListView):
        BaseExpandableListAdapter(), DragDropAnimationController.DragDropViewSource<IntervalData>, IntervalControllerFacade.IntervalControllerDataSourceI {
    // BEGIN IntervalControllerDataSourceI implementation
    override fun facadeGetIDFromPosition(groupPosition: Int): UUID {
        return getGroup(groupPosition).group
    }

    override fun facadeGetGroup(groupPosition: Int): IntervalData {
        return getGroup(groupPosition)
    }

    override fun facadeGetGroupSize(groupPosition: Int): Int {
        return getChildrenCount(groupPosition)
    }

    override fun facadeGetChild(groupPosition: Int, index: Int): IntervalData {
        return getChild(groupPosition, index)
    }

    override fun getGroupProperties(groupPosition: Long): IntervalRunProperties? {
        return mIntervalProperties[groupPosition]
    }

    // END IntervalControllerDataSourceI implementation

    private var mdb: IntervalMeDatabase? = null
    private var mIntervalDao: IntervalDataDAO? = null
   // val mCachedControllers: HashMap<Long, IntervalController> = HashMap()
    public val mChecked = SparseBooleanArray()
    public var mInEditMode: Boolean = false
    private var mIntervalsList: HashMap<Long, Array<IntervalData>>? = HashMap(10)
    private var mIntervalProperties: HashMap<Long, IntervalRunProperties> = HashMap(1)
    private val mNotFoundGroupLabel: String
    private var mGroupSize: Int = 0
    var groupSize: Int
        get() {return mGroupSize}
        set(value) {mGroupSize = value}

    init {
        mdb = IntervalMeDatabase.getInstance(mHostActivity.applicationContext)
        mIntervalDao = mdb!!.intervalDataDao()
        mNotFoundGroupLabel = mHostActivity.getString(R.string.group_not_found_label).toString()
        IntervalControllerFacade.instance.setDataSource(this)
    }

    override fun getAdapter(): ExpandableListAdapter {
        return this
    }

    override fun swapItems(item1: IntervalData, item2: IntervalData) {
        val aPos = item1.groupPosition
        var bPos = item2.groupPosition
        val aGroup = item1.group
        val bGroup = item2.group

        // This is unlikely to happen but ensures one follows the other
        if(aPos == bPos){
            bPos++
        }

        item2.groupPosition = aPos
        item1.groupPosition = bPos

        item2.group = aGroup
        item1.group = bGroup

        if(aGroup == bGroup) {
            IntervalControllerFacade.instance.intervalsSwapped(item1.id, item2.id)
            //mCachedControllers[item2.id]!!.setNextInterval(mCachedControllers[item1.id])
        }

        mHostActivity.update(item1)
        mHostActivity.update(item2)

        this.notifyDataSetChanged()
    }
    private val intervalOnDragListener = { v: View, event: DragEvent ->
        val eventType = event.action
        val intervalBeingDragged = event.localState as IntervalData
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
                val set = AnimatorSet()
                set.play(ObjectAnimator.ofFloat(v, View.SCALE_Y,
                        0.8f, 1f))
                set.duration = v.resources.getInteger(
                        android.R.integer.config_mediumAnimTime).toLong()
                set.interpolator = DecelerateInterpolator()
                set.start()

                val packedPos = mHost.getExpandableListPosition(mHost.getPositionForView(v))
                val intervalDroppedOn = if(v.id == R.id.interval_group){
                    val groupPos = ExpandableListView.getPackedPositionGroup(packedPos)
                    getGroup(groupPos)
                }else{
                    val childPos = ExpandableListView.getPackedPositionChild(packedPos)
                    val groupPos = ExpandableListView.getPackedPositionGroup(packedPos)
                    getChild(groupPos, childPos)
                }

                /**
                 * If the interval being dropped on is a group then we need
                 * to change the behaviour if the interval being dragged is itself a group
                 */
                if(intervalDroppedOn.ownerOfGroup){
                    val groupUUID = intervalDroppedOn.group
                    if(intervalBeingDragged.group != groupUUID && !intervalBeingDragged.ownerOfGroup) {
                        mHostActivity.moveIntervalToGroup(intervalBeingDragged, groupUUID)
                        mHost.expandGroup(intervalDroppedOn.groupPosition.toInt())
                    }else if(intervalBeingDragged.ownerOfGroup && intervalDroppedOn.ownerOfGroup){
                        // We've dropped one group on top of another
                        mIntervalsList?.clear()
                        mHostActivity.moveIntervalGroupAboveGroup(intervalBeingDragged, intervalDroppedOn)
                    }
                    // We don't want to be able to move a group onto a child
                }else if(!intervalBeingDragged.ownerOfGroup){

                    mHostActivity.moveChildIntervalAboveChild(intervalBeingDragged, intervalDroppedOn)
                }else{
                    if(BuildConfig.DEBUG){
                        Toast.makeText(mHostActivity, "Undefined behaviour", Toast.LENGTH_SHORT).show()
                    }
                }
                // this.notifyDataSetChanged()
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
                v.invalidate()
            }
            else -> {

            }

        }
        true
    }

    fun setProperty(intervalId: Long, property: IntervalRunProperties){
        mIntervalProperties[intervalId] = property
    }

    override fun getGroup(groupPosition: Int): IntervalData {
        var group = mIntervalsList!![groupPosition.toLong()]?.get(0)
        if(group == null){
            //group = mIntervalsList!!.value.toList()[0][0]
            group = IntervalData.generate(1)[0]!!
            group.label = ""
            //group.groupPosition = groupPosition.toLong()
            //group.label = group.label + " " + group.groupPosition
            if(BuildConfig.DEBUG) {
                val errorInfo = StringBuilder(65)
                errorInfo
                        .append("getGroup hit a null at ")
                        .append(groupPosition)
                        .append(" list size is: ")
                        .append(mIntervalsList?.size)
                        .append(" value at pos is: ")
                        .append(mIntervalsList!![groupPosition.toLong()])
                errorInfo.trimToSize()
                Log.d("ILA", errorInfo.toString())
            }
        }
        return group
    }

    fun getProperties(id: Long): IntervalRunProperties?{
        return mIntervalProperties[id]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }


    override fun hasStableIds(): Boolean {
        //The ids of the data will be not be consistent across changes
        return true
    }

    fun clear(){
        mIntervalsList?.clear()
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
        toReturn.findViewById<TextView>(R.id.intervalGroupPos).text = intervalData.groupPosition.toString()
        toReturn.setTag(R.id.id_interval_view_interval, intervalData)
        val editButton = toReturn.findViewById<AppCompatImageButton>(R.id.clockGroupEditButton)
        val deleteButton = toReturn.findViewById<AppCompatImageButton>(R.id.clockGroupDeleteButton)
        val properties = getProperties(intervalData.id)
        if(properties != null){
            toReturn.findViewById<TextView>(R.id.intervalGroupLoops).text = properties.loops.toString()
        }else{
            toReturn.findViewById<TextView>(R.id.intervalGroupLoopsLbl).visibility = View.GONE
            toReturn.findViewById<TextView>(R.id.intervalGroupLoops).visibility = View.GONE
        }

        if(mInEditMode){
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
        }else{
            editButton.visibility = View.INVISIBLE
            deleteButton.visibility = View.INVISIBLE
        }

        editButton.setOnClickListener {
            mHostActivity.launchAddInEditMode(intervalData)
        }
        deleteButton.setOnClickListener{
            mHostActivity.deleteGroupMoveChildrenToETC(intervalData)
            // We need to remove the last group because this is going to shuffle them all up
            mIntervalsList!!.remove(mIntervalsList!!.size.toLong()-1)
            //if(mIntervalsList!!.size == 0){
                notifyDataSetChanged()
            //}
        }
        toReturn.setOnDragListener(intervalOnDragListener)


        IntervalControllerFacade.instance.groupView(groupPosition, toReturn)
        /*
        val group = mIntervalsList?.get(groupPosition.toLong())
        if(group != null) {
            val groupChildren = group.drop(1).reversed()
            groupChildren.forEachIndexed { index, childInterval ->
                var childAboveController: IntervalController? = null
                if (index > 0) {
                    val childAbove = groupChildren[index - 1]
                    childAboveController = mCachedControllers[childAbove.id]
                }
                val childController = if (mCachedControllers[childInterval.id] == null) {
                    IntervalController(null, childInterval, childAboveController, applicationContext = this.mHostActivity.applicationContext, runProperties = getProperties(childInterval.id))
                } else {
                    mCachedControllers[childInterval.id]
                }
                mCachedControllers[childInterval.id] = childController!!
            }
        }
        */

        return toReturn
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        var size = mIntervalsList?.get(groupPosition.toLong())?.size ?: 0
        // Remove one from size because the first element is the group
        if(size > 0)
            size--
        return size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): IntervalData {
        return mIntervalsList!![groupPosition.toLong()]?.get(childPosition+1)!!
    }

    override fun getGroupId(groupPosition: Int): Long {
        return getGroup(groupPosition).id
    }

    /**
     * @see DataSetObservable.notifyChanged
     */
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        //Log.d("ILA", "notifyDataSetChanged Called")
    }

    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var toReturn: View? = null
        val childOfInterval = getChild(groupPosition, childPosition)
        var previousInterval: IntervalData? = null

        if(childPosition > 0) {
            previousInterval = getChild(groupPosition, childPosition - 1)
        }

        //toReturn = mCachedViews[childOfInterval.id]

        //Top null check for cached view
        if(convertView?.id == R.layout.interval_single_clock)
            toReturn = convertView
        // Look for our mController that may have been forward init
        //var controller: IntervalGroupController? = mGroupControlls[groupPosition.toLong()]!!
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
        val properties = getProperties(childOfInterval.id)
        if(properties != null){
            toReturn.findViewById<TextView>(R.id.clockLoopsTxt).text = properties .loops.toString()
        }else{
            toReturn.findViewById<TextView>(R.id.clockLoopsTxt).visibility = View.GONE
            toReturn.findViewById<TextView>(R.id.clockLabelLoops).visibility = View.GONE
        }

        toReturn.findViewById<TextView>(R.id.clockLabelTxt)?.text = childOfInterval.label
        //toReturn.findViewById<TextView>(R.id.clockLabelPos)?.text = childOfInterval.groupPosition.toString()

        if(mInEditMode){
            toReturn.findViewById<View>(R.id.clockEditCheckbox).visibility = View.VISIBLE
            toReturn.findViewById<View>(R.id.clockSingleEditButton).visibility = View.VISIBLE
            toReturn.findViewById<View>(R.id.clockSingleDeleteButton).visibility = View.VISIBLE
        }else{
            toReturn.findViewById<View>(R.id.clockEditCheckbox).visibility = View.INVISIBLE
            toReturn.findViewById<View>(R.id.clockSingleEditButton).visibility = View.INVISIBLE
            toReturn.findViewById<View>(R.id.clockSingleDeleteButton).visibility = View.INVISIBLE
        }


        toReturn.setOnDragListener(intervalOnDragListener)

        IntervalControllerFacade.instance.connectClockView(clockView, groupPosition, childOfInterval)
        // Controller hasn't been forward cached so create it
        //if(controller == null) {
            //controller = IntervalController(clockView, childOfInterval, applicationContext = this.mHostActivity.applicationContext, runProperties = properties)
        //}else {
            // We need to tell the other mController to disconnect from the clock
            //clockView.mController?.disconnectFromViews()
            //controller?.disconnectFromViews(childPosition)
            //controller?.connectNewClockView(clockView, childPosition)
            //clockView.mController = controller
        //}
        // Ensure controller is up to date
        //controller.mChildOfInterval = childOfInterval

        //if(childPosition > 0)
            //mCachedControllers[previousInterval!!.id]!!.setNextInterval(controller)

        //mCachedControllers[childOfInterval.id] = controller
        val flatListPosition = mHost.getFlatListPosition(getPackedPositionForChild(groupPosition, childPosition))
        checkBox.setOnClickListener {
            setItemChecked(flatListPosition, checkBox.isChecked)
            //mChecked.put(flatListPosition, checkBox.isChecked)
            //Log.d("ILA", "Checked: " + checkBox.isChecked)
            //checkBox.toggle()
        }
        checkBox.isChecked = mChecked.get(flatListPosition, false)

        editButton.setOnClickListener {
            mHostActivity.launchAddInEditMode(childOfInterval)
        }
        deleteButton.setOnClickListener {
            mHostActivity.deleteChild(childOfInterval)
            notifyDataSetChanged()
        }
        // Ensures when we move items around, the next intervals are getting updated
        if(isLastChild){
            //controller.setNextInterval(null)
            IntervalControllerFacade.instance.setIntervalAsLast(groupPosition, childOfInterval)
        }

        return toReturn
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        val childOfInterval = getChild(groupPosition,childPosition)
        return childOfInterval.id
    }

    override fun getGroupCount(): Int {
        val size = mIntervalsList?.size ?: 0
        return size
    }

    fun startAllIntervals() {
        IntervalControllerFacade.instance.startAllIntervals()
        //mCachedControllers.forEach { _, controller ->
            //controller.startClockAsNew()
        //}
    }

    fun setItemChecked(keyAt: Int, b: Boolean) {
        if(b) {
            mChecked.put(keyAt, b)
        }else{
            mChecked.delete(keyAt)
        }
    }

    //private var mGroupControlls: HashMap<Long, IntervalGroupController?> = HashMap(1)

    fun setGroup(groupPosition: Long, it: Array<IntervalData>) {
        mIntervalsList!![groupPosition] = it
        IntervalControllerFacade.instance.setUpGroupOrder(it[0].groupPosition.toInt(), mHostActivity)
    }
}