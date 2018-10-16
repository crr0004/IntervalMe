package io.github.crr0004.intervalme.interval

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.view.menu.MenuBuilder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView.CHOICE_MODE_MULTIPLE
import android.widget.ExpandableListView
import android.widget.Toast
import io.github.crr0004.intervalme.*
import io.github.crr0004.intervalme.routine.RoutineListActivity
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalMeDatabase
import io.github.crr0004.intervalme.analytics.AnalyticsActivity
import kotlinx.android.synthetic.main.activity_interval_list.*
import java.util.*

class IntervalListActivity : AppCompatActivity() {

    private var mAdapter: IntervalListAdapter? = null
    private var mExpandableListView: ExpandableListView? = null
    private lateinit var mDragDropSortController: DragDropAnimationController<IntervalData>
    private lateinit var mProvider: IntervalViewModel
    private var mGroupsSize: Long = 0

    companion object {
        private const val INTERVAL_LIST_BUNDLE_EXPANDED_STATE_ID = "ilpes"
        const val INTENT_EDIT_REQUEST_CODE = 1
        const val INTENT_ADD_REQUEST_CODE = 2
        val ETC_GROUP_UUID: UUID = UUID.fromString("5c9a12d5-4a99-4957-925e-e61c0bd74a77")
    }

    init {

    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_list)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        mExpandableListView = findViewById(R.id.intervalsExpList)
        mAdapter = IntervalListAdapter(this, mExpandableListView!!)
        mExpandableListView!!.setAdapter(mAdapter)
        mExpandableListView!!.choiceMode = CHOICE_MODE_MULTIPLE
        setSupportActionBar(findViewById(R.id.interval_list_actionbar))
        volumeControlStream = AudioManager.STREAM_MUSIC

        if(savedInstanceState != null){
            val expandedState = savedInstanceState.getBooleanArray(INTERVAL_LIST_BUNDLE_EXPANDED_STATE_ID)

            expandedState?.forEachIndexed { index, b ->
                mExpandableListView!!.expandGroup(index+1)
            }
        }

        this.navigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_bar_analytics -> {
                    val intent = Intent(this, AnalyticsActivity::class.java)
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptions.makeSceneTransitionAnimation(this,
                                findViewById<View>(R.id.navigation), "navigation")

                        startActivity(intent, options.toBundle())

                    }else{
                        startActivity(intent)
                    }
                    true
                }
                R.id.nav_bar_routines -> {
                    val intent = Intent(this, RoutineListActivity::class.java)
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptions.makeSceneTransitionAnimation(this,
                                findViewById<View>(R.id.navigation), "navigation")

                        startActivity(intent, options.toBundle())

                    }else{
                        startActivity(intent)
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
        this.navigation.menu.findItem(R.id.nav_bar_intervals).isChecked = true


        if(mAdapter !is DragDropAnimationController.DragDropViewSource<IntervalData>){
            throw ClassCastException("Cannot cast mAdapter to DragDropViewSource<IntervalData>")
        }
        mDragDropSortController = DragDropAnimationController(this, mAdapter as DragDropAnimationController.DragDropViewSource<IntervalData>)


        mExpandableListView!!.setOnItemLongClickListener { parentAdapter, v, position, _ ->
            val intervalData = parentAdapter.getItemAtPosition(position) as IntervalData
            if(intervalData.ownerOfGroup) {
                mExpandableListView!!.collapseGroup(intervalData.groupPosition.toInt())
            }else{


            }
            v.startDrag(ClipData.newPlainText("",""), View.DragShadowBuilder(v), intervalData, View.DRAG_FLAG_GLOBAL)
            true
        }
        mExpandableListView?.setOnGroupExpandListener {
            IntervalControllerFacade.instance.groupExpanded(it)
        }


        mProvider = ViewModelProviders.of(this).get(IntervalViewModel::class.java)

        IntervalControllerFacade.instance.setAnalyticsDataSource(mProvider.mAnalyticsRepository)

        setUpAdapterDataObservers()
    }

    private fun setUpAdapterDataObservers() {
        val groupObserver = GroupObserver()
        val liveGroups = mProvider.getGroups()
        liveGroups.observe(this, Observer { groups: Array<IntervalData>? ->
            //mAdapter?.clear()
            groups?.forEachIndexed { index, intervalData ->
                mProvider.getAllOfGroup(intervalData.group).observe(this, groupObserver)
            }
        })

        mProvider.getGroupsSize().observe(this, Observer {
            mAdapter?.groupSize = it?.toInt() ?: 0
            mGroupsSize = it ?: 0L
        })
        mProvider.getProperties().observe(this, Observer {
            it?.forEachIndexed { _, intervalRunProperties ->
                mAdapter?.setProperty(intervalRunProperties.intervalId, intervalRunProperties)
            }
        })
    }

    inner class GroupObserver : Observer<Array<IntervalData>>{
        override fun onChanged(it: Array<IntervalData>?) {
            if(it != null && it.isNotEmpty()){
                if(BuildConfig.DEBUG && !it[0].ownerOfGroup){
                    // This happens when the ETC group hasn't been created
                    // With no ETC group children are moved to it without an owner
                    //mProvider.moveOrphanedChildrenToGroup(ETC_GROUP_UUID)
                    Log.d("ILA", "Group is getting set without a group owner")
                }else{
                    mAdapter?.setGroup(it[0].groupPosition, it)

                }
            }
            mAdapter?.notifyDataSetChanged()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.interval_list_menu, menu)
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        (menu as MenuBuilder).setGroupVisible(R.id.action_debug_items_group, PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("ui_debug_menu_items", false))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_goto_add -> {
                // User chose the "Settings" item, show the app settings UI...
                val intent = Intent(this, IntervalPropertiesEditActivity::class.java)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(intent, INTENT_ADD_REQUEST_CODE,
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                }else{
                    startActivityForResult(intent, INTENT_ADD_REQUEST_CODE)
                }
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
                groups.forEachIndexed { i, it ->
                    val groupMembers = intervalDao.getAllOfGroupWithoutOwner(it.group)
                    groupMembers.forEachIndexed { index, intervalData ->
                        intervalData.groupPosition = (index).toLong()
                        intervalDao.update(intervalData)
                    }
                    it.groupPosition = i.toLong()
                    intervalDao.update(it)
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
            R.id.action_create_etc_group -> {
                mProvider.moveOrphanedChildrenToGroup(ETC_GROUP_UUID)
                true
            }
            R.id.action_create_sample_groups -> {
                val groups = IntervalData.generate(3)
                val groupSize = mGroupsSize
                mProvider.startExecuteQueue()
                groups.forEachIndexed { index, intervalData ->
                    //intervalData!!.groupPosition = groupSize+index
                    intervalData!!.label = index.toString()
                    val children = IntervalData.generate(3, intervalData, durationMod = 3L)
                    children.forEachIndexed { childIndex, child ->
                        child!!.label = "Child $childIndex"
                    }
                    mProvider.insert(intervalData)
                    mProvider.insertIntervalIntoGroup(children, intervalData.group)
                }
                mProvider.runQueue()


                true
            }
            R.id.action_duplicate_intervals -> {
                val checked = mAdapter!!.mChecked
                if(checked.size() > 0) {
                    val copiedList = Array<IntervalData?>(checked.size()) {
                        val intervalToCopy = mExpandableListView!!.getItemAtPosition(checked.keyAt(it)) as IntervalData
                        IntervalData(intervalToCopy)
                    }
                    mProvider.insertIntervalIntoGroup(copiedList, copiedList[0]!!.group)
                    mAdapter!!.mChecked.clear()
                }else{
                    Toast.makeText(this, "Please select items", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }
            R.id.action_clear_db -> {
                IntervalMeDatabase.getInstance(this)?.clearAllTables()
                return true
            }
            R.id.action_interval_to_settings -> {
               val intent = Intent(this, SettingsActivity::class.java)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                }else{
                    startActivity(intent)
                }
                return true
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
        IntervalControllerFacade.instance.onPauseCalled(this)
        /*
        Thread(Runnable {
            val cachedControllers = mAdapter!!.mCachedControllers
            cachedControllers.forEach { key, controller ->
                controller.onPause()
                val intervalData = controller.mChildOfInterval
                mProvider.update(intervalData)
            }
        }).start()
        */

    }

    /**
     * Dispatch incoming result to the correct fragment.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        Log.d("ila", "IntervalListActivity onActivityResult")
        if(requestCode == INTENT_EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null){
            //val id = data.getLongExtra(EDIT_MODE_FLAG_INTERVAL_ID, -1)
            mAdapter?.notifyDataSetChanged()
        }else if(requestCode == INTENT_ADD_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            mAdapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {

        super.onResume()
        IntervalControllerFacade.instance.onResumeCalled(this)
        /*
        val cachedControllers = mAdapter!!.mCachedControllers
        cachedControllers.forEach { key, controller ->
            controller.onResume(this.applicationContext)
        }
        */
    }



    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {

        val expandedGroups = BooleanArray(mAdapter!!.groupCount) {false}
        expandedGroups.forEachIndexed { index, _ ->
            expandedGroups[index] = mExpandableListView!!.isGroupExpanded(index+1)
        }
        // ilpes -> interval_list_pause_expanded_state
        outState!!.putBooleanArray(INTERVAL_LIST_BUNDLE_EXPANDED_STATE_ID, expandedGroups)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onStop() {
        super.onStop()
        IntervalControllerFacade.instance.onStopCalled(this)
        /*
        val cachedControllers = mAdapter!!.mCachedControllers
        cachedControllers.forEach { key, controller ->
            controller.onStop()
        }
        */
    }

    override fun onDestroy() {
        super.onDestroy()
        IntervalControllerFacade.instance.destroy()
        IntervalMeDatabase.destroyInstance()
    }

    fun launchAddInEditMode(childOfInterval: IntervalData) {
        val intent = Intent(this, IntervalPropertiesEditActivity::class.java)
        intent.putExtra(IntervalAddFragment.EDIT_MODE_FLAG_ID, true) //We're going into edit mode
        intent.putExtra(IntervalAddFragment.EDIT_MODE_FLAG_INTERVAL_ID, childOfInterval.id)
        //startActivity(mContext,intent,null)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(intent, INTENT_EDIT_REQUEST_CODE,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }else{
            startActivityForResult(intent, INTENT_EDIT_REQUEST_CODE)
        }
    }

    fun update(item: IntervalData) {
        mProvider.update(item)
    }

    fun deleteGroupMoveChildrenToETC(intervalData: IntervalData) {
        mProvider.deleteGroupAndMoveChildrenToGroup(intervalData, ETC_GROUP_UUID)
    }
    fun moveChildIntervalAboveChild(interval: IntervalData, intervalData: IntervalData) {
        mProvider.moveChildIntervalAboveChild(interval, intervalData)
    }

    fun moveIntervalToGroup(interval: IntervalData, groupUUID: UUID) {
        mProvider.moveIntervalToGroup(interval, groupUUID)
    }

    fun moveIntervalGroupAboveGroup(interval: IntervalData, intervalData: IntervalData) {
        mProvider.moveIntervalGroupAboveGroup(interval, intervalData)
    }

    fun deleteChild(childOfInterval: IntervalData) {
        mProvider.deleteChild(childOfInterval)
    }
}
