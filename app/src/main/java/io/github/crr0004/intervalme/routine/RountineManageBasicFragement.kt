package io.github.crr0004.intervalme.routine

import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import kotlinx.android.synthetic.main.fragment_routine_manage_basic.view.*
import kotlinx.android.synthetic.main.routine_manage_basic_single_item.view.*

class RoutineManageBasicFragment : Fragment(){

    private lateinit var mModel: RoutineViewModel
    private lateinit var mAdapter: RoutineManageBasicItemsAdapter
    //private val exercises = ArrayList<ExerciseData>(1)

    private val mSelectedItems = SparseBooleanArray()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_routine_manage_basic, container, false)
        mModel = ViewModelProviders.of(this.activity!!).get(RoutineViewModel::class.java)
        view.apply {
            findViewById<RecyclerView>(R.id.routineManageBasicRecycler).apply {
                layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
                mAdapter = RoutineManageBasicItemsAdapter(this@RoutineManageBasicFragment)
                mAdapter.routine = mModel.routineToEdit

                adapter = mAdapter
            }
        }
        val routineEditId = activity?.intent?.getLongExtra(RoutineManageActivity.routine_edit_id_key, -1) ?: -1
        if(routineEditId > 0){
            mModel.setRoutineToEdit(routineEditId)
        }
        mModel.getRoutineLiveData().observe(this.activity!!, android.arch.lifecycle.Observer{
            if(it != null){
                view.routineManageBasicDescriptionTxt.setText(it.description)
                view.routineManageTemplateChxBox.isChecked = it.isTemplate
                mAdapter.routine = it
                mAdapter.notifyDataSetChanged()
            }
        })
        view.routineEditCommitBtn.setOnClickListener {
            mModel.routineToEdit.description = view.routineManageBasicDescriptionTxt.text.toString()
            //mModel.routineToEdit.exercises.addAll(exercises)
            mModel.commit()
            activity!!.finish()
        }
        view.routineEditDeleteExerciseBtn.setOnClickListener {
            for(i in mSelectedItems.size()-1 downTo 0){
                val pos = mSelectedItems.keyAt(i)
                mAdapter.notifyDataSetChanged()

                mModel.deleteExerciseAt(pos)
            }
            mSelectedItems.clear()

        }
        view.routineManageTemplateChxBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(mModel.isInEditMode){
                // In edit mode, the routine should be copied rather than updating
                buttonView.isChecked = false
                val builder: AlertDialog.Builder? = activity?.let { activity ->
                    AlertDialog.Builder(activity)
                }
                builder?.setMessage(R.string.dialog_message_copy_routine_to_template)
                        ?.setTitle(R.string.dialog_title_copy_routine_to_template)
                builder?.setPositiveButton(R.string.okay) { dialog: DialogInterface, _: Int ->
                    mModel.copyRoutineToTemplate(mModel.routineToEdit)
                    Toast.makeText(buttonView.context, R.string.copied_routine_to_template, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                builder?.setNegativeButton(R.string.cancel){dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    buttonView.isChecked = false
                }
                builder?.show()
            }else {
                mModel.routineToEdit.isTemplate = isChecked
            }
        }

        view.routineEditAddExerciseBtn.setOnClickListener {
            val e = ExerciseData()
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
        var routine: RoutineSetData? = null
        //private lateinit var mRoutineData: ArrayList<ExerciseData>
        //var values: ArrayList<ExerciseData>
        //set(value) {mRoutineData = value}
        //get() {return mRoutineData}


        override fun onCreateViewHolder(parent: ViewGroup, pos: Int): RoutineManageBasicItemViewHolder {
            val view = LayoutInflater.from(mHost.context).inflate(R.layout.routine_manage_basic_single_item, parent, false)

            return RoutineManageBasicItemViewHolder(mHost, view)
        }

        override fun getItemCount(): Int {
            return routine?.exercises?.size ?: 0
        }

        override fun onBindViewHolder(holder: RoutineManageBasicItemViewHolder, pos: Int) {
            if(routine != null)
                holder.bind(routine!!.exercises[pos], pos)
        }

        override fun onViewRecycled(holder: RoutineManageBasicItemViewHolder) {
            holder.unbind()
            super.onViewRecycled(holder)
        }

        override fun getItemViewType(position: Int): Int {
            return 0
        }
    }

    class RoutineManageBasicItemViewHolder(val host: RoutineManageBasicFragment, view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var v0TextWatcher: TextWatcher
        private lateinit var v1TextWatcher: TextWatcher
        private lateinit var v2TextWatcher: TextWatcher
        private lateinit var descTextWatcher: TextWatcher
        fun unbind(){
            itemView.rMBSICheckBox.setOnCheckedChangeListener(null)
            itemView.rMBSIValue0.removeTextChangedListener(v0TextWatcher)
            itemView.rMBSIValue1.removeTextChangedListener(v1TextWatcher)
            itemView.rMBSIValue2.removeTextChangedListener(v2TextWatcher)
            itemView.rMBSIDescText.removeTextChangedListener(descTextWatcher)
        }
        fun bind(exerciseData: ExerciseData, pos: Int) {
            itemView.rMBSICheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                host.itemSelected(adapterPosition, isChecked)
            }
            itemView.rMBSICheckBox.isChecked = false

            v0TextWatcher = object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    exerciseData.value0 = s?.toString() ?: ""
                }
            }


            v1TextWatcher = object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    exerciseData.value1 = s?.toString() ?: ""
                }
            }



            v2TextWatcher = object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    exerciseData.value2 = s?.toString() ?: ""
                }
            }


            descTextWatcher = object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    exerciseData.description = s?.toString() ?: ""
                }
            }
            itemView.rMBSIValue0.addTextChangedListener(v0TextWatcher)
            itemView.rMBSIValue1.addTextChangedListener(v1TextWatcher)
            itemView.rMBSIValue2.addTextChangedListener(v2TextWatcher)
            itemView.rMBSIDescText.addTextChangedListener(descTextWatcher)

            itemView.rMBSIDescText.setText(exerciseData.description)
            itemView.rMBSIValue0.setText(exerciseData.value0)
            itemView.rMBSIValue1.setText(exerciseData.value1)
            itemView.rMBSIValue2.setText(exerciseData.value2)

            //view.findViewById<LinearLayout>(R.id.routineValuesLayout)
        }
    }

    
}