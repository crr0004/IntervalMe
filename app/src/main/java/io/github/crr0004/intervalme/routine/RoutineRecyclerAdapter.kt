package io.github.crr0004.intervalme.routine

import android.animation.ObjectAnimator
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import kotlinx.android.synthetic.main.interval_group.view.*
import kotlinx.android.synthetic.main.routine_list_single_exercise.view.*
import kotlinx.android.synthetic.main.routine_single.view.*

interface RoutineRecyclerAdapterActionsI{
    fun deleteRoutine(routineData: RoutineSetData)
    fun isShowEditButtons(): Boolean
    fun update(exerciseData: ExerciseData)
    fun isOverrideRoutineSetViewHolder() : Boolean {return false}
    fun getRoutineSetViewHolder(parent: ViewGroup, pos: Int) : RoutineSetViewHolder? {return null}
    fun update(routineData: RoutineSetData)
}

interface RoutineRecyclerViewHolderActionsI{
    fun update(exerciseData: ExerciseData)
    fun isGroupExpanded(adapterPosition: Int): Boolean
    fun isShowEditButtons(): Boolean
    fun deleteRoutine(routineData: RoutineSetData)
    fun update(exerciseData: RoutineSetData)
    fun toggleGroupExpanded(adapterPosition: Int)

}

data class RoutinePositionMap(val pos: Int, val routineData: RoutineSetData)

class RoutineRecyclerAdapter(private val mHost: RoutineRecyclerAdapterActionsI) : Adapter<RoutineSetViewHolder>(), RoutineRecyclerViewHolderActionsI{
    var values: ArrayList<RoutineSetData>? = null
    set(value) {
        var index = 0
        totalCount = 0
        totalCount += value?.size ?: 0
        value?.forEach { routineSetData ->
            positionMap[index] = RoutinePositionMap(index, routineSetData)
            totalCount += routineSetData.exercises.size
            index += routineSetData.exercises.size+1
        }
        field = value
        notifyDataSetChanged()
    }
    private var positionMap: HashMap<Int, RoutinePositionMap> = HashMap(1)
    private val mExpandedGroups: SparseBooleanArray = SparseBooleanArray()
    var totalCount: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineSetViewHolder {

        val viewHolder: RoutineSetViewHolder
        viewHolder = if(viewType == 0){

            if(mHost.isOverrideRoutineSetViewHolder())
                mHost.getRoutineSetViewHolder(parent, viewType)!!
            else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_single, parent, false)
                val menu = PopupMenu(parent.context, view, (Gravity.END or Gravity.TOP))
                menu.menuInflater.inflate(R.menu.menu_routine_group, menu.menu)
                val holder = RoutineSetViewHolder(view, this)
                holder.menu = menu
                holder
            }
        }else{
            ExerciseViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.routine_list_single_exercise, parent, false),
                    this
            )
        }

        return viewHolder
    }

    fun updateRoutineViews(){
        var index = 0
        values?.forEach { routineSetData ->
            this.notifyItemChanged(index)
            index += routineSetData.exercises.size+1
        }
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    override fun getItemId(pos: Int): Long {
        return if(getItemViewType(pos) == 0){
            positionMap[pos]!!.routineData.routineId
        }else {
            val data = positionMap[pos]
            val index = pos - data!!.pos
            data.routineData.exercises[index - 1].id
        }
    }

    override fun getItemViewType(pos: Int): Int {
        val data = positionMap[pos]
        return if(data != null && data.pos == pos){
            0
        }else{
            1
        }
    }

    override fun getItemCount(): Int {
        return if(values != null)
            totalCount
        else
            0
    }

    override fun onBindViewHolder(holderRoutineSet: RoutineSetViewHolder, pos: Int) {
        val data = positionMap[pos]
        if(getItemViewType(pos) == 0) {
            data!!.routineData.exercises.forEachIndexed { index, _ ->
                positionMap[pos + index + 1] = RoutinePositionMap(pos, data.routineData)
            }
        }
        val index = pos - data!!.pos
        holderRoutineSet.bind(data.routineData, index)
    }

    override fun onViewRecycled(holder: RoutineSetViewHolder) {
        holder.unbind()
    }

    override fun update(exerciseData: ExerciseData) {
        mHost.update(exerciseData)
    }

    override fun isGroupExpanded(adapterPosition: Int): Boolean {
        return mExpandedGroups[adapterPosition, false]
    }

    override fun isShowEditButtons(): Boolean {
        return mHost.isShowEditButtons()
    }

    override fun deleteRoutine(routineData: RoutineSetData) {
        mHost.deleteRoutine(routineData)
    }

    override fun update(exerciseData: RoutineSetData) {
        mHost.update(exerciseData)
    }

    override fun toggleGroupExpanded(adapterPosition: Int) {
        // If the group is already expanded then it will have an entry, which will we invert,
        // however it doesn't, it will come back as false so it will be inverted to true
        mExpandedGroups.put(adapterPosition, !mExpandedGroups[adapterPosition, false])
        notifyItemRangeChanged(adapterPosition+1, (positionMap[adapterPosition]?.routineData?.exercises?.size ?: 0))
    }
}

open class RoutineSetViewHolder(view: View, private val mHost: RoutineRecyclerViewHolderActionsI) : RecyclerView.ViewHolder(view) {
    var menu: PopupMenu? = null
    private fun toggleMoreButton(view: View){
        if(mHost.isGroupExpanded(adapterPosition)) {
            ObjectAnimator.ofFloat(view, "rotation", 0f, 180f).apply {
                duration = 150
                start()
            }
        }else{
            ObjectAnimator.ofFloat(view, "rotation", 180f, 0f).apply {
                duration = 150
                start()
            }
        }
    }

    open fun bind(routineData: RoutineSetData, index: Int) {
        itemView.routineSingleName.text = routineData.description
        if(!mHost.isShowEditButtons()){
            itemView.routineListGroupEditBtn.visibility = View.INVISIBLE
            itemView.routineListGroupDeleteBtn.visibility = View.INVISIBLE
        }else{
            itemView.routineListGroupEditBtn.visibility = View.VISIBLE
            itemView.routineListGroupDeleteBtn.visibility = View.VISIBLE
        }
        itemView.routineListGroupEditBtn.setOnClickListener {
            editRoutine(routineData)
        }
        itemView.routineListGroupDeleteBtn.setOnClickListener {
            mHost.deleteRoutine(routineData)
        }
        itemView.routineGroupMenuView.setOnClickListener {
            menu?.show()
        }
        if(mHost.isGroupExpanded(adapterPosition)) {
            itemView.routineGroupMoreButton.rotation = 180f
        }else{
            itemView.routineGroupMoreButton.rotation = 0f
        }
        if(routineData.isDone){
            menu?.menu?.findItem(R.id.routine_group_menu_mark_done)?.title = itemView.context.getString(R.string.unmark_done)
        }else{
            menu?.menu?.findItem(R.id.routine_group_menu_mark_done)?.title = itemView.context.getString(R.string.mark_done)
        }
        menu?.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.routine_group_menu_mark_done -> {
                    routineData.isDone = !routineData.isDone
                    mHost.update(routineData)
                    true
                }
                R.id.routine_group_menu_delete -> {
                    mHost.deleteRoutine(routineData)
                    true
                }
                R.id.routine_group_menu_edit -> {
                    editRoutine(routineData)
                    true
                }
                else -> {
                    false
                }
            }
        }
        itemView.routineGroupMoreButton.setOnClickListener {
            mHost.toggleGroupExpanded(adapterPosition)
            toggleMoreButton(it)
        }
        //itemView.findViewById<LinearLayout>(R.id.routineValuesLayout)
    }

    private fun editRoutine(routineData: RoutineSetData) {
        val intent = Intent(itemView.context, RoutineManageActivity::class.java)
        intent.putExtra(RoutineManageActivity.routine_edit_id_key, routineData.routineId)
        itemView.context.startActivity(intent)
    }

    open fun unbind(){
        itemView.routineListGroupEditBtn.setOnClickListener(null)
        itemView.routineListGroupDeleteBtn.setOnClickListener(null)
        menu?.setOnMenuItemClickListener(null)
        itemView.routineGroupMenuView.setOnClickListener(null)
        itemView.routineGroupMoreButton.setOnClickListener(null)
    }

}

class ExerciseViewHolder(view: View, val mHost: RoutineRecyclerViewHolderActionsI) :
        RoutineSetViewHolder(view, mHost){
    /**
     * @param index offset of exercise item in [RoutineSetData], starting at 1
     */
    override fun bind(routineData: RoutineSetData, index: Int) {
        if(mHost.isGroupExpanded(adapterPosition-index)){
            itemView.visibility = View.VISIBLE
            itemView.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        }else{
            itemView.layoutParams.height = 0
            itemView.visibility = View.GONE
        }
        val exerciseData = routineData.exercises[index-1]
        itemView.rLSEIDescText.text = exerciseData.description
        itemView.rLSEIValue0.text = exerciseData.value0
        itemView.rLSEIValue1.text = exerciseData.value1
        itemView.rLSEIValue2.text = exerciseData.value2
        itemView.rLSEICheckBox.isChecked = exerciseData.isDone
        itemView.rLSEICheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            exerciseData.isDone = isChecked
            // We do this so the animation has time to complete
            buttonView.postDelayed({
                mHost.update(exerciseData)
            }, buttonView.resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
        }
    }

    override fun unbind() {
        itemView.rLSEICheckBox.setOnCheckedChangeListener(null)
    }
}
