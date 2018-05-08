package io.github.crr0004.intervalme

import android.os.SystemClock
import android.view.MotionEvent
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.views.IntervalClockView
import java.util.concurrent.TimeUnit
import android.util.Log
import android.view.GestureDetector
import android.support.v4.view.GestureDetectorCompat


/**
 * A controller for dealing with the logic between the IntervalClockView and a data source
 * @param mNextInterval the interval to start after this one is done
 * @param mClockView the view that this controller uses
 * @param childOfInterval the interval data from the db
 */
class IntervalController(
        private val mClockView: IntervalClockView,
        val childOfInterval: IntervalData,
        private val mNextInterval: IntervalController? = null):
        GestureDetector.SimpleOnGestureListener() {

    private var mClockRunning = false
    private val DEBUG_TAG = "ICGestures"
    private var mDetector: GestureDetectorCompat
    private var mClockTickRunnable: TickClockRunnable


    init {
        mDetector = GestureDetectorCompat(mClockView.context, this)
        mClockView.setOnTouchListener{ _, event ->
            mDetector.onTouchEvent(event) }
        mClockView.setClockTime(TimeUnit.SECONDS.toMillis(childOfInterval.duration))
        mClockTickRunnable = TickClockRunnable(mClockView, TimeUnit.SECONDS.toMillis(childOfInterval.duration), this)
        if(childOfInterval.runningDuration > 0) {
            mClockTickRunnable.timeToRun = childOfInterval.runningDuration
            mClockView.setClockTime(mClockTickRunnable.timeToRun)
        }

    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onSingleTap called")


        // We've started the clock for the first time
        if(!mClockRunning) {
            startClockAsNew()
        }else{
            // The clock has already been started so now we just invert the runnable
            mClockTickRunnable.mRunning = !mClockTickRunnable.mRunning
            if(mClockTickRunnable.mRunning){
                // The clock was paused so we need to start it again
                // We also move the clock ahead to the current time minus however much time has passed
                mClockTickRunnable.mStartingTime = SystemClock.elapsedRealtime() - mClockTickRunnable.mElapsedTime
                mClockView.post(mClockTickRunnable)
            }
        }
        return true
    }

    private fun startClockAsNew() {
        mClockTickRunnable.reset()
        mClockTickRunnable.mRunning = true
        mClockView.postDelayed(mClockTickRunnable, 100)
        mClockRunning = true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTap called")
        // We stop the clock
        mClockRunning = false
        mClockTickRunnable.mRunning = false
        mClockView.mPercentageComplete = 0f
        mClockTickRunnable.timeToRun = TimeUnit.SECONDS.toMillis(childOfInterval.duration)
        mClockView.setClockTime(mClockTickRunnable.timeToRun)

        return true
    }

    override fun onContextClick(e: MotionEvent?): Boolean {
        Log.d(DEBUG_TAG, "onContextClick called")
        return super.onContextClick(e)
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(DEBUG_TAG, "onLongPress called")
        super.onLongPress(e)
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        Log.d(DEBUG_TAG, "onScroll called")
        mClockTickRunnable.timeToRun -= distanceX.toLong() * 100
        mClockView.setClockTime(mClockTickRunnable.timeToRun)
        mClockTickRunnable.updatePercentComplete()
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    /**
     * Called when the timer has finished and the next one is to begin
     */
    private fun finishedTimer() {
        mClockRunning = false
        if(mNextInterval != null) {
            mNextInterval.previousTimerFinished(this)
        }
    }

    private fun previousTimerFinished(previousIntervalController: IntervalController){
        startClockAsNew()
    }

    fun onPause() {
        childOfInterval.runningDuration = mClockTickRunnable.timeToRun
    }

    private class TickClockRunnable(
            val mClockView: IntervalClockView,
            var timeToRun: Long,
            private val mIntervalController: IntervalController) : Runnable{

        var mStartingTime = SystemClock.elapsedRealtime()
        var mElapsedTime = 0L


        var mRunning: Boolean = true

        override fun run() {
            mElapsedTime = SystemClock.elapsedRealtime() - mStartingTime
            if(timeToRun - mElapsedTime >= 100f && mRunning) {
                updatePercentComplete()
                mClockView.setClockTime(timeToRun - mElapsedTime)
                mClockView.postDelayed(this, 100)
            }else if(mRunning){
                mClockView.mPercentageComplete = 1f
                mClockView.setClockTime(0)
                mIntervalController.finishedTimer()
                mRunning = false
            }
        }
        fun reset(){
            mStartingTime = SystemClock.elapsedRealtime()
        }
        fun updatePercentComplete(){
            mClockView.mPercentageComplete = mElapsedTime.toFloat()/timeToRun
        }

    }
}