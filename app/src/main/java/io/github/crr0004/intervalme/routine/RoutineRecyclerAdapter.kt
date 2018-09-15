package io.github.crr0004.intervalme.routine

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import java.util.*

class RoutineRecyclerAdapter(val mHost: RoutineListActivity) : Adapter<RoutineRecyclerAdapter.ViewHolder>() {

    val routineData = arrayOf(ExerciseData(description = "Squat",
            valueCount = 10,
            values = arrayOf(10, 2, 3, 4, 5,10, 2, 3, 4, 5),
            lastModified = Date()))

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): ViewHolder {
        val view = LayoutInflater.from(mHost).inflate(R.layout.routine_single, parent, false)

        return ViewHolder(mHost, view)
    }

    override fun getItemCount(): Int {
        return routineData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(routineData[pos])
    }

    class ViewHolder(val mContext: Context, private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(exerciseData: ExerciseData) {
            this.view.findViewById<TextView>(R.id.routineDescText).text = exerciseData.description
            val valuesContainer = view.findViewById<LinearLayout>(R.id.routineValuesLayout)

            for(i: Int in 0 until exerciseData.valueCount){
                val valueView = EditText(mContext)

                valueView.setEms(10)
                valueView.inputType = InputType.TYPE_CLASS_NUMBER
                valueView.setText(exerciseData.values[i].toString())
                valueView.gravity = Gravity.CENTER_HORIZONTAL

                //valueView.width = valueView.textSize.toInt()
                valueView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)

                valuesContainer.addView(valueView)
            }

            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }

    }

}
