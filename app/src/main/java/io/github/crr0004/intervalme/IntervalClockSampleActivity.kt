package io.github.crr0004.intervalme

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import io.github.crr0004.intervalme.views.IntervalClockView

class IntervalClockSampleActivity : AppCompatActivity() {


    private lateinit var tickClockRunnable: TickClockRunnable

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_clock_sample)
        val clockView: IntervalClockView = findViewById(R.id.intervalClockView)
        tickClockRunnable = TickClockRunnable(clockView)
        findViewById<Button>(R.id.sampleClockInfoBtn).setOnClickListener {
            val txtView = findViewById<TextView>(R.id.sampleInfoTxt)

            txtView.text = "Width: " + clockView.width + " Height: " + clockView.height
            clockView.postDelayed(tickClockRunnable, 100)
        }
    }

}

private class TickClockRunnable(val mClockView: IntervalClockView) : Runnable{

    override fun run() {
        if(mClockView.mPercentageComplete < 100.0f) {
            mClockView.mPercentageComplete += 0.01f
            mClockView.postDelayed(this, 100)
        }
    }
}
