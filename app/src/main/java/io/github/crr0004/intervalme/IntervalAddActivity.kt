package io.github.crr0004.intervalme

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class IntervalAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interveraladd)

        (findViewById<View>(R.id.goToListBtn)).setOnClickListener {
            val intent = Intent(this, IntervalListActivity::class.java)
            startActivity(intent)

        }
    }


}
