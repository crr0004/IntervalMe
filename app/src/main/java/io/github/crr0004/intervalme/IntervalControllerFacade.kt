package io.github.crr0004.intervalme

import android.util.Log
import android.view.View
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.views.IntervalClockView
import java.util.*

class IntervalControllerFacade : IntervalController.IntervalControllerCallBackI {

    private val mControllers: HashMap<UUID, Array<IntervalController>> = HashMap(1)
    private lateinit var mDataSource: IntervalControllerFacade.IntervalControllerDataSourceI

    fun intervalsSwapped(id: Long, id1: Long) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("ICF", "IntervalSwapped called")
    }

    fun groupView(groupPosition: Int, toReturn: View) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setUpGroupOrder(groupPosition: Int) {
        mControllers[mDataSource.facadeGetIDFromPosition(groupPosition)] = Array(mDataSource.facadeGetGroupSize(groupPosition)){index: Int ->
            IntervalController(mChildOfInterval = mDataSource.facadeGetChild(groupPosition, index), callBackHost = this)
        }
    }

    fun connectClockView(clockView: IntervalClockView, groupPosition: Int, childOfInterval: IntervalData) {
        mControllers[mDataSource.facadeGetIDFromPosition(groupPosition)]!![childOfInterval.groupPosition.toInt()].connectNewClockView(clockView)
    }

    fun delete(childOfInterval: IntervalData) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setIntervalAsLast(groupPosition: Int, childOfInterval: IntervalData) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun startAllIntervals() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onPauseCalled(intervalListActivity: IntervalListActivity) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onResumeCalled(intervalListActivity: IntervalListActivity) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onStopCalled(intervalListActivity: IntervalListActivity) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun isIntervalLast(interval: IntervalData): Boolean{
        return mControllers[interval.group]!!.last().mChildOfInterval == interval
    }

    // BEGIN IntervalControllerCallBackI
    override fun clockStartedAsNew(intervalController: IntervalController) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clockResumedFromPause(intervalController: IntervalController) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clockPaused(intervalController: IntervalController) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clockFinished(intervalController: IntervalController, mSoundController: IntervalSoundController?) {
        val interval = intervalController.mChildOfInterval
         if(isIntervalLast(interval)) {
            mSoundController?.playLoop(2)
        }else{
            mSoundController?.playDone()
            mControllers[interval.group]!![(interval.groupPosition+1).toInt()].startClockAsNew()
        }
    }

    override fun clockStopped(intervalController: IntervalController) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clockTimeUpdatedTo(intervalController: IntervalController, mTimeToRun: Long) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //END IntervalControllerCallBackI

    fun setDataSource(source: IntervalControllerDataSourceI){
        mDataSource = source
    }

    fun destory(){
        IntervalControllerFacade.mInstance = null
    }

    companion object {

        private var mInstance: IntervalControllerFacade? = null

        val instance: IntervalControllerFacade
            get() {
                if(mInstance == null){
                    mInstance = IntervalControllerFacade()
                }

            return mInstance!!
        }
    }

    interface IntervalControllerDataSourceI{
        fun facadeGetGroup(groupPosition: Int): IntervalData
        fun facadeGetGroupSize(groupPosition: Int): Int
        fun facadeGetChild(groupPosition: Int, index: Int): IntervalData
        fun facadeGetIDFromPosition(groupPosition: Int): UUID
    }
}