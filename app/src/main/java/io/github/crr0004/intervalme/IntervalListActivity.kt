package io.github.crr0004.intervalme

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView.CHOICE_MODE_MULTIPLE
import android.widget.ExpandableListView
import android.widget.Toast
import io.github.crr0004.intervalme.IntervalAddActivity.Companion.EDIT_MODE_FLAG_INTERVAL_ID
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase

class IntervalListActivity : AppCompatActivity() {

    private var mAdapter: IntervalListAdapter? = null
    private var mExpandableListView: ExpandableListView? = null
    private lateinit var mDragDropSortController: DragDropAnimationController<IntervalData>

    companion object {
        private const val INTERVAL_LIST_BUNDLE_EXPANDED_STATE_ID = "ilpes"
        const val INTENT_EXTRA_RENEW_DATA_ID = "ilrd"
        const val INTENT_EDIT_REQUEST_CODE = 1
        const val INTENT_ADD_REQUEST_CODE = 2
    }

    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_list)


        mExpandableListView = findViewById(R.id.intervalsExpList)
        mAdapter = IntervalListAdapter(this, mExpandableListView!!)
        mExpandableListView!!.setAdapter(mAdapter)
        mExpandableListView!!.choiceMode = CHOICE_MODE_MULTIPLE
        setSupportActionBar(findViewById(R.id.interval_list_actionbar))
        if(savedInstanceState != null){
            val expandedState = savedInstanceState.getBooleanArray(INTERVAL_LIST_BUNDLE_EXPANDED_STATE_ID)
            expandedState.forEachIndexed { index, b ->
                mExpandableListView!!.expandGroup(index+1)
            }
        }
        if(mAdapter !is DragDropAnimationController.DragDropViewSource<IntervalData>){
            throw ClassCastException("Cannot cast mAdapter to DragDropViewSource<IntervalData>")
        }
        mDragDropSortController = DragDropAnimationController(this, mAdapter as DragDropAnimationController.DragDropViewSource<IntervalData>)

        mExpandableListView!!.setOnItemLongClickListener { parentAdapter, v, position, _ ->
            val intervalData = parentAdapter.getItemAtPosition(position) as IntervalData
            if(intervalData.ownerOfGroup) {
                val groupUUID = intervalData.group.toString()
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("group uuid", groupUUID)
                Toast.makeText(this, "Copied UUID", Toast.LENGTH_SHORT).show()

                clipboard.primaryClip = clipData

                false
            }else{

                false
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
                ActivityCompat.startActivityForResult(this, intent, INTENT_ADD_REQUEST_CODE, null)
                true
            }
            R.id.action_start_all_clocks ->{
                mAdapter?.startAllIntervals()
                true
            }
            R.id.action_swap_intervals ->{
                val itemPositions = mAdapter!!.mChecked
                if(itemPositions.size() != 2){
                    Toast.makeText(this, "Please select two items", Toast.LENGTH_SHORT).show()
                    return false
                }
                val item1 = mExpandableListView!!.getItemAtPosition(itemPositions.keyAt(0)) as IntervalData
                val item2 = mExpandableListView!!.getItemAtPosition(itemPositions.keyAt(1)) as IntervalData

                mAdapter!!.setItemChecked(itemPositions.keyAt(0), false)
                mAdapter!!.setItemChecked(itemPositions.keyAt(1), false)

                mDragDropSortController.swapItems(item1,item2)


                true
            }
            R.id.action_fix_group_positions -> {
                val intervalDao = IntervalMeDatabase.getInstance(this)!!.intervalDataDao()
                val groups = intervalDao.getGroupOwners()
                groups.forEach {
                    val groupMembers = intervalDao.getAllOfGroupWithoutOwner(it.group)
                    groupMembers.forEachIndexed { index, intervalData ->
                        intervalData.groupPosition = (index).toLong()
                        intervalDao.update(intervalData)
                    }
                }
                mAdapter!!.notifyDataSetInvalidated()
                true
            }
            R.id.action_edit_items -> {
                if(!mAdapter!!.mInEditMode) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = resources.getDrawable(R.drawable.ic_done_white_24dp, theme)
                    }else{
                        item.icon = resources.getDrawable(R.drawable.ic_done_white_24dp)
                    }
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = resources.getDrawable(R.drawable.ic_mode_edit_white_24dp, theme)
                    }else{
                        item.icon = resources.getDrawable(R.drawable.ic_mode_edit_white_24dp)
                    }
                }
                mAdapter!!.mInEditMode = !mAdapter!!.mInEditMode
                mAdapter!!.notifyDataSetChanged()
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
            if (intervalData != null) {
                mIntervalDao.update(intervalData)
            }
        }
    }

    /**
     * Dispatch incoming result to the correct fragment.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        Log.d("ila", "IntervalListActivity onActivityResult")
        if(requestCode == INTENT_EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null){
            val id = data.getLongExtra(EDIT_MODE_FLAG_INTERVAL_ID, -1)
            mAdapter?.updateInterval(id)
            mAdapter?.notifyDataSetChanged()
        }else if(requestCode == INTENT_ADD_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            mAdapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
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

    fun launchAddInEditMode(childOfInterval: IntervalData) {
        val intent = Intent(this, IntervalAddActivity::class.java)
        intent.putExtra(IntervalAddActivity.EDIT_MODE_FLAG_ID, true) //We're going into edit mode
        intent.putExtra(IntervalAddActivity.EDIT_MODE_FLAG_INTERVAL_ID, childOfInterval.id)
        //startActivity(mContext,intent,null)
        ActivityCompat.startActivityForResult(this, intent, INTENT_EDIT_REQUEST_CODE, null)
    }
}
