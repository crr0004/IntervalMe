package io.github.crr0004.intervalme.analytics

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.crr0004.intervalme.R
import io.github.crr0004.intervalme.database.analytics.ExerciseAnalyticData
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsData
import io.github.crr0004.intervalme.database.analytics.RoutineAnalyticData
import kotlinx.android.synthetic.main.analytics_interval_single_view.view.*
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var intervalItems = arrayOf(IntervalAnalyticsData(label = "Hello world", duration = 60L),IntervalAnalyticsData(label = "Hello world2"))
    var routineItems = arrayOf<RoutineAnalyticData>(RoutineAnalyticData(0, "RoutineAnalytics"))
    var exerciseItems = arrayOf<ExerciseAnalyticData>(
            ExerciseAnalyticData(0, 0, "ExerciseItem", Date(), "v0", "v1", "v2", true)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            0 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.analytics_interval_single_view, parent, false)
                IntervalViewHolder(view)
            }
            1 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.analytics_routine_single_view, parent, false)
                RoutineViewHolder(view)
            }
            2 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.analytics_exercise_single_view, parent, false)
                ExerciseViewHolder(view)
            }
            else -> {
                throw RuntimeException("A view type hasn't been accounted for in analytics")
            }
        }
    }

    override fun getItemCount(): Int {
        return intervalItems.size + routineItems.size + exerciseItems.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, pos: Int) {
        when(getItemViewType(pos)) {
            0 -> {
                (viewHolder as IntervalViewHolder).bind(intervalItems[pos])
            }
            1 -> {
                (viewHolder as RoutineViewHolder).bind(routineItems[pos-intervalItems.size])
            }
            2 -> {
                (viewHolder as ExerciseViewHolder).bind(exerciseItems[pos-(intervalItems.size+routineItems.size)])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(position < intervalItems.size)
            return 0
        else if(position < intervalItems.size + routineItems.size)
            return 1
        else if(position < exerciseItems.size + intervalItems.size + routineItems.size)
            return 2
        else
            throw RuntimeException("A view type hasn't been accounted for in analytics")
        //return super.getItemViewType(position)
    }

    class ExerciseViewHolder(view: View): RecyclerView.ViewHolder(view){
        fun bind(data: ExerciseAnalyticData){
            itemView.analyticsViewDate.text = SimpleDateFormat.getDateTimeInstance().format(data.lastModified)
            itemView.analyticsViewLabel.text = data.description
            itemView.findViewById<TextView>(R.id.analyticsViewValue0).text = data.value0
            itemView.findViewById<TextView>(R.id.analyticsViewValue1).text = data.value1
            itemView.findViewById<TextView>(R.id.analyticsViewValue2).text = data.value2
        }
    }

    class RoutineViewHolder(view: View): RecyclerView.ViewHolder(view){
        fun bind(data: RoutineAnalyticData){
            itemView.analyticsViewDate.text = SimpleDateFormat.getDateTimeInstance().format(data.lastModified)
            itemView.analyticsViewLabel.text = data.description
        }
    }

    class IntervalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: IntervalAnalyticsData){
            itemView.analyticsViewDate.text = SimpleDateFormat.getDateTimeInstance().format(data.lastModified)
            itemView.analyticsViewDuration.text = data.duration.toString()
            itemView.analyticsViewGroupName.text = data.groupName ?: ""
            itemView.analyticsViewLabel.text = data.label
            itemView.analyticsViewPosition.text = data.groupPosition.toString()
        }
    }
}