package io.github.crr0004.intervalme.routine

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import java.util.*

class RoutineManageBasicFragment : Fragment(){

    private lateinit var mModel: RoutineViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_routine_manage_basic, container, false)
        mModel = ViewModelProviders.of(this.activity!!).get(RoutineViewModel::class.java)
        view.apply {
            findViewById<RecyclerView>(R.id.routine_manage_basic_recycler).apply {
                layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
                adapter = RoutineManageBasicItemsAdapter(this@RoutineManageBasicFragment)
            }
        }

        return view
    }

    class RoutineManageBasicItemsAdapter(private val mHost: RoutineManageBasicFragment) : RecyclerView.Adapter<RoutineManageBasicItemViewHolder>() {
        val routineData = arrayOf(ExerciseData(description = "Squat",
                value = "hello",
                lastModified = Date()))



        override fun onCreateViewHolder(parent: ViewGroup, pos: Int): RoutineManageBasicItemViewHolder {
            val view = LayoutInflater.from(mHost.context).inflate(R.layout.routine_manage_basic_single_item, parent, false)

            return RoutineManageBasicItemViewHolder(mHost, view)
        }

        override fun getItemCount(): Int {
            return routineData.size
        }

        override fun onBindViewHolder(holder: RoutineManageBasicItemViewHolder, pos: Int) {
            holder.bind(routineData[pos])
        }

    }

    class RoutineManageBasicItemViewHolder(itemView: RoutineManageBasicFragment, val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(exerciseData: ExerciseData) {
            this.view.findViewById<TextView>(R.id.routineDescText2).text = exerciseData.description
            val value= view.findViewById<EditText>(R.id.routineValuesLayout)
            value.setText(exerciseData.value)


            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }
    }
}