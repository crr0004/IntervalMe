package io.github.crr0004.intervalme

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_interval_list.*

class AnalyticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        this.navigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_bar_intervals -> {
                    startActivity(Intent(this, IntervalListActivity::class.java))
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
