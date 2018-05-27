package io.github.crr0004.intervalme

import android.os.SystemClock
import android.support.v4.view.GestureDetectorCompat
import android.view.GestureDetector
import android.view.MotionEvent
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.views.IntervalClockView
import java.util.concurrent.TimeUnit


/**
 * A mController for dealing with the logic between the IntervalClockView and a data source
 */
open class IntervalController:GestureDetector.SimpleOnGestureListener {

    private var mClockRunning = false
    private val DEBUG_TAG = "ICGestures"
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var mClockTickRunnable: TickClockRunnable
    private var mClockView: IntervalClockView? = null
    lateinit var mChildOfInterval: IntervalData
    private var mNextInterval: IntervalController? = null
    private var mSoundController: IntervalSoundController? = null


    /**
     * @param mNextInterval the interval to start after this one is done
     * @param mClockView the view that this mController uses
     * @param mChildOfInterval the interval data from the db
     */
    constructor(mClockView: IntervalClockView? = null,
                mChildOfInterval: IntervalData,
                mNextInterval: IntervalController? = null) {
        init(mClockView, mChildOfInterval, mNextInterval)
    }

    fun init(clockView: IntervalClockView?, childOfInterval: IntervalData, nextInterval: IntervalController? = null) {
        mClockView = clockView
        mChildOfInterval = childOfInterval
        mNextInterval = nextInterval
        mDetector = GestureDetectorCompat(mClockView?.context, this)

        mClockView?.setOnTouchListener { _, event ->
            mDetector.onTouchEvent(event)
        }

        mClockView?.setClockTime(TimeUnit.SECONDS.toMillis(childOfInterval.duration))
            mClockTickRunnable = TickClockRunnable(mClockView, TimeUnit.SECONDS.toMillis(childOfInterval.duration), this)
        if (childOfInterval.runningDuration > 0) {
            mClockTickRunnable.mTimeToRun = childOfInterval.runningDuration
            mClockView?.setClockTime(mClockTickRunnable.mTimeToRun)
        }
        //Only create new sound mController if it's been previously released
        //if(mSoundController == null)
            //mSoundController = IntervalSoundController(clockView.context,R.raw.digital_watch_alarm_1)
    }

    open fun disconnectFromViews(){
        mClockView?.setOnTouchListener(null)
        mClockTickRunnable.releaseClockView()
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
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
               // mClockView!!.post(mClockTickRunnable)
            }
        }
        return true
    }

    private fun startClockAsNew() {
        mClockTickRunnable.reset()
        mClockTickRunnable.mRunning = true
        Thread(mClockTickRunnable, mChildOfInterval.label).start()
        mClockRunning = true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        // We stop the clock
        stopAndRefreshClock()

        return true
    }

    open fun stopAndRefreshClock() {
        mClockRunning = false
        mClockTickRunnable.mRunning = false
        mClockView!!.mPercentageComplete = 0f
        mClockTickRunnable.mTimeToRun = TimeUnit.SECONDS.toMillis(mChildOfInterval.duration)
        mClockView!!.setClockTime(mClockTickRunnable.mTimeToRun)
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        mClockTickRunnable.mTimeToRun -= distanceX.toLong() * 100
        mClockView!!.setClockTime(mClockTickRunnable.mTimeToRun)
        if(mClockRunning) {
            mClockTickRunnable.updatePercentComplete()
        }
        return true
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
            mNextInterval!!.previousTimerFinished(this)
        }
        mSoundController?.playDone()
    }

    private fun previousTimerFinished(previousIntervalController: IntervalController){
        startClockAsNew()
    }

    open fun onPause() {
        mChildOfInterval.runningDuration = mClockTickRunnable.mTimeToRun
    }

    open fun onStop() {
        mSoundController?.release()
        mSoundController = null
    }

    open fun setNextInterval(intervalController: IntervalController?) {
        mNextInterval = intervalController
    }

    open fun refreshInterval(updatedInterval: IntervalData) {
        mChildOfInterval = updatedInterval

    }

    open fun connectNewClockView(clockView: IntervalClockView) {
        mClockView = clockView

        mClockTickRunnable.connectNewClock(clockView)
        mClockView!!.setOnTouchListener { _, event ->
            mDetector.onTouchEvent(event)
        }


    }

    private class TickClockRunnable(
            var mClockView: IntervalClockView?,
            var mTimeToRun: Long,
            private val mIntervalController: IntervalController) : Runnable{

        var mStartingTime = SystemClock.elapsedRealtime()
        var mElapsedTime = 0L


        var mRunning: Boolean = true

        fun connectNewClock(clockView: IntervalClockView){
            mClockView = clockView
            mClockView?.setClockTime(mTimeToRun - mElapsedTime)
            updatePercentComplete()
        }

        override fun run() {
            while(mRunning) {
                mElapsedTime = SystemClock.elapsedRealtime() - mStartingTime
                if (mTimeToRun - mElapsedTime >= 100f && mRunning) {


                    mClockView?.post( {
                        updatePercentComplete()
                        mClockView?.setClockTime(mTimeToRun - mElapsedTime)
                    })
                    Thread.sleep(33)
                } else if (mRunning) {

                    mClockView?.post({
                        updatePercentComplete()
                        mClockView?.setClockTime(0)
                    })
                    mIntervalController.finishedTimer()
                    mRunning = false
                }
            }
        }
        fun reset(){
            mStartingTime = SystemClock.elapsedRealtime()
        }
        fun updatePercentComplete(){
            if(mTimeToRun - mElapsedTime >= 100f) {
                mClockView?.mPercentageComplete = mElapsedTime.toFloat() / mTimeToRun
            }else{
                mClockView?.mPercentageComplete = 1f
            }
        }

        fun releaseClockView(){
            mClockView =null
        }
    }
}