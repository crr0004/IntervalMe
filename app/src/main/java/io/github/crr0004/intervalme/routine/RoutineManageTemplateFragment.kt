package io.github.crr0004.intervalme.routine

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineSetData

class RoutineManageTemplateFragment : Fragment(), RoutineRecyclerAdapter.RoutineRecyclerAdapterActionsI {


    private lateinit var mModel: RoutineViewModel
    private lateinit var mAdapter: RoutineRecyclerAdapter
    private val mSelectedItems = SparseBooleanArray()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_routine_manage_templates, container, false)
        mModel = ViewModelProviders.of(this.activity!!).get(RoutineViewModel::class.java)
        view.apply {
            findViewById<RecyclerView>(R.id.routineManageTemplateRecycler).apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                mAdapter = RoutineRecyclerAdapter(this@RoutineManageTemplateFragment)

                adapter = mAdapter
            }
        }

        mModel.getTemplateRoutines().observe(this.activity!!, Observer {
            mAdapter.values = it
        })

        return view
    }

    override fun deleteRoutine(routineData: RoutineSetData) {

    }

    override fun isShowEditButtons(): Boolean {
        return true
    }

    override fun update(exerciseData: ExerciseData) {

    }
}
