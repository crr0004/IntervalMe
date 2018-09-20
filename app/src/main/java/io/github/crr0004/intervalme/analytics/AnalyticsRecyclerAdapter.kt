package io.github.crr0004.intervalme.analytics

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsData
import kotlinx.android.synthetic.main.analytics_single_view.view.*
import java.text.SimpleDateFormat

class AnalyticsRecyclerAdapter(val mHost: Activity) : RecyclerView.Adapter<AnalyticsRecyclerAdapter.ViewHolder>() {

    var items = arrayOf(IntervalAnalyticsData(label = "Hello world", duration = 60L),IntervalAnalyticsData(label = "Hello world2"))

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.analytics_single_view, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(items[p1])
    }


    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: IntervalAnalyticsData){
            view.analyticsViewDate.text = SimpleDateFormat.getDateTimeInstance().format(data.lastModified)
            view.analyticsViewDuration.text = data.duration.toString()
            view.analyticsViewGroupName.text = data.groupName ?: ""
            view.analyticsViewLabel.text = data.label
            view.analyticsViewPosition.text = data.groupPosition.toString()
        }
    }
}