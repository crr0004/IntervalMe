package io.github.crr0004.intervalme

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
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import java.util.*

class IntervalAddActivity : AppCompatActivity() {

    private var mDuration: Int = 0
    private var mDurationTextView: EditText? = null
    private val DEBUG_TAG = "IntervalAdd"
    private var mGestureDetector: GestureDetectorCompat? = null
    private val mDurationGestureDetector: DurationGestureDetector = DurationGestureDetector()
    private var mIntervalToEditID: Long = -1
    private var mIntervalToEdit: IntervalData? = null

    companion object {
        const val EDIT_MODE_FLAG_ID = "edit_mode"
        const val EDIT_MODE_FLAG_INTERVAL_ID = "edit_mode_interval_id"
    }


    private var mUpdatedInterval: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interveraladd)

        (findViewById<View>(R.id.goToListBtn)).setOnClickListener {
            val intent = Intent()
            if(mIntervalToEdit != null && mUpdatedInterval) {
                intent.putExtra(EDIT_MODE_FLAG_INTERVAL_ID, mIntervalToEdit!!.id)
            }
            setResult(RESULT_OK, intent)
            mUpdatedInterval = false
            finish()
        }

        mDurationTextView = findViewById(R.id.intervalDurationTxt)
        mGestureDetector = GestureDetectorCompat(this.applicationContext, mDurationGestureDetector)

        findViewById<Button>(R.id.intervalAddBtn).setOnClickListener(addIntervalListener)
        findViewById<Button>(R.id.goToClockSampleBtn).setOnClickListener(clockSampleBtnListener)


        mDurationGestureDetector.mDurationTextView = mDurationTextView
        findViewById<View>(R.id.increaseDurationBtn).setOnTouchListener { v, event ->
            mDurationGestureDetector.direction = 1

            val results = mGestureDetector!!.onTouchEvent(event)
            if(event?.action == MotionEvent.ACTION_UP){
                mDurationGestureDetector.onUp(event)
            }
            results
        }
        findViewById<View>(R.id.decreaseDurationBtn).setOnTouchListener { v, event ->
            mDurationGestureDetector.direction = -1
            val results = mGestureDetector!!.onTouchEvent(event)
            if(event?.action == MotionEvent.ACTION_UP){
                mDurationGestureDetector.onUp(event)
            }
            results
        }

        mDurationTextView?.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                val seconds = v.text.toString().toLong()
                mDurationGestureDetector.mDuration = seconds
                v.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        val isEditMode = intent.getBooleanExtra(EDIT_MODE_FLAG_ID, false)
        if(isEditMode){
            mIntervalToEditID = intent.getLongExtra(EDIT_MODE_FLAG_INTERVAL_ID, -1)
            mIntervalToEdit = IntervalMeDatabase.getInstance(this.applicationContext)!!.intervalDataDao().get(mIntervalToEditID)
            if(mIntervalToEdit != null) {
                mDurationTextView?.setText(mIntervalToEdit?.duration.toString())
                findViewById<TextView>(R.id.intervalNameTxt).text = mIntervalToEdit?.label
                if (!mIntervalToEdit!!.ownerOfGroup)
                    findViewById<TextView>(R.id.intervalParentTxt).text = mIntervalToEdit?.group.toString()
                findViewById<Button>(R.id.intervalAddBtn).setOnClickListener(addIntervalEditModeListener)
                findViewById<Button>(R.id.intervalAddBtn).text = resources.getString(R.string.update)
                mDurationGestureDetector.mDuration = mIntervalToEdit!!.duration
                mDurationGestureDetector.updateDurationText(0) // Ensures text is formatted correctly
            }

        }

    }

    private val addIntervalEditModeListener = fun(v: View){
        if(mIntervalToEdit != null) {
            val text = (findViewById<TextView>(R.id.intervalNameTxt)).text
            val durationText = (findViewById<TextView>(R.id.intervalDurationTxt)).text
            val groupText = (findViewById<TextView>(R.id.intervalParentTxt)).text

            mIntervalToEdit!!.duration = durationText.toString().toLong()
            mIntervalToEdit!!.label = text.toString()
            try {
                val groupUUID = UUID.fromString(groupText.toString())
                mIntervalToEdit!!.group = groupUUID
                mIntervalToEdit!!.ownerOfGroup = false
            }catch (e: IllegalArgumentException){

            }

            IntervalMeDatabase.getInstance(this.applicationContext)!!.intervalDataDao().update(mIntervalToEdit!!)


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
        val groupText = (findViewById<TextView>(R.id.intervalParentTxt)).text

        val interval: IntervalData
        interval = try {
            val groupUUID = UUID.fromString(groupText.toString())
            IntervalData(label=text.toString(), duration = durationText.toString().toLong(),group = groupUUID,ownerOfGroup = false)
        }catch (e: IllegalArgumentException){
            //Toast.makeText(this, "Invalid UUID. Setting to random", Toast.LENGTH_SHORT).show()
            IntervalData(label=text.toString(), duration = durationText.toString().toLong())
        }

        IntervalMeDatabase.getInstance(this.applicationContext)!!.intervalDataDao().insert(interval)
        Toast.makeText(this, "Added interval", Toast.LENGTH_SHORT).show()
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
