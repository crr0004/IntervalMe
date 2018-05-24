package io.github.crr0004.intervalme

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ExpandableListView
import io.github.crr0004.intervalme.database.IntervalMeDatabase

class IntervalListActivity : AppCompatActivity() {

    private var mAdapter: IntervalListAdapter? = null
    private var mExpandableListView: ExpandableListView? = null
    companion object {
        private const val INTERVAL_LIST_BUNDLE_EXPANDED_STATE_ID = "ilpes"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_list)

        mExpandableListView = findViewById(R.id.intervalsExpList)
        mAdapter = IntervalListAdapter(this.applicationContext, mExpandableListView!!)
        mExpandableListView!!.setAdapter(mAdapter)
        setSupportActionBar(findViewById(R.id.interval_list_actionbar))
        if(savedInstanceState != null){
            val expandedState = savedInstanceState.getBooleanArray(INTERVAL_LIST_BUNDLE_EXPANDED_STATE_ID)
            expandedState.forEachIndexed { index, b ->
                mExpandableListView!!.expandGroup(index+1)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.interval_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_goto_add -> {
                // User chose the "Settings" item, show the app settings UI...
                val intent = Intent(this, IntervalAddActivity::class.java)
                startActivity(intent)
                true
            }
            else ->
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
        }
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



    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {

        val expandedGroups = BooleanArray(mAdapter!!.groupCount, {false})
        expandedGroups.forEachIndexed { index, _ ->
            expandedGroups[index] = mExpandableListView!!.isGroupExpanded(index+1)
        }
        // ilpes -> interval_list_pause_expanded_state
        outState!!.putBooleanArray(INTERVAL_LIST_BUNDLE_EXPANDED_STATE_ID, expandedGroups)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onStop() {
        super.onStop()
        val cachedControllers = mAdapter!!.mCachedControllers
        cachedControllers.forEach { key, controller ->
            controller.onStop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
