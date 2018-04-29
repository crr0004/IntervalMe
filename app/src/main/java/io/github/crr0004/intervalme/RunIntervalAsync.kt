package io.github.crr0004.intervalme

import android.widget.TextView
import io.github.crr0004.intervalme.database.IntervalData

class RunIntervalAsync(private val interval: IntervalData, private val durationText: TextView) : Runnable{

    override fun run() {
        interval.duration--
        durationText.text = interval.duration.toString()
        durationText.postDelayed(this, 1000)
    }
}