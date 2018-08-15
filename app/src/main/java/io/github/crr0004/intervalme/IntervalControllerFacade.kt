package io.github.crr0004.intervalme

import android.content.Context
import android.util.Log
import android.view.View
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.views.IntervalClockView
import java.util.*
import kotlin.collections.HashMap

class IntervalControllerFacade : IntervalController.IntervalControllerCallBackI {

    private val mControllers: HashMap<UUID, Array<IntervalController>> = HashMap(1)
    private val mRunningProperties: HashMap<UUID, IntervalRunProperties> = HashMap(1)
    private lateinit var mDataSource: IntervalControllerFacade.IntervalControllerDataSourceI

    fun intervalsSwapped(id: Long, id1: Long) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("ICF", "IntervalSwapped called")
    }

    fun groupView(groupPosition: Int, toReturn: View) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setUpGroupOrder(groupPosition: Int, context: Context) {
        // We add one to the size as the first one is the group itself
        mControllers[mDataSource.facadeGetIDFromPosition(groupPosition)] = Array(mDataSource.facadeGetGroupSize(groupPosition)+1){index: Int ->
            if(index == 0){
                IntervalController(mChildOfInterval = mDataSource.facadeGetGroup(groupPosition), callBackHost = this, applicationContext = context)
            }else {
                IntervalController(mChildOfInterval = mDataSource.facadeGetChild(groupPosition, index-1), callBackHost = this, applicationContext = context)
            }
        }
    }

    fun connectClockView(clockView: IntervalClockView, groupPosition: Int, childOfInterval: IntervalData) {
        mControllers[mDataSource.facadeGetIDFromPosition(groupPosition)]!![childOfInterval.groupPosition.toInt()+1].connectNewClockView(clockView)
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

    /**
     * This creates a copy of properties for the group and stores it
     * This will not create a copy if the properites doesn't exist in the data source
     * @param the properties of the group
     * @return a copy of the properties
     */
    fun getRunningProperties(group: UUID): IntervalRunProperties?{
        var properties = mRunningProperties[group]
        if(properties == null) {
            properties = mDataSource.getGroupProperties(mControllers[group]!![0].mChildOfInterval.id)
            if(properties != null) {
                properties = IntervalRunProperties(properties)
                mRunningProperties[group] = properties
            }
        }
        return properties
    }

    fun clearRunningPropertie(group: UUID){
        mRunningProperties.remove(group)
    }

    override fun clockFinished(intervalController: IntervalController, mSoundController: IntervalSoundController?) {
        val interval = intervalController.mChildOfInterval
        if(isIntervalLast(interval)) {
            mSoundController?.playLoop(2)
            val properties = getRunningProperties(interval.group)
            if(properties != null){
                if(properties.loops > 0){
                    properties.loops--
                    // 1 because we want to start the first child again. 0 is the group itself
                    mControllers[interval.group]!![1].startClockAsNew()
                }else{
                    clearRunningPropertie(interval.group)
                }
            }
        }else{
            mSoundController?.playDone()
            // +2 because we want the interval following this one and the first element of the controllers is the group itself
            mControllers[interval.group]!![(interval.groupPosition+2).toInt()].startClockAsNew()
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
        fun getGroupProperties(groupPosition: Long): IntervalRunProperties?
    }
}