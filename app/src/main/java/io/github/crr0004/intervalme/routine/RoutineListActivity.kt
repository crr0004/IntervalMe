package io.github.crr0004.intervalme.routine

import android.app.ActivityOptions
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.analytics.AnalyticsActivity
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineSetData
import io.github.crr0004.intervalme.interval.IntervalListActivity
import kotlinx.android.synthetic.main.activity_interval_list.*

class RoutineListActivity : AppCompatActivity(), RoutineRecyclerAdapter.RoutineRecyclerAdapterActionsI {

    private var mRoutineAdapter: RoutineRecyclerAdapter = RoutineRecyclerAdapter(this)
    private val mLayoutManager: RecyclerView.LayoutManager? = LinearLayoutManager(this)
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mModel: RoutineViewModel
    private var mShowEditButtons: Boolean = false

    private var editStrikeOutIcon: AnimatedVectorDrawableCompat? = null
    private var editStrikeOutIconReversed: AnimatedVectorDrawableCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine)

        setSupportActionBar(findViewById(R.id.routine_actionbar))
        supportActionBar?.title = getString(R.string.app_name)
        mRecyclerView = findViewById<RecyclerView>(R.id.routineRecyclerView)
        with(mRecyclerView){
            setHasFixedSize(false)
            adapter = mRoutineAdapter
            layoutManager = mLayoutManager
        }
        mModel = ViewModelProviders.of(this).get(RoutineViewModel::class.java)

        mModel.getAllRoutines().observe(this, Observer{
            mRoutineAdapter.values = it
        })

        editStrikeOutIcon = AnimatedVectorDrawableCompat.create(this, R.drawable.ic_mode_edit_strike_out_animated_24dp)
        editStrikeOutIconReversed = AnimatedVectorDrawableCompat.create(this, R.drawable.ic_mode_edit_strike_out_reverse_animation)

        setupNavigation()
    }

    override fun deleteRoutine(routineData: RoutineSetData) {
        mModel.deleteRoutine(routineData)
    }

    override fun isShowEditButtons(): Boolean {
        return mShowEditButtons
    }

    override fun update(exerciseData: ExerciseData) {
        mModel.update(exerciseData)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.routine_list_menu_add -> {
                startActivity(Intent(this, RoutineManageActivity::class.java))
                true
            }
            R.id.routine_list_toggle_edit_buttons -> {

                //val animation = AnimationUtils.loadAnimation(this, R.drawable.ic_mode_edit_strike_out_animated_24dp)
                if(!mShowEditButtons){
                    item.icon = editStrikeOutIcon
                    editStrikeOutIcon?.start()
                }else{
                    item.icon = editStrikeOutIconReversed
                    editStrikeOutIconReversed?.start()
                }
                mShowEditButtons = !mShowEditButtons
                mRoutineAdapter.updateRoutineViews()

                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_routine_list_activity, menu)
        return true
    }

    private fun setupNavigation() {
        this.navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_bar_intervals -> {
                    val intent = Intent(this, IntervalListActivity::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptions.makeSceneTransitionAnimation(this,
                                findViewById<View>(R.id.navigation), "navigation")

                        startActivity(intent, options.toBundle())

                    } else {
                        startActivity(intent)
                    }
                    true
                }
                R.id.nav_bar_analytics -> {
                    val intent = Intent(this, AnalyticsActivity::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptions.makeSceneTransitionAnimation(this,
                                findViewById<View>(R.id.navigation), "navigation")

                        startActivity(intent, options.toBundle())

                    } else {
                        startActivity(intent)
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
        this.navigation.menu.findItem(R.id.nav_bar_routines).isChecked = true
    }
}
