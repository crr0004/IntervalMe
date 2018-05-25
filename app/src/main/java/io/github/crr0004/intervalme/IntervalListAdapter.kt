package io.github.crr0004.intervalme

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v7.widget.AppCompatImageButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.views.IntervalClockView


/**
 * Created by crr00 on 24-Apr-18.
 */
class IntervalListAdapter constructor(private val mHostActivity: IntervalListActivity, private val mHost: ExpandableListView): BaseExpandableListAdapter() {

    private var mdb: IntervalMeDatabase? = null
    private var mIntervalDao: IntervalDataDOA? = null
    val mCachedViews: HashMap<Long, View> = HashMap()
    val mCachedControllers: HashMap<Long, IntervalController> = HashMap()

    init {
        mdb = IntervalMeDatabase.getInstance(mHostActivity.applicationContext)
        mIntervalDao = mdb!!.intervalDataDao()
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
        var toReturn: View?

        toReturn = mCachedViews[getGroupId(groupPosition)]
        if(toReturn == null) {

            toReturn = convertView

            if (toReturn == null) {
                val infalInflater = mHostActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                toReturn = infalInflater.inflate(R.layout.interval_group, null)
            }

            val intervalData = getGroup(groupPosition)
            toReturn!!.findViewById<TextView>(R.id.textView).text = intervalData.label ?: "Interval not found"
            toReturn.setTag(R.id.id_interval_view_interval, intervalData)

            //toReturn.setOnLongClickListener(intervalLongClickListener)
            this.mHost.setOnItemLongClickListener { _, _, position, _ ->
                val intervalDataParent = mIntervalDao?.getGroupByOffset(position.toLong() + 1)
                val groupUUID = intervalDataParent?.group.toString()
                val clipboard = mHostActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("group uuid", groupUUID)
                Toast.makeText(mHostActivity, "Copied UUID", Toast.LENGTH_SHORT).show()


                clipboard.primaryClip = clipData

                true
            }
            //toReturn.setOnClickListener { this.mHost.expandGroup(groupPosition)}
            mCachedViews[intervalData!!.id] = toReturn
        }
        return toReturn
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val intervalDataParent = getGroup(groupPosition) as IntervalData
        return mIntervalDao!!.getAllOfGroupWithoutOwner(intervalDataParent.group).size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val intervalDataParent = getGroup(groupPosition) as IntervalData
        return mIntervalDao!!.getChildOfGroupByOffset(
                (childPosition+ 1).toLong(),
                intervalDataParent.group)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return (getGroup(groupPosition) as IntervalData).id
    }



    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var toReturn: View?
        val childOfInterval = getChild(groupPosition, childPosition) as IntervalData
        var nextChildOfInterval: IntervalData? = null
        if(!isLastChild) {
            nextChildOfInterval = getChild(groupPosition, childPosition + 1) as IntervalData
        }

        toReturn = mCachedViews[childOfInterval.id]

        //Top null check for cached view
        if(toReturn == null) {
            toReturn = convertView
            // Look for our controller that may have been forward init
            var controller: IntervalController? = mCachedControllers[childOfInterval.id]


            // second null check for using a converted view
            if (toReturn == null) {
                val inflater = mHostActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                //Passing null as root until I figure it out. Passing parent causes a crash
                toReturn = inflater.inflate(R.layout.interval_single_clock, null)

            }

            val clockView = toReturn!!.findViewById<IntervalClockView>(R.id.intervalClockView)
            val editButton = toReturn.findViewById<AppCompatImageButton>(R.id.clockSingleEditButton)

            //Create our next controller with wrong values only if we have a nextChildInterval
            var nextController: IntervalController? = null
            if(nextChildOfInterval != null && !isLastChild) {
                nextController = IntervalController()
            }

            // Controller hasn't been forward cached so create it
            if(controller == null) {
                controller = IntervalController(clockView, childOfInterval, nextController)
            }else{
                // Controller got forward cached so values need to be updated to correct values
                controller.init(clockView,childOfInterval,nextController)
            }
            clockView.setController(controller)
            mCachedViews[childOfInterval.id] = toReturn
            mCachedControllers[childOfInterval.id] = controller

            editButton.setOnClickListener {
                mHostActivity.launchAddInEditMode(childOfInterval)
            }

            if(nextChildOfInterval != null && nextController != null)
                mCachedControllers[nextChildOfInterval.id] = nextController
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
            mCachedControllers[id]?.mChildOfInterval = updatedInterval
            mCachedControllers[id]?.stopAndRefreshClock()
        }
    }
}