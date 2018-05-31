package io.github.crr0004.intervalme

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.views.IntervalClockView


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

    init {
        mdb = IntervalMeDatabase.getInstance(mHostActivity.applicationContext)
        mIntervalDao = mdb!!.intervalDataDao()
    }

    override fun getAdapter(): ExpandableListAdapter {
        return this
    }

    override fun swapItems(a: IntervalData, b: IntervalData) {
        val aPos = a.groupPosition
        val bPos = b.groupPosition

        b.groupPosition = aPos
        a.groupPosition = bPos

        mCachedControllers[b.id]!!.setNextInterval(mCachedControllers[a.id])

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


       // toReturn = convertView

        //if (toReturn == null) {
            val infalInflater = mHostActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            toReturn = infalInflater.inflate(R.layout.interval_group, null)
        //}

        val intervalData = getGroup(groupPosition)
        toReturn!!.findViewById<TextView>(R.id.textView).text = intervalData.label ?: "Interval not found"
        toReturn.setTag(R.id.id_interval_view_interval, intervalData)

        //toReturn.setOnLongClickListener(intervalLongClickListener)
        this.mHost.setOnItemLongClickListener { parentAdapter, _, position, _ ->
            val intervalDataParent = parentAdapter.getItemAtPosition(position) as IntervalData
            val groupUUID = intervalDataParent.group.toString()
            val clipboard = mHostActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("group uuid", groupUUID)
            Toast.makeText(mHostActivity, "Copied UUID", Toast.LENGTH_SHORT).show()


            clipboard.primaryClip = clipData

            true
        }

        val groupChildren = mIntervalDao!!.getAllOfGroupWithoutOwner(intervalData.group).reversed()
        groupChildren.forEachIndexed{ index, childInterval ->
            var childAboveController: IntervalController? = null
            if(index > 0) {
                val childAbove = groupChildren[index-1]
                childAboveController =  mCachedControllers[childAbove.id]
            }
            val childController = if(mCachedControllers[childInterval.id] == null) {
                IntervalController(null, childInterval, childAboveController)
            }else{
                mCachedControllers[childInterval.id]
            }
            mCachedControllers[childInterval.id] = childController!!
        }

        return toReturn
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
        toReturn.findViewById<TextView>(R.id.clockLabelTxt)?.text = childOfInterval.label

        // Controller hasn't been forward cached so create it
        if(controller == null) {
            controller = IntervalController(clockView, childOfInterval)
        }else {
            // We need to tell the other mController to disconnect from the clock
            clockView.mController?.disconnectFromViews()
            controller.disconnectFromViews()
            controller.connectNewClockView(clockView)
            clockView.mController = controller
        }

        if(childPosition > 0)
            mCachedControllers[previousInterval!!.id]!!.setNextInterval(controller)

        //mCachedViews[childOfInterval.id] = toReturn
        mCachedControllers[childOfInterval.id] = controller


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
}