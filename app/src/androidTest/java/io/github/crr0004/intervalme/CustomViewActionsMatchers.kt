package io.github.crr0004.intervalme

import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.view.View
import android.widget.ExpandableListView
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.interval.IntervalListAdapter
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

    }

}