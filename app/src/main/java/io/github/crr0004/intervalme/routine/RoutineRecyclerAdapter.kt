package io.github.crr0004.intervalme.routine

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import kotlinx.android.synthetic.main.routine_single.view.*
import java.util.*

class RoutineRecyclerAdapter(private val mHost: RoutineListActivity) : Adapter<RoutineRecyclerAdapter.ViewHolder>() {

    private val routineData = arrayOf(ExerciseData(description = "Squat",
            value0 = "",
            value1 = "",
            value2 = "",
            lastModified = Date()))

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ViewHolder {
        val view = LayoutInflater.from(mHost).inflate(R.layout.routine_single, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return routineData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(routineData[pos])
    }

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(exerciseData: ExerciseData) {
            this.view.findViewById<TextView>(R.id.rMBSIDescText).text = exerciseData.description
            val value = view.findViewById<EditText>(R.id.value0)
            view.value0.setText(exerciseData.value0)
            view.value1.setText(exerciseData.value1)
            view.value2.setText(exerciseData.value2)



            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }

    }

}
