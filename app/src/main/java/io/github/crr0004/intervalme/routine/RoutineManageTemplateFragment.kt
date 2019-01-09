package io.github.crr0004.intervalme.routine

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import kotlinx.android.synthetic.main.routine_single_template.view.*

class RoutineManageTemplateFragment : Fragment(), RoutineRecyclerAdapterActionsI, RoutineRecyclerViewHolderActionsI {


    private lateinit var mModel: RoutineViewModel
    private lateinit var mAdapter: RoutineRecyclerAdapter
    private val mSelectedItems = SparseBooleanArray()
    val isInEditMode: Boolean
    get() = mModel.isInEditMode

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
        mModel.deleteRoutine(routineData)
    }

    override fun isShowEditButtons(): Boolean {
        return true
    }

    override fun update(exerciseData: ExerciseData) {

    }

    override fun isOverrideRoutineSetViewHolder() : Boolean{
        return true
    }

    override fun update(routineData: RoutineSetData){

    }

    override fun isGroupExpanded(adapterPosition: Int): Boolean {
        return true
    }

    override fun toggleGroupExpanded(adapterPosition: Int) {

    }

    override fun routineUpdateDone(routineData: RoutineSetData) {

    }

    override fun exerciseUpdateDone(exerciseData: ExerciseData) {

    }

    override fun getRoutineSetViewHolder(parent: ViewGroup, pos: Int): RoutineSetViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.routine_single_template, parent, false)
        return RoutineSetViewHolder1(view, this)
    }

    fun editTemplate(routineData: RoutineSetData) {
        mModel.routineToEdit = routineData
        this.activity!!.findViewById<ViewPager>(R.id.container).setCurrentItem(0, true)
    }

    fun copyTemplate(routineData: RoutineSetData) {
        mModel.copyRoutineFromTemplate(routineData)
        this.activity!!.findViewById<ViewPager>(R.id.container).setCurrentItem(0, true)
    }
}

class RoutineSetViewHolder1(view: View, val mHost: RoutineManageTemplateFragment) : RoutineSetViewHolder(view, mHost){
    override fun bind(routineData: RoutineSetData, index: Int) {
        itemView.findViewById<TextView>(R.id.routineSingleName).text = routineData.description
        if(!mHost.isShowEditButtons()){
            itemView.routineListGroupEditBtn.visibility = View.INVISIBLE
            itemView.routineListGroupDeleteBtn.visibility = View.INVISIBLE
            itemView.routineListGroupCopyBtn.visibility = View.INVISIBLE
        }else{
            itemView.routineListGroupEditBtn.visibility = View.VISIBLE
            itemView.routineListGroupDeleteBtn.visibility = View.VISIBLE
            itemView.routineListGroupCopyBtn.visibility = View.VISIBLE
        }
        if(mHost.isInEditMode){
            itemView.routineListGroupEditBtn.visibility = View.GONE
            itemView.routineListGroupCopyBtn.visibility = View.GONE
        }
        itemView.routineListGroupEditBtn.setOnClickListener {
            mHost.editTemplate(routineData)
        }
        itemView.routineListGroupDeleteBtn.setOnClickListener {
            mHost.deleteRoutine(routineData)
        }
        itemView.routineListGroupCopyBtn.setOnClickListener {
            mHost.copyTemplate(routineData)
        }
        //itemView.findViewById<LinearLayout>(R.id.routineValuesLayout)
    }
    override fun unbind(){
        itemView.routineListGroupEditBtn.setOnClickListener(null)
        itemView.routineListGroupDeleteBtn.setOnClickListener(null)
    }

}
