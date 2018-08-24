package io.github.crr0004.intervalme

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.github.crr0004.intervalme.database.IntervalData

class IntervalClockSampleActivity : AppCompatActivity(), IntervalSimpleGroupListFragment.OnFragmentInteractionListener {
    override fun detachedFrom(intervalSimpleGroupListFragment: IntervalSimpleGroupListFragment) {

    }

    override fun attachedTo(intervalSimpleGroupListFragment: IntervalSimpleGroupListFragment) {

    }

    override fun onItemSelected(interval: IntervalData, isSelected: Boolean) {
        Log.d("icsa", "Interval selected: $interval")
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_clock_sample)
    }


}

