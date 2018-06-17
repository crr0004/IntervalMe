package io.github.crr0004.intervalme

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.views.IntervalViewModel
import java.util.*

class IntervalAddActivity : AppCompatActivity(), IntervalSimpleGroupListFragement.OnFragmentInteractionListener {


    private var mDuration: Int = 0
    private var mDurationTextView: EditText? = null
    private val DEBUG_TAG = "IntervalAdd"
    private var mGestureDetector: GestureDetectorCompat? = null
    private val mDurationGestureDetector: DurationGestureDetector = DurationGestureDetector()
    private var mIntervalToEditID: Long = -1
    private var mIntervalToEdit: LiveData<IntervalData>? = null
    private var mSelectedIntervalGroup: IntervalData? = null
    private var mSelectedGroupChildSize: Long = 0
    private lateinit var mModelProvider: IntervalViewModel
    private var mGroupOwnersSize: LiveData<Long>? = null

    companion object {
        const val EDIT_MODE_FLAG_ID = "edit_mode"
        const val EDIT_MODE_FLAG_INTERVAL_ID = "edit_mode_interval_id"
    }


    private var mUpdatedInterval: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_interveraladd)
        mModelProvider = ViewModelProviders.of(this).get(IntervalViewModel::class.java)

        (findViewById<View>(R.id.goToListBtn)).setOnClickListener {
            val intent = Intent()
            if(mIntervalToEdit?.value != null && mUpdatedInterval) {
                intent.putExtra(EDIT_MODE_FLAG_INTERVAL_ID, mIntervalToEdit!!.value!!.id)
            }
            setResult(RESULT_OK, intent)
            mUpdatedInterval = false
            finish()
        }

        mDurationTextView = findViewById(R.id.intervalDurationTxt)
        mGestureDetector = GestureDetectorCompat(this.applicationContext, mDurationGestureDetector)

        onCreateSetListeners()

        createInEditMode()
        mGroupOwnersSize = mModelProvider.getGroupsSize()
        mGroupOwnersSize!!.observe(this, android.arch.lifecycle.Observer {
            Log.d(DEBUG_TAG, "Group owners size: $it")
        })

    }

    private fun onCreateSetListeners() {
        findViewById<Button>(R.id.intervalAddBtn).setOnClickListener(addIntervalListener)
        findViewById<Button>(R.id.goToClockSampleBtn).setOnClickListener(clockSampleBtnListener)


        mDurationGestureDetector.mDurationTextView = mDurationTextView
        findViewById<View>(R.id.increaseDurationBtn).setOnTouchListener { v, event ->
            mDurationGestureDetector.direction = 1

            val results = mGestureDetector!!.onTouchEvent(event)
            if (event?.action == MotionEvent.ACTION_UP) {
                mDurationGestureDetector.onUp(event)
            }
            results
        }
        findViewById<View>(R.id.decreaseDurationBtn).setOnTouchListener { v, event ->
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
                v.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }
    }

    private fun createInEditMode() {
        val isEditMode = intent.getBooleanExtra(EDIT_MODE_FLAG_ID, false)
        if (isEditMode) {
            mIntervalToEditID = intent.getLongExtra(EDIT_MODE_FLAG_INTERVAL_ID, -1)
            mIntervalToEdit = mModelProvider.get(mIntervalToEditID)
            mIntervalToEdit?.observe(this, android.arch.lifecycle.Observer {
                if (it != null) {
                    mDurationTextView?.setText(it.duration.toString())
                    findViewById<TextView>(R.id.intervalNameTxt).text = it.label
                    findViewById<Button>(R.id.intervalAddBtn).setOnClickListener(addIntervalEditModeListener)
                    findViewById<Button>(R.id.intervalAddBtn).text = resources.getString(R.string.update)
                    mDurationGestureDetector.mDuration = it.duration
                    mDurationGestureDetector.updateDurationText(0) // Ensures text is formatted correctly
                }
            })


        }
    }

    private val addIntervalEditModeListener = fun(v: View){
        if(mIntervalToEdit?.value != null) {
            val text = (findViewById<TextView>(R.id.intervalNameTxt)).text
            val durationText = (findViewById<TextView>(R.id.intervalDurationTxt)).text

            mIntervalToEdit?.value!!.duration = durationText.toString().toLong()
            mIntervalToEdit?.value!!.label = text.toString()
            val groupUUID = mSelectedIntervalGroup?.group
            if(mIntervalToEdit?.value!!.group != groupUUID && groupUUID != null) {
                mModelProvider.shuffleChildrenInGroupUpFrom(mIntervalToEdit?.value!!.groupPosition, mIntervalToEdit?.value!!.group)
                mIntervalToEdit?.value!!.group = groupUUID
                mIntervalToEdit?.value!!.groupPosition = mSelectedGroupChildSize+1
                mIntervalToEdit?.value!!.ownerOfGroup = false
            }else if(mSelectedIntervalGroup == null){
                mIntervalToEdit?.value!!.ownerOfGroup = true
                mIntervalToEdit?.value!!.group = UUID.randomUUID()
            }


            mIntervalToEdit?.value!!.lastModified = Date()
            mModelProvider.update(mIntervalToEdit?.value!!)

            Toast.makeText(this, "Updated interval", Toast.LENGTH_SHORT).show()
            mUpdatedInterval = true
        }
    }

    private val clockSampleBtnListener = fun(v: View){
        val intent = Intent(this, IntervalClockSampleActivity::class.java)
        startActivity(intent)
    }


    private val addIntervalListener = fun(v: View){
        val text = (findViewById<TextView>(R.id.intervalNameTxt)).text
        val durationText = (findViewById<TextView>(R.id.intervalDurationTxt)).text

        val groupUUID = mSelectedIntervalGroup?.group
        val interval = if(groupUUID != null){
            val childCount = mSelectedGroupChildSize
            IntervalData(
                    label=text.toString(),
                    duration = durationText.toString().toLong(),
                    group = groupUUID,
                    ownerOfGroup = false,
                    groupPosition = childCount)
        }else{
            //Toast.makeText(this, "Invalid UUID. Setting to random", Toast.LENGTH_SHORT).show()
            IntervalData(label=text.toString(), duration = durationText.toString().toLong(),groupPosition = mGroupOwnersSize!!.value!!)
        }

        mModelProvider.insert(interval)
        mUpdatedInterval = true
        Toast.makeText(this, "Added interval", Toast.LENGTH_SHORT).show()
    }



    override fun onItemSelected(interval: IntervalData, isSelected: Boolean) {
        if(isSelected) {
            if(mSelectedIntervalGroup != null){
                mModelProvider.getChildSizeOfGroup(interval.group).removeObservers(this)
            }
            mSelectedIntervalGroup = interval
            mModelProvider.getChildSizeOfGroup(interval.group).observe(this, android.arch.lifecycle.Observer {
                mSelectedGroupChildSize = it ?: 0
            })
        }else{
            mSelectedIntervalGroup =  null
            mSelectedGroupChildSize = 0
        }
    }

    private class DurationGestureDetector : GestureDetector.SimpleOnGestureListener() {
        var direction: Int = 1
        var mDurationTextView: TextView? = null
        var mDuration = 0L
        private val DEBUG_TAG = "IADTxtVGesture"
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
