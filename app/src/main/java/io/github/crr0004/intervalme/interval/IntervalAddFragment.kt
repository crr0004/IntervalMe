package io.github.crr0004.intervalme.interval

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.GestureDetectorCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.IntervalData
import kotlinx.android.synthetic.main.fragment_interveraledit.*

/**
 * This fragment is responsible for the interval properties adding and editing.
 * It has two modes. Editing and adding.
 */
class IntervalAddFragment : Fragment(), IntervalSimpleGroupListFragment.OnFragmentInteractionListener {

    private var mDurationTextView: EditText? = null
    private var mGestureDetector: GestureDetectorCompat? = null

    private var mIntervalToEdit: MutableLiveData<IntervalData>? = null
    private lateinit var mModelProvider: IntervalViewModel
    private var mGroupSelectionFragment: IntervalSimpleGroupListFragment? = null
    private lateinit var mListener: IntervalAddFragmentInteractionI
    private lateinit var mModel: IntervalAddSharedModel
    private lateinit var mDurationGestureDetector: DurationGestureDetector

    companion object {
        const val EDIT_MODE_FLAG_ID = "edit_mode"
        const val EDIT_MODE_FLAG_INTERVAL_ID = "edit_mode_interval_id"
    }

    interface IntervalAddFragmentInteractionI{
        fun attachedTo(intervalAddActivity: IntervalAddFragment)
        fun setResult(result_OK: Int, intent: Intent)
        fun wantToFinish()
        fun getCreationIntent(): Intent

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mModel = ViewModelProviders.of(activity!!).get(IntervalAddSharedModel::class.java)
        mModelProvider = ViewModelProviders.of(activity!!).get(IntervalViewModel::class.java)
        mDurationGestureDetector = DurationGestureDetector(mModel)

        //mGroupSelectionFrag = findViewById<Frag>(R.id.intervalAddGroupSelectionFrag)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_interveraledit, container, false)
    }



    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is IntervalAddFragmentInteractionI) {
            mListener = context
            mListener.attachedTo(this)
        } else {
            throw RuntimeException(context.toString() + " must implement IntervalPropertiesEditFragmentInteractionI")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val invertAddMinus = PreferenceManager.getDefaultSharedPreferences(this.context)
                .getBoolean("ui_invert_add_minus", false)

        // Invert the add and minus buttons for users who want it
        if(invertAddMinus){
            val increaseParams = view.findViewById<View>(R.id.increaseDurationBtn).layoutParams
            val decreaseParams = view.findViewById<View>(R.id.decreaseDurationBtn).layoutParams
            view.findViewById<View>(R.id.increaseDurationBtn).layoutParams = decreaseParams
            view.findViewById<View>(R.id.decreaseDurationBtn).layoutParams = increaseParams
        }

        mDurationTextView = view.findViewById(R.id.intervalDurationTxt)
        mGestureDetector = GestureDetectorCompat(view.context.applicationContext, mDurationGestureDetector)
        this.intervalNameTxt.setOnEditorActionListener { textView, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                this.intervalDurationTxt.requestFocus()
                mModel.intervalToEdit.label = textView.text.toString()
            }
            true
        }
        this.intervalNameTxt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                mModel.intervalToEdit.label = p0.toString()
            }
        })



        onCreateSetListeners(view)

        createInEditMode(view)

    }

    private fun onCreateSetListeners(view: View) {

        mDurationGestureDetector.mDurationTextView = mDurationTextView
        view.findViewById<View>(R.id.increaseDurationBtn).setOnTouchListener { v, event ->
            mDurationGestureDetector.direction = 1

            val results = mGestureDetector!!.onTouchEvent(event)
            if (event?.action == MotionEvent.ACTION_UP) {
                mDurationGestureDetector.onUp(event)
            }
            results
        }
        view.findViewById<View>(R.id.decreaseDurationBtn).setOnTouchListener { v, event ->
            mDurationGestureDetector.direction = -1
            val results = mGestureDetector!!.onTouchEvent(event)
            if (event?.action == MotionEvent.ACTION_UP) {
                mDurationGestureDetector.onUp(event)
            }
            results
        }

        mDurationTextView?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val seconds = v.text.toString().toLong()
                mDurationGestureDetector.mDuration = seconds
                mModel.intervalToEdit.duration = mDurationGestureDetector.mDuration
                v.clearFocus()
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }
        mDurationTextView?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                try {
                    mDurationGestureDetector.mDuration = p0.toString().toLong()
                    mModel.intervalToEdit.duration = mDurationGestureDetector.mDuration
                }catch(e: NumberFormatException){

                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
    }

    private fun createInEditMode(view: View) {
        if (mModel.isInEditMode) {
            mIntervalToEdit = mModel.getIntervalToEdit()
            mIntervalToEdit?.observe(this, android.arch.lifecycle.Observer { it ->
                if (it != null) {
                    mDurationTextView?.setText(it.duration.toString())
                    view.findViewById<TextView>(R.id.intervalNameTxt).text = it.label
                    view.findViewById<Button>(R.id.intervalAddBtn).text = resources.getString(R.string.update)
                    mDurationGestureDetector.mDuration = it.duration
                    mDurationGestureDetector.updateDurationText(0) // Ensures text is formatted correctly
                    if(!it.ownerOfGroup) {
                        mModelProvider.getGroupOwner(it.group).observe(this, android.arch.lifecycle.Observer {
                            if (it != null)
                                mGroupSelectionFragment?.selectItem(it)
                        })
                    }
                }
            })


        }
    }

    /**
     * Called when the SimpleGroupListFragment changes selection. It sets the
     * selected group in the model
     * @see IntervalSimpleGroupListFragment
     */
    override fun onItemSelected(interval: IntervalData, isSelected: Boolean) {
        if(isSelected) {
            mModel.setIntervalToEditGroup(interval)
        }else{
            mModel.setIntervalToEditGroup(null)
        }
    }



    override fun attachedTo(intervalSimpleGroupListFragment: IntervalSimpleGroupListFragment) {
        mGroupSelectionFragment = intervalSimpleGroupListFragment
    }

    override fun detachedFrom(intervalSimpleGroupListFragment: IntervalSimpleGroupListFragment) {
        mGroupSelectionFragment = null
    }

    /**
     * Controls the gestures for increasing and decreasing the duration.
     * Without this, click the buttons can feel clunky
     */
    private class DurationGestureDetector(val mModel: IntervalAddSharedModel) : GestureDetector.SimpleOnGestureListener() {
        var direction: Int = 1
        var mDurationTextView: TextView? = null
        var mDuration = 0L
        companion object {
            const val DEBUG_TAG = "IADTxtVGesture"    
        }
        private val mAddDurationRunnable = AddContinuousDurationRunnable(this)

        fun updateDurationText(addToDuration: Long = 0L){
            val valToAdd = (addToDuration*direction)
            Log.d(DEBUG_TAG, "Adding $valToAdd")
            if(mDuration + valToAdd > 0) {
                mDuration += valToAdd
            }else{
                mDuration = 0
            }
            mDurationTextView?.text = String.format("%03d", mDuration)
            mModel.intervalToEdit.duration = mDuration
        }

        fun onUp(e: MotionEvent?){
            mAddDurationRunnable.reset()
        }

        override fun onDown(e: MotionEvent?): Boolean {
            Log.d(DEBUG_TAG, "Increase Duration onDown Click")

            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            updateDurationText(1)
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            updateDurationText(2)
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            Log.d(DEBUG_TAG, "Increase Duration Long Click")
            mAddDurationRunnable.start()
        }
    }

    /**
     * This controls when continuously adding to duration when the buttons are held down
     */
    private class AddContinuousDurationRunnable(val mDurationGestureDetector: DurationGestureDetector) : Runnable{

        var mRunning = false
        var mStartingTime: Long = 0
        var mAddScale = 1L


        private fun beforeStart(){
            mStartingTime = SystemClock.elapsedRealtime()
        }

        fun reset(){
            mRunning = false
            mAddScale = 1
        }

        override fun run() {
            if(mRunning) {

                val runningTime = (SystemClock.elapsedRealtime() - mStartingTime)
                if(runningTime % 500 in 0..10){
                    mAddScale++
                }
                if(runningTime % 100 in 0..10){
                    mDurationGestureDetector.updateDurationText(1*mAddScale)
                }
                mDurationGestureDetector.mDurationTextView?.post(this)
            }else{
                reset()
            }
        }

        fun start() {
            if(mRunning){
                Log.d("IACRrun", "Runnable called to start while running")
            }
            mRunning = true
            beforeStart()
            run()
        }
    }

}
