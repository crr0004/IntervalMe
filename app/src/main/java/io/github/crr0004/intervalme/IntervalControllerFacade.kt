package io.github.crr0004.intervalme

import android.view.View
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.views.IntervalClockView

class IntervalControllerFacade {

    fun intervalsSwapped(id: Long, id1: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun groupView(groupPosition: Int, toReturn: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setUpGroupOrder(groupPosition: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun connectClockView(clockView: IntervalClockView?, groupPosition: Int, childOfInterval: IntervalData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun delete(childOfInterval: IntervalData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setIntervalAsLast(groupPosition: Int, childOfInterval: IntervalData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun startAllIntervals() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setGroup(groupPosition: Long, it: Array<IntervalData>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onPauseCalled(intervalListActivity: IntervalListActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onResumeCalled(intervalListActivity: IntervalListActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun onStopCalled(intervalListActivity: IntervalListActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
}