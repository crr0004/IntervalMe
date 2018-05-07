package io.github.crr0004.intervalme

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ExpandableListView
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase

class IntervalListActivity : AppCompatActivity() {

    private var mAdapter: IntervalListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_list)

        val intervalListView = findViewById<ExpandableListView>(R.id.intervalsExpList)
        mAdapter = IntervalListAdapter(this.applicationContext, intervalListView)
        intervalListView.setAdapter(mAdapter)
    }

    /**
     * Dispatch onPause() to fragments.
     */
    override fun onPause() {
        super.onPause()
        val cachedViews = mAdapter!!.mCachedViews
        val mIntervalDao = IntervalMeDatabase.getInstance(this.applicationContext)!!.intervalDataDao()
        cachedViews.forEach { key, view ->
            val intervalData = view.getTag(R.id.id_interval_view_interval)
            if (intervalData != null) {
                mIntervalDao.update(intervalData as IntervalData)
            }else{
                Log.d("UpdatingIntervals", "Updating interval at key $key is null")
            }
        }
    }
}
