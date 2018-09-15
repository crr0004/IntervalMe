package io.github.crr0004.intervalme.interval

import android.content.Context
import android.view.View
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties
import io.github.crr0004.intervalme.views.IntervalClockView

class IntervalGroupController(mClockView: IntervalClockView? = null,
                              mChildOfInterval: IntervalData,
                              runProperties: IntervalRunProperties? = null,
                              applicationContext: Context? = null,
                              callBackHost: IntervalControllerCallBackI) :
        IntervalController(mClockView,
                mChildOfInterval,
                runProperties,
                applicationContext,
                callBackHost) {

    override fun updateViewToProperties(properties: IntervalRunProperties) {
        mGroupView?.post {
            mGroupView?.findViewById<TextView>(R.id.intervalGroupLoops)?.text =
                    properties.loops.toString()
        }

    }

    private var mGroupView: View? = null

    override fun connectNewClockView(view: View) {
        if(view.id == R.id.interval_group){
            mGroupView = view
        }
    }
}
