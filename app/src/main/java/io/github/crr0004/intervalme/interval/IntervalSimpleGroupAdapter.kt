package io.github.crr0004.intervalme.interval

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.IntervalData
import kotlinx.android.synthetic.main.interval_group.view.*

class IntervalSimpleGroupAdapter : RecyclerView.Adapter<IntervalSimpleGroupAdapter.SimpleGroupViewHolder>() {

    class SimpleGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var mBoundData: IntervalData? = null
        val itemDetails: ItemDetailsLookup.ItemDetails<Long> = SimpleGroupItemDetails(this)


        fun bind(groupData: IntervalData){
            mBoundData = groupData
            itemView.intervalGroupNameTxt.text = groupData.label
            itemView.intervalGroupPos.visibility = View.GONE
            itemView.intervalGroupLoopsLbl.visibility = View.GONE
            itemView.intervalGroupLoops.visibility = View.GONE
        }

    }

    private class SimpleGroupItemDetails(val mViewHolder: SimpleGroupViewHolder): ItemDetailsLookup.ItemDetails<Long>(){
        override fun getSelectionKey(): Long? {
            return mViewHolder.itemId
        }

        override fun getPosition(): Int {
            return mViewHolder.adapterPosition
        }

    }

    private var mTracker: SelectionTracker<Long>? = null
    private val mIdToGroupPosition: HashMap<Long, Long> = HashMap(1)
    var mGroupsList: Array<IntervalData>? = null
        @Synchronized
        get


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleGroupViewHolder {
        val group = LayoutInflater.from(parent.context).inflate(R.layout.interval_group, parent, false)

        return SimpleGroupViewHolder(group)
    }

    override fun getItemCount(): Int {

        return mGroupsList?.size ?: 0
    }

    override fun onBindViewHolder(holder: SimpleGroupViewHolder, position: Int) {
        val groupData = mGroupsList!![position]
        holder.bind(groupData)
        if(mTracker!!.isSelected(holder.itemId)){
            holder.itemView.background?.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
        }else{
            holder.itemView.background?.clearColorFilter()
        }
        holder.itemView.invalidate()
    }

    fun setTracker(tracker: SelectionTracker<Long>?) {
        mTracker = tracker
    }

    override fun getItemId(position: Int): Long {
        return mGroupsList!![position].id
    }

    fun getPositionOfId(id: Long): Long {
        return mIdToGroupPosition[id]!!
    }

    fun getItemAt(id: Long): IntervalData? {
        return mGroupsList!![mIdToGroupPosition[id]!!.toInt()]
    }

    fun setGroupPositionOfId(id: Long, groupPosition: Long) {
        mIdToGroupPosition[id] = groupPosition
    }
}