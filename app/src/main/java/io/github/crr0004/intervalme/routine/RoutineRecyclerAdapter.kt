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
import kotlinx.android.synthetic.main.routine_manage_basic_single_item.view.*
import kotlinx.android.synthetic.main.routine_single.view.*

class RoutineRecyclerAdapter(private val mHost: RoutineListActivity) : Adapter<RoutineRecyclerAdapter.RoutineSetViewHolder>() {

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
            val view = LayoutInflater.from(mHost).inflate(R.layout.routine_single, parent, false)

            RoutineSetViewHolder(view)
        }else{
            ExerciseViewHolder(LayoutInflater.from(mHost).inflate(R.layout.routine_manage_basic_single_item, parent, false))
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

    open class RoutineSetViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        open fun bind(routineData: RoutineSetData, index: Int) {
            this.view.findViewById<TextView>(R.id.routineSingleName).text = routineData.description
            this.view.routineListGroupEditBtn.setOnClickListener {
                val intent = Intent(view.context, RoutineManageActivity::class.java)
                intent.putExtra(RoutineManageActivity.routine_edit_id_key, routineData.routineId)
                view.context.startActivity(intent)
            }


            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }

    }

    class ExerciseViewHolder(private val view: View) : RoutineSetViewHolder(view){
        override fun bind(routineData: RoutineSetData, index: Int) {
            val exerciseData = routineData.exercises[index-1]
            this.view.findViewById<TextView>(R.id.rMBSIDescText).text = exerciseData.description
            view.rMBSIValue0.setText(exerciseData.value0)
            view.rMBSIValue1.setText(exerciseData.value1)
            view.rMBSIValue2.setText(exerciseData.value2)


            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }
    }

}
