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
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import kotlinx.android.synthetic.main.fragment_routine_manage_basic.*
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
        val routineEditId = activity?.intent?.getLongExtra(RoutineManageActivity.routine_edit_id_key, -1) ?: -1
        if(routineEditId >= 0){
            mModel.setRoutineToEdit(routineEditId)
        }
        this.routineEditCommitBtn.setOnClickListener {
            mModel.mRoutineToEdit?.value?.exercises?.addAll(arrayListOf(
                    ExerciseData(description = "Squat",
                    lastModified = Date(),
                    value0 = "",
                    value1 = "",
                    value2 = ""),
                    ExerciseData(description = "Dead lift",
                            lastModified = Date(),
                            value0 = "",
                            value1 = "",
                            value2 = "")))
            mModel.commit()
        }

        return view
    }

    class RoutineManageBasicItemsAdapter(private val mHost: RoutineManageBasicFragment) : RecyclerView.Adapter<RoutineManageBasicItemViewHolder>() {
        val routineData = arrayOf(ExerciseData(description = "Squat",
                lastModified = Date(),
                value0 = "",
                value1 = "",
                value2 = ""))



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
            value.setText(exerciseData.value0)


            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }
    }
}