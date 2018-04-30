package io.github.crr0004.intervalme

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import java.util.*

class IntervalAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interveraladd)

        (findViewById<View>(R.id.goToListBtn)).setOnClickListener {
            val intent = Intent(this, IntervalListActivity::class.java)
            startActivity(intent)

        }

        findViewById<Button>(R.id.intervalAddBtn).setOnClickListener(addIntervalListener)
        findViewById<Button>(R.id.increaseDurationBtn).setOnClickListener(increaseDurationClickListner)
        findViewById<Button>(R.id.decreaseDurationBtn).setOnClickListener(decreaseDurationClickListner)
        findViewById<Button>(R.id.goToClockSampleBtn).setOnClickListener(clockSampleBtnListener)
    }

    private val clockSampleBtnListener = fun(v: View){
        val intent = Intent(this, IntervalClockSampleActivity::class.java)
        startActivity(intent)
    }

    private val increaseDurationClickListner = fun(v: View){
        val duration = (findViewById<TextView>(R.id.intervalDurationTxt)).text.toString().toLong()
        (findViewById<TextView>(R.id.intervalDurationTxt)).text = (duration+1).toString()
    }

    private val decreaseDurationClickListner = fun(v: View){
        val duration = (findViewById<TextView>(R.id.intervalDurationTxt)).text.toString().toLong()
        (findViewById<TextView>(R.id.intervalDurationTxt)).text = (duration-1).toString()
    }

    private val addIntervalListener = fun(v: View){
        val text = (findViewById<TextView>(R.id.intervalNameTxt)).text
        val durationText = (findViewById<TextView>(R.id.intervalDurationTxt)).text
        val groupText = (findViewById<TextView>(R.id.intervalParentTxt)).text

        var interval: IntervalData
        interval = try {
            val groupUUID = UUID.fromString(groupText.toString())
            IntervalData(label=text.toString(), duration = durationText.toString().toLong(),group = groupUUID,ownerOfGroup = false)
        }catch (e: IllegalArgumentException){
            //Toast.makeText(this, "Invalid UUID. Setting to random", Toast.LENGTH_SHORT).show()
            IntervalData(label=text.toString(), duration = durationText.toString().toLong())
        }

        IntervalMeDatabase.getInstance(this)!!.intervalDataDao().insert(interval)
        Toast.makeText(this, "Added interval", Toast.LENGTH_SHORT).show()
    }

}
