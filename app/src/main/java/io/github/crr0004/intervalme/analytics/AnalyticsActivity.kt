package io.github.crr0004.intervalme.analytics

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.routine.RoutineListActivity
import io.github.crr0004.intervalme.interval.IntervalListActivity
import kotlinx.android.synthetic.main.activity_interval_list.*

class AnalyticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)




        setUpNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.interval_list_menu, menu)
        return true
    }

    private fun setUpNavigation() {
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
                R.id.nav_bar_routines -> {
                    val intent = Intent(this, RoutineListActivity::class.java)
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
        this.navigation.menu.findItem(R.id.nav_bar_analytics).isChecked = true
    }
}
