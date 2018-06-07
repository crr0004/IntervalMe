package io.github.crr0004.intervalme

import android.content.Context
import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalDataDOA
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import kotlinx.android.synthetic.main.interval_group.view.*

class IntervalSimpleGroupAdapter(mContext: Context): RecyclerView.Adapter<IntervalSimpleGroupAdapter.SimpleGroupViewHolder>() {

    class SimpleGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var mBoundData: IntervalData? = null
        val itemDetails: ItemDetailsLookup.ItemDetails<Long> = SimpleGroupItemDetails(this)


        var boundData: IntervalData? = null
        get() {return mBoundData}
        fun bind(groupData: IntervalData){
            mBoundData = groupData
            itemView.intervalGroupNameTxt.text = groupData.label
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

    private val mDb: IntervalMeDatabase
    private val mIntervalDAO: IntervalDataDOA
    private var mTracker: SelectionTracker<Long>? = null
    private val mGroupsList: HashMap<Long, IntervalData?>
        @Synchronized
        get

    init{
        mDb = IntervalMeDatabase.getInstance(mContext)!!
        mIntervalDAO = mDb.intervalDataDao()
        mGroupsList = HashMap(3)
        val doneCallBack = {
            this.notifyDataSetChanged()
        }
        GetGroups(mGroupsList, doneCallBack).execute(mContext)

        //mIntervalDAO.getGroupOwners().
    }

    private class GetGroups(val groupsList: HashMap<Long, IntervalData?>, val doneCallBack: () -> Unit): AsyncTask<Context, Int, Array<IntervalData>>(){
        override fun doInBackground(vararg p0: Context?): Array<IntervalData> {
            return IntervalMeDatabase.getInstance(p0[0]!!)!!.intervalDataDao().getGroupOwners()
        }

        override fun onPostExecute(result: Array<IntervalData>?) {
            result!!.forEachIndexed { _, intervalData ->
                groupsList[intervalData.id] = intervalData
            }
            doneCallBack()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleGroupViewHolder {
        val group = LayoutInflater.from(parent.context).inflate(R.layout.interval_group, parent, false)

        return SimpleGroupViewHolder(group)
    }

    override fun getItemCount(): Int {

        return mGroupsList.size
    }

    override fun onBindViewHolder(holder: SimpleGroupViewHolder, position: Int) {
        val groupData = mGroupsList.values.elementAt(position)!!
        holder.bind(groupData)

        holder.itemView.invalidate()
    }

    fun setTracker(tracker: SelectionTracker<Long>?) {
        mTracker = tracker
    }

    override fun getItemId(position: Int): Long {
        return mGroupsList.values.elementAt(position)!!.id
    }



    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }

    fun getPositionOfId(p0: Long): Long {
        return mGroupsList[p0]!!.groupPosition
    }

    fun getItemAt(id: Long): IntervalData? {
        return mGroupsList[id]
    }
}