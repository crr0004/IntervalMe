package io.github.crr0004.intervalme.interval

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.views.IntervalClockView
import java.util.concurrent.TimeUnit


/**
 * A mController for dealing with the logic between the IntervalClockView and a data source
 */
// We can suppress this because we ensure click logic is in onClickListener
@SuppressLint("ClickableViewAccessibility")
open class IntervalController
/**
 * @param mClockView the view that this mController uses
 * @param mChildOfInterval the interval data from the db
 */(mClockView: IntervalClockView? = null, mChildOfInterval: IntervalData, runProperties: IntervalRunProperties? = null, applicationContext: Context? = null, callBackHost: IntervalControllerCallBackI) : GestureDetector.SimpleOnGestureListener() {

    private var mClockRunning = false
    companion object {
        const val DEBUG_TAG = "ICGestures"
    }
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var mClockTickRunnable: TickClockRunnable
    private var mCallBackHost: IntervalControllerCallBackI = callBackHost

    interface IntervalControllerCallBackI {
        fun clockStartedAsNew(intervalController: IntervalController)
        fun clockResumedFromPause(intervalController: IntervalController)
        fun clockPaused(intervalController: IntervalController)
        fun clockFinished(intervalController: IntervalController, mSoundController: IntervalSoundController?)
        fun clockStopped(intervalController: IntervalController)
        fun clockTimeUpdatedTo(intervalController: IntervalController, mTimeToRun: Long)

    }

    private var mClockView: IntervalClockView? = null
    lateinit var mChildOfInterval: IntervalData
    private var mIntervalProperties: IntervalRunProperties? = null
    //private var mNextInterval: IntervalController? = null
    private var mSoundController: IntervalSoundController? = null
    private var mThread: Thread? = null

    init {
        init(mClockView, mChildOfInterval, runProperties = runProperties, applicationContext = applicationContext)
    }

    private fun init(
            clockView: IntervalClockView?,
            childOfInterval: IntervalData,
            applicationContext: Context? = null,
            runProperties: IntervalRunProperties? = null) {
        mClockView = clockView
        mChildOfInterval = childOfInterval
        mDetector = GestureDetectorCompat(applicationContext, this)
        mIntervalProperties = runProperties



        mClockView?.setOnTouchListener { _, event ->
            mDetector.onTouchEvent(event)
        }

        // We do this so accessibility has correct logic
        mClockView?.setOnClickListener {
            clickOnClock()
        }
        mClockView?.setClockTime(TimeUnit.SECONDS.toMillis(childOfInterval.duration))
            mClockTickRunnable = TickClockRunnable(mClockView, TimeUnit.SECONDS.toMillis(childOfInterval.duration), this)
        if (childOfInterval.runningDuration > 0) {
            mClockTickRunnable.mTimeToRun = childOfInterval.runningDuration
            mClockView?.setClockTime(mClockTickRunnable.mTimeToRun)
        }
        //Only create new sound mController if it's been previously released
        if(mSoundController == null && applicationContext != null)
            mSoundController = IntervalSoundController.instanceWith(applicationContext.applicationContext, R.raw.digital_watch_alarm_1)
    }

    private fun clickOnClock(){
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
                mCallBackHost.clockResumedFromPause(this)
            }else{
                mThread?.interrupt()
                mCallBackHost.clockPaused(this)
            }
            startClockThread()
        }
    }

    /* This may still be needed in the future
    open fun disconnectFromViews(){
        mClockView?.setOnTouchListener(null)
        mClockTickRunnable.releaseClockView()
    }
    */

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        // We've started the clock for the first time
        mClockView?.performClick()
        return true
    }

    @Synchronized
    private fun startClockThread(){
        mThread = Thread(mClockTickRunnable, mChildOfInterval.label)
        try {
            mThread?.start()
        }catch (e: IllegalThreadStateException){
            Log.d(DEBUG_TAG, "Tried to start a bad thread")
        }
    }

    fun startClockAsNew() {
        stopAndRefreshClock()
        mClockTickRunnable.mRunning = true
        startClockThread()
        mClockRunning = true
        mCallBackHost.clockStartedAsNew(this)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        // We stop the clock
        stopAndRefreshClock()
        mClockView?.mPercentageComplete = 0f
        mClockView?.setClockTime(mClockTickRunnable.mTimeToRun)

        return true
    }

    open fun stopAndRefreshClock() {
        mClockRunning = false
        mClockTickRunnable.mRunning = false
        mClockTickRunnable.reset()
        mThread?.interrupt()

        mClockTickRunnable.mTimeToRun = TimeUnit.SECONDS.toMillis(mChildOfInterval.duration)
        //mCallBackHost.clockStopped(this)

        //mClockView?.setClockTime(mClockTickRunnable.mTimeToRun)
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        /*
        mClockTickRunnable.mTimeToRun -= distanceX.toLong() * 100
        mClockView!!.setClockTime(mClockTickRunnable.mTimeToRun)
        if(mClockRunning) {
            mClockTickRunnable.updatePercentComplete()
        }
        mCallBackHost.clockTimeUpdatedTo(this, mClockTickRunnable.mTimeToRun)
        */
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

        //Log.d(DEBUG_TAG, "IntervalController $mChildOfInterval done")

        mCallBackHost.clockFinished(this, mSoundController)
    }

    open fun connectNewClockView(view: View) {
        val attached = false
        Log.d("IC", "Connecting " + Integer.toHexString(System.identityHashCode(view)) + " is attached $attached")
        if(view.id == R.id.intervalClockView) {

            mClockView?.setOnTouchListener(null)
            mClockView?.setOnClickListener(null)
            mClockView?.mPercentageComplete = 0f
            mClockView?.setClockTime(0)

            mClockView = view as IntervalClockView


            mClockTickRunnable.connectNewClock(view)
            mClockView!!.setOnTouchListener { _, event ->
                mDetector.onTouchEvent(event)
            }
            mClockView!!.setOnClickListener {
                clickOnClock()
            }
        }


    }

    open fun updateViewToProperties(properties: IntervalRunProperties) {
        // this currently doesn't do anything for child view
    }

    private class TickClockRunnable(
            @get:Synchronized @set:Synchronized
            var mClockView: IntervalClockView?,
            @get:Synchronized @set:Synchronized
            var mTimeToRun: Long,
            private val mIntervalController: IntervalController) : Runnable{

        @get:Synchronized @set:Synchronized
        var mStartingTime = SystemClock.elapsedRealtime()

        @get:Synchronized @set:Synchronized
        var mElapsedTime = 0L

        @get:Synchronized @set:Synchronized
        var mRunning: Boolean = true

        private var mUpdateClockHandler: Handler? = null

        private val updateClock = Runnable{
            updatePercentComplete()
            if(mTimeToRun - mElapsedTime > 100f) {
                mClockView?.setClockTime(mTimeToRun - mElapsedTime)
            }else{
                mClockView?.setClockTime(0)
                //mUpdateClockHandler?.removeCallbacks(this)
            }
        }

        fun connectNewClock(clockView: IntervalClockView){
            mUpdateClockHandler?.removeCallbacks(updateClock)
            this.mClockView = clockView
            updatePercentComplete()
            mUpdateClockHandler = mClockView!!.handler
            mClockView!!.setClockTime(mTimeToRun - mElapsedTime)

        }

        override fun run() {
            while(mRunning) {
                mElapsedTime = SystemClock.elapsedRealtime() - mStartingTime
                if (mTimeToRun - mElapsedTime >= 100f && mRunning) {
                    mUpdateClockHandler?.post(updateClock)
                    try {
                        Thread.sleep(33)
                    }catch(e: InterruptedException){

                    }
                } else if (mRunning) {
                    //mUpdateClockHandler?.removeCallbacks(updateClock)
                    mUpdateClockHandler?.post(updateClock)
                    mIntervalController.finishedTimer()
                    mRunning = false
                }
            }

        }
        fun reset(){
            mStartingTime = SystemClock.elapsedRealtime()
            mElapsedTime = 0
        }

        fun updatePercentComplete(){
            if(mTimeToRun - mElapsedTime >= 100f) {
                mClockView?.mPercentageComplete = mElapsedTime.toFloat() / mTimeToRun
            }else{
                mClockView?.mPercentageComplete = 1f
            }
        }
    }
}