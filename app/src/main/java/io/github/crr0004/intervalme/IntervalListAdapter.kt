package io.github.crr0004.intervalme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.*
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import java.util.*
import kotlin.collections.HashMap


/**
 * Created by crr00 on 24-Apr-18.
 */
class IntervalListAdapter constructor(private val mContext: Context, private val mHost: ExpandableListView): BaseExpandableListAdapter() {

    private var mdb: IntervalMeDatabase? = null
    private var mIntervalDao: IntervalDataDOA? = null
    private val mCachedViews: HashMap<Long, View> = HashMap()

    init {
        mdb = IntervalMeDatabase.getInstance(mContext)
        mIntervalDao = mdb!!.intervalDataDao()
    }

    override fun getGroup(groupPosition: Int): Any {
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

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var toReturn: View?

        toReturn = mCachedViews[getGroupId(groupPosition)]
        if(toReturn == null) {

            toReturn = convertView

            if (toReturn == null) {
                val infalInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                toReturn = infalInflater.inflate(R.layout.interval_group, null)
            }

            val intervalData = mIntervalDao?.getGroupByOffset(groupPosition.toLong() + 1)
            toReturn!!.findViewById<TextView>(R.id.textView).text = intervalData?.label ?: "Interval not found"


            //toReturn.setOnLongClickListener(intervalLongClickListener)
            this.mHost.setOnItemLongClickListener { parentView, view, position, id ->
                val intervalData = mIntervalDao?.getGroupByOffset(position.toLong() + 1)
                val groupUUID = intervalData?.group.toString()
                val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("group uuid", groupUUID)
                Toast.makeText(mContext, "Copied UUID", Toast.LENGTH_SHORT).show()


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

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var toReturn: View?
        val childOfInterval = getChild(groupPosition, childPosition) as IntervalData

        toReturn = mCachedViews[childOfInterval.id]

        //Top null check for cached view
        if(toReturn == null) {
            toReturn = convertView


            // second null check for using a converted view
            if (toReturn == null) {
                val infalInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                //Passing null as root until I figure it out. Passing parent causes a crash
                toReturn = infalInflater.inflate(R.layout.interval_single, null)
            }


            toReturn!!.findViewById<TextView>(R.id.intervalChildNameTxt).text = childOfInterval.label ?: "Interval not found"
            val durationTextView = toReturn.findViewById<TextView>(R.id.intervalChildDurationTxt)
            durationTextView.text = childOfInterval.duration.toString()

            toReturn.findViewById<ImageButton>(R.id.intervalChildStartBtn).setOnClickListener { v ->

                if (v.getTag(R.id.id_interval_timer_tag) == null) {
                    val timerTicker = RunIntervalAsync(childOfInterval, durationTextView)
                    v.postDelayed(timerTicker, 300)
                    v.setTag(R.id.id_interval_timer_tag, RunIntervalAsync(childOfInterval, durationTextView))
                    durationTextView.setTag(R.id.id_interval_timer_running_tag, true)
                } else {
                    val timerTicker = v.getTag(R.id.id_interval_timer_tag) as RunIntervalAsync
                    val running = durationTextView.getTag(R.id.id_interval_timer_running_tag) as Boolean
                    durationTextView.setTag(R.id.id_interval_timer_running_tag, !running)
                    v.postDelayed(timerTicker, 300)
                }
            }
            mCachedViews[childOfInterval.id] = toReturn
        }

        return toReturn
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val childOfInterval = getChild(groupPosition,childPosition) as IntervalData
        return childOfInterval.id
    }

    override fun getGroupCount(): Int {
        //TODO placeholder
        return mIntervalDao!!.getGroupOwners().size
    }
}