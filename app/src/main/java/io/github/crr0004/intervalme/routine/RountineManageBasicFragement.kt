package io.github.crr0004.intervalme.routine

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import kotlinx.android.synthetic.main.fragment_routine_manage_basic.view.*
import kotlinx.android.synthetic.main.routine_manage_basic_single_item.view.*
import java.util.*

class RoutineManageBasicFragment : Fragment(){

    private lateinit var mModel: RoutineViewModel
    private lateinit var mAdapter: RoutineManageBasicItemsAdapter
    private val exercises = ArrayList<ExerciseData>(1)

    private val mSelectedItems = SparseBooleanArray()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_routine_manage_basic, container, false)
        mModel = ViewModelProviders.of(this.activity!!).get(RoutineViewModel::class.java)
        view.apply {
            findViewById<RecyclerView>(R.id.routineManageBasicRecycler).apply {
                layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
                mAdapter = RoutineManageBasicItemsAdapter(this@RoutineManageBasicFragment)
                mAdapter.values = exercises

                adapter = mAdapter
            }
        }
        val routineEditId = activity?.intent?.getLongExtra(RoutineManageActivity.routine_edit_id_key, -1) ?: -1
        if(routineEditId > 0){
            mModel.setRoutineToEdit(routineEditId)
            mModel.mRoutineToEdit.observe(this.activity!!, android.arch.lifecycle.Observer{
                if(it != null){
                    view.routineManageBasicDescriptionTxt.setText(it.description)
                    exercises.clear()
                    exercises.addAll(it.exercises)
                    mAdapter.values = exercises
                    mAdapter.notifyDataSetChanged()
                }
            })
            view.routineEditCommitBtn.setText(R.string.update)
        }
        view.routineEditCommitBtn.setOnClickListener {
            mModel.routineToEdit.description = view.routineManageBasicDescriptionTxt.text.toString()
            //mModel.routineToEdit.exercises.addAll(exercises)
            mModel.commit()
            activity!!.finish()
        }
        view.routineEditDeleteExerciseBtn.setOnClickListener {
            for(i in mSelectedItems.size()-1 downTo 0){
                val pos = mSelectedItems.keyAt(i)
                exercises.removeAt(pos)
                mAdapter.notifyDataSetChanged()

                mModel.deleteExerciseAt(pos)
            }
            mSelectedItems.clear()

        }

        view.routineEditAddExerciseBtn.setOnClickListener {
            val e = ExerciseData()
            exercises.add(e)
            mModel.routineToEdit.exercises.add(e)
            mAdapter.notifyDataSetChanged()
        }

        return view
    }

    private fun itemSelected(adapterPosition: Int, checked: Boolean) {
        if(checked)
            mSelectedItems.put(adapterPosition, checked)
        else
            mSelectedItems.delete(adapterPosition)
    }

    class RoutineManageBasicItemsAdapter(private val mHost: RoutineManageBasicFragment) : RecyclerView.Adapter<RoutineManageBasicItemViewHolder>() {
        private lateinit var mRoutineData: ArrayList<ExerciseData>
        var values: ArrayList<ExerciseData>
        set(value) {mRoutineData = value}
        get() {return mRoutineData}


        override fun onCreateViewHolder(parent: ViewGroup, pos: Int): RoutineManageBasicItemViewHolder {
            val view = LayoutInflater.from(mHost.context).inflate(R.layout.routine_manage_basic_single_item, parent, false)

            return RoutineManageBasicItemViewHolder(mHost, view)
        }

        override fun getItemCount(): Int {
            return mRoutineData.size
        }

        override fun onBindViewHolder(holder: RoutineManageBasicItemViewHolder, pos: Int) {
            holder.bind(mRoutineData[pos])
        }

    }

    class RoutineManageBasicItemViewHolder(val host: RoutineManageBasicFragment, val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(exerciseData: ExerciseData) {
            this.view.rMBSIDescText.setText(exerciseData.description)
            view.rMBSIValue0.setText(exerciseData.value0)
            view.rMBSIValue1.setText(exerciseData.value1)
            view.rMBSIValue2.setText(exerciseData.value2)
            view.rMBSICheckBox.setOnCheckedChangeListener { buttonView, isChecked -> 
                host.itemSelected(adapterPosition, isChecked)
            }
            view.rMBSICheckBox.isChecked = false

            view.rMBSIValue0.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    exerciseData.value0 = s?.toString() ?: ""
                }
            })
            view.rMBSIValue1.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    exerciseData.value1 = s?.toString() ?: ""
                }
            })
            view.rMBSIValue2.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    exerciseData.value2 = s?.toString() ?: ""
                }
            })
            view.rMBSIDescText.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    exerciseData.description = s?.toString() ?: ""
                }
            })


            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }
    }

    
}