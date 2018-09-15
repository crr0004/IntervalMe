package io.github.crr0004.intervalme.routine

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.github.crr0004.intervalme.analytics.AnalyticsActivity
import io.github.crr0004.intervalme.interval.IntervalListActivity
import io.github.crr0004.intervalme.R
import kotlinx.android.synthetic.main.activity_interval_list.*

class RoutineListActivity : AppCompatActivity() {

    private var mRoutineAdapter: RoutineRecyclerAdapter = RoutineRecyclerAdapter(this)
    private val mLayoutManager: RecyclerView.LayoutManager? = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine)

        setSupportActionBar(findViewById(R.id.routine_actionbar))
        supportActionBar?.title = getString(R.string.app_name)

        with(findViewById<RecyclerView>(R.id.routineRecyclerView)){
            setHasFixedSize(false)
            adapter = mRoutineAdapter
            layoutManager = mLayoutManager
        }

        setupNavigation()
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
