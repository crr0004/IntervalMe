package io.github.crr0004.intervalme.routine

import android.content.Intent
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import kotlinx.android.synthetic.main.routine_list_single_exercise.view.*
import kotlinx.android.synthetic.main.routine_single.view.*

class RoutineRecyclerAdapter(private val mHost: RoutineRecyclerAdapterActionsI) : Adapter<RoutineRecyclerAdapter.RoutineSetViewHolder>() {

    interface RoutineRecyclerAdapterActionsI{
        fun deleteRoutine(routineData: RoutineSetData)
        fun isShowEditButtons(): Boolean
        fun update(exerciseData: ExerciseData)
        fun isOverrideRoutineSetViewHolder() : Boolean {return false}
        fun getRoutineSetViewHolder(parent: ViewGroup, pos: Int) : RoutineSetViewHolder? {return null}
        fun update(routineData: RoutineSetData)
    }

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
    var totalCount: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): RoutineSetViewHolder {

        val viewHolder: RoutineSetViewHolder
        viewHolder = if(getItemViewType(pos) == 0){

            if(mHost.isOverrideRoutineSetViewHolder())
                mHost.getRoutineSetViewHolder(parent, pos)!!
            else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_single, parent, false)
                val menu = PopupMenu(parent.context, view, (Gravity.END or Gravity.TOP))
                menu.menuInflater.inflate(R.menu.menu_routine_group, menu.menu)
                val holder = RoutineSetViewHolder(view, mHost)
                holder.menu = menu
                holder
            }
        }else{
            ExerciseViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.routine_list_single_exercise, parent, false),
                    mHost
            )
        }
        viewHolder.setIsRecyclable(true)

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

    data class RoutinePositionMap(val pos: Int, val routineData: RoutineSetData)

    open class RoutineSetViewHolder(view: View, private val mHost: RoutineRecyclerAdapterActionsI) : RecyclerView.ViewHolder(view) {
        var menu: PopupMenu? = null
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
            menu?.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.routine_group_menu_mark_done -> {
                        routineData.isDone = true
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
        }

    }

    class ExerciseViewHolder(view: View, val mHost: RoutineRecyclerAdapterActionsI) :
            RoutineSetViewHolder(view, mHost){
        override fun bind(routineData: RoutineSetData, index: Int) {
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

}
