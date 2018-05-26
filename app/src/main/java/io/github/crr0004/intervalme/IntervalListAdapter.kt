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
    private val mCachedViews: HashMap<Long, View> = HashMap()
    val mCachedControllers: HashMap<Long, IntervalController> = HashMap()

    companion object {
        public var IN_MEMORY_DB = false
    }

    init {
        if(!IN_MEMORY_DB) {
            mdb = IntervalMeDatabase.getInstance(mHostActivity.applicationContext)
        }else{
            mdb = IntervalMeDatabase.getTemporaryInstance(mHostActivity.applicationContext)
        }
        mIntervalDao = mdb!!.intervalDataDao()
    }

    /**
     * Changes the database source for the adapter.
     * This is mainly used for dependency injection
     * @param db The database to change to. All dao's will be created from this
     */
    public fun updateDataSource(db: IntervalMeDatabase){
        mdb = db
        mIntervalDao = mdb!!.intervalDataDao()
        notifyDataSetInvalidated()

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

        if(!isLastChild) {
            previousInterval = getChild(groupPosition, childPosition - 1) as IntervalData
        }

        //toReturn = mCachedViews[childOfInterval.id]

        //Top null check for cached view
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

        mCachedControllers[previousInterval?.id]?.setNextInterval(controller)

        //mCachedViews[childOfInterval.id] = toReturn
        mCachedControllers[childOfInterval.id] = controller


        editButton.setOnClickListener {
            mHostActivity.launchAddInEditMode(childOfInterval)
        }
        // Ensures when we move items around, the next intervals are getting updated
        if(isLastChild){
            mCachedControllers[childOfInterval.id]?.setNextInterval(null)
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
}