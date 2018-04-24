package io.github.crr0004.intervalme

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.widget.TextView


/**
 * Created by crr00 on 24-Apr-18.
 */
class IntervalListAdapter constructor(private val mContext: Context): BaseExpandableListAdapter() {


    override fun getGroup(groupPosition: Int): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun hasStableIds(): Boolean {
        //The ids of the data will be not be consistent across changes
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var toReturn: View

        if(convertView == null){
            val infalInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            toReturn = infalInflater.inflate(R.layout.interval_group, null);
        }else{
            toReturn = convertView
        }

        toReturn.findViewById<TextView>(R.id.textView).text = "Hello"

        return toReturn
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return 0
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupId(groupPosition: Int): Long {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return 0
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupCount(): Int {
        //TODO placeholder
        return 1
    }
}