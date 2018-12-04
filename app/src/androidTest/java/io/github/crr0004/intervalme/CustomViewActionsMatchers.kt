package io.github.crr0004.intervalme

import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ExpandableListView
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.interval.old.IntervalListAdapter
import io.github.crr0004.intervalme.routine.RoutineManageBasicFragment
import kotlinx.android.synthetic.main.routine_manage_basic_single_item.view.*
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf


class CustomViewActionsMatchers{
    companion object {
        fun withIntervalData(intervalData: IntervalData): Matcher<Any> {
            checkNotNull(intervalData)
            return withIntervalData(Matchers.equalTo(intervalData))
        }

        fun withIntervalData(intervalData: Matcher<IntervalData>): Matcher<Any> {
            checkNotNull(intervalData)
            // ChildStruct is the Class returned by BaseExpandableListAdapter.getChild()
            return object : BoundedMatcher<Any, IntervalData>(IntervalData::class.java) {

                public override fun matchesSafely(data: IntervalData): Boolean {
                    return intervalData.matches(data)
                }

                override fun describeTo(description: Description) {
                    intervalData.describeTo(description)
                }
            }
        }

        fun swapIntervalListAdapterItems(intervalData1: IntervalData, intervalData2: IntervalData): ViewAction {
            return object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return allOf(isDisplayed(), isAssignableFrom(ExpandableListView::class.java))
                }

                override fun perform(uiController: UiController, view: View) {
                    if((view as ExpandableListView).expandableListAdapter is IntervalListAdapter){
                        ((view.expandableListAdapter) as IntervalListAdapter).swapItems(intervalData1, intervalData2)
                    }
                }

                override fun getDescription(): String {
                    return "swap items in intervallistadapter"
                }
            }
        }

        fun invalidateAdapter(): ViewAction {
            return object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return allOf(isDisplayed(), isAssignableFrom(ExpandableListView::class.java))
                }

                override fun perform(uiController: UiController, view: View) {
                    (view as ExpandableListView).invalidateViews()
                }

                override fun getDescription(): String {
                    return "invalidate expandablelistview"
                }
            }
        }

        fun editRoutineItemViewHolderDescription(exerciseData: ExerciseData) : ViewAction{
            return object: ViewAction{
                override fun getDescription(): String {
                    return "set description to $exerciseData"
                }

                override fun getConstraints(): Matcher<View> {
                    return allOf(isDisplayed(), withId(R.id.routineManageBasicSingleItemLayoutId))
                }

                override fun perform(uiController: UiController?, view: View?) {
                    view!!.rMBSIDescText.setText(exerciseData.description)
                    view.rMBSIValue0.setText(exerciseData.value0)
                    view.rMBSIValue1.setText(exerciseData.value1)
                    view.rMBSIValue2.setText(exerciseData.value2)
                }
            }
        }

        fun matchRoutineItemViewHolderPosition(pos: Int) : Matcher<RoutineManageBasicFragment.RoutineManageBasicItemViewHolder>{
            return object : BaseMatcher<RoutineManageBasicFragment.RoutineManageBasicItemViewHolder>() {
                override fun describeTo(description: Description?) {

                }

                override fun matches(item: Any?): Boolean {
                    return item is RoutineManageBasicFragment.RoutineManageBasicItemViewHolder &&
                            item.adapterPosition == pos
                }
            }
        }

        fun setRoutineManageBasicRecyclerItems(data: ArrayList<ExerciseData>) : ViewAction{
            return object: ViewAction{
                override fun getDescription(): String {
                    return "sets the items in R.id.routineManageBasicRecycler"
                }

                override fun getConstraints(): Matcher<View> {
                    return allOf(withId(R.id.routineManageBasicRecycler))
                }

                override fun perform(uiController: UiController?, view: View?) {
                    val adapter = ((view!! as RecyclerView)
                            .adapter as RoutineManageBasicFragment.RoutineManageBasicItemsAdapter)
                    adapter.routine?.exercises?.addAll(data)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

}