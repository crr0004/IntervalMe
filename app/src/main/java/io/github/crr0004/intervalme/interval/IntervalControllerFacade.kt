package io.github.crr0004.intervalme.interval

import android.content.Context
import android.util.Log
import android.view.View
import io.github.crr0004.intervalme.BuildConfig
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsDataSourceI
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsRepository
import io.github.crr0004.intervalme.views.IntervalClockView
import java.util.*
import kotlin.collections.HashMap

/**
 * A singleton facade that has an easy to use API for controller and managing the interval controllers.
 * This facade connects the interval controllers, the interval properties and data sources together
 * for controlling behaviour of the intervals.
 *
 * When using this facade you just need to connect the data sources with an appropriate implementation
 * @see IntervalControllerFacade.setDataSource
 * @see IntervalControllerFacade.setAnalyticsDataSource
 */
class IntervalControllerFacade : IntervalController.IntervalControllerCallBackI {

    private val mControllers: HashMap<UUID, Array<IntervalController>> = HashMap(1)
    private val mRunningProperties: HashMap<UUID, IntervalRunProperties> = HashMap(1)
    private lateinit var mDataSource: IntervalControllerDataSourceI
    private lateinit var mAnalyticsDataSource: IntervalAnalyticsDataSourceI

    fun intervalsSwapped(id: Long, id1: Long) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("ICF", "IntervalSwapped called")
    }

    fun groupView(groupPosition: Int, toReturn: View) {
        mControllers[mDataSource.facadeGetIDFromPosition(groupPosition)]?.get(0)?.
                connectNewClockView(toReturn)
    }

    /**
     * Sets up an intervals group controllers. This is a heavy call so be careful to only cal when needed
     */
    fun setUpGroupOrder(groupPosition: Int, context: Context) {
        // We add one to the size as the first one is the group itself
        mControllers[mDataSource.facadeGetIDFromPosition(groupPosition)] = Array(mDataSource.facadeGetGroupSize(groupPosition)+1){index: Int ->
            if(index == 0){
                IntervalGroupController(mChildOfInterval = mDataSource.facadeGetGroup
                (groupPosition), callBackHost = this, applicationContext = context)
            }else {
                IntervalController(mChildOfInterval = mDataSource.facadeGetChild(groupPosition, index - 1), callBackHost = this, applicationContext = context)
            }
        }
    }

    fun connectClockView(clockView: IntervalClockView, groupPosition: Int, childOfInterval: IntervalData) {
        // Gets the id (uuid) of a group from its position, then the child by the position within the group
        // then connects a new view to the controller
        if(BuildConfig.DEBUG && childOfInterval.groupPosition + 1 >= mControllers[mDataSource.facadeGetIDFromPosition(groupPosition)]?.size ?: 0){
            val childPos = childOfInterval.groupPosition + 1
            Log.d("ICF", "Trying to get a child at $childPos of group $groupPosition that doesn't have a controller")
        }
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

    /**
     * Compares the last interval controller in the parameter interval group to see if it is last
     * in the group
     * @param interval the interval to check to see if it is last in its group
     */
    fun isIntervalLast(interval: IntervalData): Boolean{
        return mControllers[interval.group]!!.last().mChildOfInterval == interval
    }

    // BEGIN IntervalControllerCallBackI
    override fun clockStartedAsNew(intervalController: IntervalController) {
        updateProperties(intervalController.mChildOfInterval.group)
    }

    /**
     * Causes a view update to match the groups properties
     */
    private fun updateProperties(group: UUID){
        val properties = getRunningProperties(group)
        if(properties != null)
            mControllers[group]!![0].updateViewToProperties(properties)
    }

    override fun clockResumedFromPause(intervalController: IntervalController) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("ICF", "clockResumedFromPause called")
    }

    override fun clockPaused(intervalController: IntervalController) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        Log.d("ICF", "clockPaused called")
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

    fun clearRunningProperties(group: UUID){
        mRunningProperties.remove(group)
    }

    override fun clockFinished(intervalController: IntervalController, mSoundController: IntervalSoundController?) {
        val interval = intervalController.mChildOfInterval
        mAnalyticsDataSource.intevalFinish(interval)
        if(isIntervalLast(interval)) {
            mSoundController?.playLoop(2)
            val properties = getRunningProperties(interval.group)
            if(properties != null){

                if(properties.loops > 0){
                    properties.loops--
                    // 1 because we want to start the first child again. 0 is the group itself
                    mControllers[interval.group]!![1].startClockAsNew()
                }else{
                    // Register the group has done a full run
                    mAnalyticsDataSource.intevalFinishWithProperties(mControllers[interval.group]!![0].mChildOfInterval,
                            mDataSource.getGroupProperties(mControllers[interval.group]!![0].mChildOfInterval.id)!!)
                    clearRunningProperties(interval.group)
                }
                // We call another getRunningProperties because the properties may have been cleared
                // and this will cause it to return to the original value
                mControllers[interval.group]!![0].
                        updateViewToProperties(properties)
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

    /**
     * This needs to be set before you can use this facade
     */
    fun setDataSource(source: IntervalControllerDataSourceI){
        mDataSource = source
    }

    fun destroy(){
        mInstance = null
    }

    /**
     * This needs to be set before you can use this facade
     */
    fun setAnalyticsDataSource(analyticsRepository: IntervalAnalyticsRepository) {
        this.mAnalyticsDataSource = analyticsRepository
    }

    fun groupExpanded(pos: Int) {
        updateProperties(mDataSource.facadeGetIDFromPosition(pos))
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
        /**
         * Get a interval group by its position
         */
        fun facadeGetGroup(groupPosition: Int): IntervalData

        /**
         * Get a size of a group by its position
         */
        fun facadeGetGroupSize(groupPosition: Int): Int

        /**
         * Get a interval by its group position and index within the group
         */
        fun facadeGetChild(groupPosition: Int, index: Int): IntervalData

        /**
         * Get a group id (UUID) by its position
         */
        fun facadeGetIDFromPosition(groupPosition: Int): UUID

        /**
         * Get a group's properties by its position
         */
        fun getGroupProperties(groupPosition: Long): IntervalRunProperties?
    }
}