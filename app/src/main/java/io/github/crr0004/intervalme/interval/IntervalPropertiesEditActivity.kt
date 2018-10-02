package io.github.crr0004.intervalme.interval

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.IntervalData
import kotlinx.android.synthetic.main.activity_interval_properties.*

class IntervalPropertiesEditActivity : AppCompatActivity(),
        IntervalAddFragment.IntervalAddFragmentInteractionI,
        IntervalPropertiesEditFragment.IntervalPropertiesEditFragmentInteractionI,
        IntervalSimpleGroupListFragment.OnFragmentInteractionListener {

    private var mIntervalToEdit: MutableLiveData<IntervalData>? = null

    override fun onItemSelected(interval: IntervalData, isSelected: Boolean) {
        (mSectionsPagerAdapter?.addFragment)?.onItemSelected(interval, isSelected)
    }

    override fun attachedTo(intervalSimpleGroupListFragment: IntervalSimpleGroupListFragment) {
        (mSectionsPagerAdapter?.addFragment)?.attachedTo(intervalSimpleGroupListFragment)
    }

    override fun detachedFrom(intervalSimpleGroupListFragment: IntervalSimpleGroupListFragment) {
        (mSectionsPagerAdapter?.addFragment)?.detachedFrom(intervalSimpleGroupListFragment)
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    override fun attachedTo(intervalAddActivity: IntervalAddFragment) {

    }

    override fun wantToFinish() {
        finish()
    }

    override fun getCreationIntent(): Intent {
        return intent
    }

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private lateinit var mModel: IntervalAddSharedModel
    private lateinit var mModelProvider: IntervalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interval_properties)
        volumeControlStream = AudioManager.STREAM_MUSIC

        mModel = ViewModelProviders.of(this).get(IntervalAddSharedModel::class.java)
        mModelProvider = ViewModelProviders.of(this).get(IntervalViewModel::class.java)
        mModel.setIntervalToEdit(intent!!.getLongExtra(IntervalAddFragment.EDIT_MODE_FLAG_INTERVAL_ID, -1), this)
        mIntervalToEdit = mModel.getIntervalToEdit()
        mModel.isInEditMode = intent!!.getBooleanExtra(IntervalAddFragment.EDIT_MODE_FLAG_ID, false)

        setSupportActionBar(this.toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))


        (intervalPropertiesFAB).setOnClickListener {
            mModel.commit()
            val intent = Intent()
            if(mIntervalToEdit?.value != null) {
                intent.putExtra(IntervalAddFragment.EDIT_MODE_FLAG_INTERVAL_ID, mIntervalToEdit!!.value!!.id)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_interval_properties, menu)
        if(!mModel.isInEditMode){
            menu.findItem(R.id.action_reset_changes).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when(id){
            R.id.action_reset_changes -> {
                mModel.resetChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    interface IntervalPropertiesEditI{
        fun onBind()
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private var mAddFragment: IntervalAddFragment? = null
        private var mPropertiesFragment: IntervalPropertiesEditFragment? = null
        val addFragment: IntervalAddFragment?
        get() {return mAddFragment}

        override fun getItem(position: Int): Fragment {
            when(position){
                0 ->{
                    // Existing add activity (to be turned into a fragment)
                    if(mAddFragment == null){
                       mAddFragment = IntervalAddFragment()
                    }
                    return mAddFragment!!
                }
                1 -> {
                    // New add properties fragment
                    if(mPropertiesFragment == null){
                        mPropertiesFragment = IntervalPropertiesEditFragment()
                    }
                    return mPropertiesFragment!!
                }
                else -> {
                    throw RuntimeException("Trying to get a fragment that doesn't exist")
                }
            }
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 2
        }
    }
}
