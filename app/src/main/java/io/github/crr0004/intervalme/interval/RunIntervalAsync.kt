package io.github.crr0004.intervalme.interval

import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.IntervalData

class RunIntervalAsync(private val interval: IntervalData, private val durationText: TextView) : Runnable{

    override fun run() {
        if(durationText.getTag(R.id.id_interval_timer_running_tag) as Boolean) {
            interval.duration--
            durationText.text = interval.duration.toString()
            durationText.postDelayed(this, 1000)
        }
    }
}