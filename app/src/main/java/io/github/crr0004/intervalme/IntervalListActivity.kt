package io.github.crr0004.intervalme

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ExpandableListView
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
        val cachedControllers = mAdapter!!.mCachedControllers
        val mIntervalDao = IntervalMeDatabase.getInstance(this.applicationContext)!!.intervalDataDao()
        cachedControllers.forEach { key, controller ->
            controller.onPause()
            val intervalData = controller.mChildOfInterval
            mIntervalDao.update(intervalData)
        }
    }
}
