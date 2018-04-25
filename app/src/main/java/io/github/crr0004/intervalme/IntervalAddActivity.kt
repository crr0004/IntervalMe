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

class IntervalAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interveraladd)

        (findViewById<View>(R.id.goToListBtn)).setOnClickListener {
            val intent = Intent(this, IntervalListActivity::class.java)
            startActivity(intent)

        }

        findViewById<Button>(R.id.intervalAddBtn).setOnClickListener(addIntervalListener)
    }

    private val addIntervalListener = fun(v: View){
        val text = (findViewById<TextView>(R.id.intervalNameTxt)).text
        val durationText = (findViewById<TextView>(R.id.intervalDurationTxt)).text
        val interval = IntervalData(label=text.toString(), duration = durationText.toString().toLong())
        IntervalMeDatabase.getInstance(this)!!.intervalDataDao().insert(interval)
        Toast.makeText(this, "Added interval", Toast.LENGTH_SHORT).show()
    }

}
