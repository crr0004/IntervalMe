package io.github.crr0004.intervalme.routine

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import kotlinx.android.synthetic.main.routine_list_single_exercise.view.*
import kotlinx.android.synthetic.main.routine_single.view.*

class RoutineRecyclerAdapter(private val mHost: RoutineRecyclerAdapterActionsI) : Adapter<RoutineRecyclerAdapter.RoutineSetViewHolder>() {

    interface RoutineRecyclerAdapterActionsI{
        fun deleteRoutine(routineData: RoutineSetData)
        fun isShowEditButtons(): Boolean
    }

    var values: ArrayList<RoutineSetData>? = null
    set(value) {
        var index = 0
        value?.forEach { routineSetData ->
            positionMap[index] = RoutinePositionMap(index, routineSetData)
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

    data class RoutinePositionMap(val pos: Int, val routineData: RoutineSetData)

    open class RoutineSetViewHolder(private val view: View, private val mHost: RoutineRecyclerAdapterActionsI) : RecyclerView.ViewHolder(view) {
        open fun bind(routineData: RoutineSetData, index: Int) {
            this.view.findViewById<TextView>(R.id.routineSingleName).text = routineData.description
            if(!mHost.isShowEditButtons()){
                this.view.routineListGroupEditBtn.visibility = View.INVISIBLE
                this.view.routineListGroupDeleteBtn.visibility = View.INVISIBLE
            }else{
                this.view.routineListGroupEditBtn.visibility = View.VISIBLE
                this.view.routineListGroupDeleteBtn.visibility = View.VISIBLE
            }
            this.view.routineListGroupEditBtn.setOnClickListener {
                val intent = Intent(view.context, RoutineManageActivity::class.java)
                intent.putExtra(RoutineManageActivity.routine_edit_id_key, routineData.routineId)
                view.context.startActivity(intent)
            }
            this.view.routineListGroupDeleteBtn.setOnClickListener {
                mHost.deleteRoutine(routineData)
            }


            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }

    }

    class ExerciseViewHolder(private val view: View, mHost: RoutineRecyclerAdapterActionsI) :
            RoutineSetViewHolder(view, mHost){
        override fun bind(routineData: RoutineSetData, index: Int) {
            val exerciseData = routineData.exercises[index-1]
            this.view.rLSEIDescText.text = exerciseData.description
            view.rLSEIValue0.text = exerciseData.value0
            view.rLSEIValue1.text = exerciseData.value1
            view.rLSEIValue2.text = exerciseData.value2


            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }
    }

}
