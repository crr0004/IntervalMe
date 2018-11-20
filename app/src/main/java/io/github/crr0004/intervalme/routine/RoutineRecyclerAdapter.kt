package io.github.crr0004.intervalme.routine

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    var positionMap: HashMap<Int, RoutinePositionMap> = HashMap(1)
    var totalCount: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): RoutineSetViewHolder {

        val viewHolder: RoutineSetViewHolder
        viewHolder = if(getItemViewType(pos) == 0){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_single, parent, false)

            RoutineSetViewHolder(view, mHost)
        }else{
            ExerciseViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.routine_list_single_exercise, parent, false),
                    mHost
            )
        }


        return viewHolder
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
        super.onViewRecycled(holder)
        holder.unbind()
    }

    data class RoutinePositionMap(val pos: Int, val routineData: RoutineSetData)

    open class RoutineSetViewHolder(view: View, private val mHost: RoutineRecyclerAdapterActionsI) : RecyclerView.ViewHolder(view) {
        open fun bind(routineData: RoutineSetData, index: Int) {
            itemView.findViewById<TextView>(R.id.routineSingleName).text = routineData.description
            if(!mHost.isShowEditButtons()){
                itemView.routineListGroupEditBtn.visibility = View.INVISIBLE
                itemView.routineListGroupDeleteBtn.visibility = View.INVISIBLE
            }else{
                itemView.routineListGroupEditBtn.visibility = View.VISIBLE
                itemView.routineListGroupDeleteBtn.visibility = View.VISIBLE
            }
            itemView.routineListGroupEditBtn.setOnClickListener {
                val intent = Intent(itemView.context, RoutineManageActivity::class.java)
                intent.putExtra(RoutineManageActivity.routine_edit_id_key, routineData.routineId)
                itemView.context.startActivity(intent)
            }
            itemView.routineListGroupDeleteBtn.setOnClickListener {
                mHost.deleteRoutine(routineData)
            }
            //itemView.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }
        open fun unbind(){
            itemView.routineListGroupEditBtn.setOnClickListener(null)
            itemView.routineListGroupDeleteBtn.setOnClickListener(null)
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
