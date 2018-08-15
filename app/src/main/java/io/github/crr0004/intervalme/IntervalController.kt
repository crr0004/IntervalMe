package io.github.crr0004.intervalme

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties
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
    private lateinit var mCallBackHost: IntervalControllerCallBackI

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
    var mIntervalProperties: IntervalRunProperties? = null
    //private var mNextInterval: IntervalController? = null
    private var mSoundController: IntervalSoundController? = null
    private var mThread: Thread? = null

    /**
     * @param mNextInterval the interval to start after this one is done
     * @param mClockView the view that this mController uses
     * @param mChildOfInterval the interval data from the db
     */
    constructor(mClockView: IntervalClockView? = null,
                mChildOfInterval: IntervalData,
                runProperties: IntervalRunProperties? = null,
                applicationContext: Context? = null,
                callBackHost: IntervalController.IntervalControllerCallBackI) {
        init(mClockView, mChildOfInterval, runProperties = runProperties, applicationContext = applicationContext)
        mCallBackHost = callBackHost
    }

    fun init(
            clockView: IntervalClockView?,
            childOfInterval: IntervalData,
            applicationContext: Context? = null,
            runProperties: IntervalRunProperties? = null) {
        mClockView = clockView
        mChildOfInterval = childOfInterval
        mDetector = GestureDetectorCompat(mClockView?.context, this)
        mIntervalProperties = runProperties

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
        if(mSoundController == null && applicationContext != null)
            mSoundController = IntervalSoundController.instanceWith(applicationContext.applicationContext,R.raw.digital_watch_alarm_1)
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
                mCallBackHost.clockResumedFromPause(this)
            }else{
                mThread?.interrupt()
                mCallBackHost.clockPaused(this)
            }
            startClockThread()
        }
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

    public fun startClockAsNew() {
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
        mCallBackHost.clockStopped(this)

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

    override fun onLongPress(e: MotionEvent?) {
        super.onLongPress(e)
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

    private fun previousTimerFinished(previousIntervalController: IntervalController){
        startClockAsNew()
    }

    open fun onPause() {
        mChildOfInterval.runningDuration = mClockTickRunnable.mTimeToRun
        if(mSoundController != null)
            IntervalSoundController.release(mSoundController!!)
        mSoundController = null
    }

    fun onResume(context: Context){
        mSoundController = IntervalSoundController.instanceWith(context.applicationContext, R.raw.digital_watch_alarm_1)
    }

    open fun onStop() {

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
            }
        }

        fun connectNewClock(clockView: IntervalClockView){
            mClockView = clockView
            mClockView?.setClockTime(mTimeToRun - mElapsedTime)
            updatePercentComplete()
            mUpdateClockHandler = mClockView?.handler

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
        @Synchronized
        fun updatePercentComplete(){
            if(mTimeToRun - mElapsedTime >= 100f) {
                mClockView?.mPercentageComplete = mElapsedTime.toFloat() / mTimeToRun
            }else{
                mClockView?.mPercentageComplete = 1f
            }
        }

        fun releaseClockView(){
            mClockView =null
            mUpdateClockHandler = null
        }
    }
}