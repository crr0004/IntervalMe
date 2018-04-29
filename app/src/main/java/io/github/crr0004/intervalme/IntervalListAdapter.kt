package io.github.crr0004.intervalme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import java.util.*


/**
 * Created by crr00 on 24-Apr-18.
 */
class IntervalListAdapter constructor(private val mContext: Context, private val mHost: ExpandableListView): BaseExpandableListAdapter() {

    private var mdb: IntervalMeDatabase? = null
    private var mIntervalDao: IntervalDataDOA? = null

    init {
        mdb = IntervalMeDatabase.getInstance(mContext)
        mIntervalDao = mdb!!.intervalDataDao()
    }

    override fun getGroup(groupPosition: Int): Any {
        val intervalDataParent = mIntervalDao?.get(groupPosition.toLong()+1)
        return mIntervalDao!!.getAllOfGroup(intervalDataParent!!.group)
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
        val toReturn: View

        if(convertView == null){
            val infalInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            toReturn = infalInflater.inflate(R.layout.interval_group, null)
        }else{
            toReturn = convertView
        }

        val intervalData = mIntervalDao?.getGroupByOffset(groupPosition.toLong()+1)
        toReturn.findViewById<TextView>(R.id.textView).text = intervalData?.label ?: "Interval not found"


        //toReturn.setOnLongClickListener(intervalLongClickListener)
        this.mHost.setOnItemLongClickListener { parentView, view, position, id ->
            val intervalData = mIntervalDao?.getGroupByOffset(position.toLong()+1)
            val groupUUID = intervalData?.group.toString()
            val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("group uuid", groupUUID)
            Toast.makeText(mContext, "Copied UUID", Toast.LENGTH_SHORT).show()


            clipboard.primaryClip = clipData

            true
        }
        //toReturn.setOnClickListener { this.mHost.expandGroup(groupPosition)}

        return toReturn
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val intervalDataParent = mIntervalDao?.getGroupOwners()?.get(groupPosition)
        return mIntervalDao!!.getAllOfGroupWithoutOwner(intervalDataParent!!.group).size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val intervalDataParent = mIntervalDao!!.getGroupOwners()[groupPosition]
        return mIntervalDao!!.getChildOfGroupByOffset(
                (childPosition+ 1).toLong(),
                intervalDataParent.group)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return (groupPosition+1).toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val toReturn: View
        val intervalDataParent = mIntervalDao?.getGroupOwners()?.get(groupPosition)
        val childOfInterval = mIntervalDao?.getChildOfGroupByOffset(
                (childPosition+ 1).toLong(),
                intervalDataParent?.group ?: UUID.fromString("00000000-0000-0000-0000-000000000000"))

        if(convertView == null){
            val infalInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            //Passing null as root until I figure it out. Passing parent causes a crash
            toReturn = infalInflater.inflate(R.layout.interval_single,  null)
        }else{
            toReturn = convertView
        }


        toReturn.findViewById<TextView>(R.id.intervalChildNameTxt).text = childOfInterval?.label ?: "Interval not found"
        val durationTextView = toReturn.findViewById<TextView>(R.id.intervalChildDurationTxt)
        durationTextView.text = childOfInterval?.duration.toString()

        toReturn.findViewById<ImageButton>(R.id.intervalChildStartBtn).setOnClickListener { v -> v.postDelayed(RunIntervalAsync(childOfInterval!!,durationTextView),1000) }

        return toReturn
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return (groupPosition+1+childPosition).toLong()
    }

    override fun getGroupCount(): Int {
        //TODO placeholder
        return mIntervalDao!!.getGroupOwners().size
    }
}