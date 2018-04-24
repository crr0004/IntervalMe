package io.github.crr0004.intervalme

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ExpandableListView

class IntervalListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_list)
        val intervalListView = findViewById<ExpandableListView>(R.id.intervalsExpList)
        intervalListView.setAdapter(IntervalListAdapter(this.applicationContext))
    }
}
