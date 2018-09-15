package io.github.crr0004.intervalme

import android.support.test.espresso.NoMatchingViewException
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.ViewAssertion
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.view.View
import android.widget.AdapterView
import android.widget.ExpandableListView
import android.widget.TextView
import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.interval.IntervalListAdapter
import junit.framework.AssertionFailedError
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher


public class CustomViewActionsMatchers{
    companion object {
        fun setTextInTextView(value: String): ViewAction {
            return object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return allOf(isDisplayed(), isAssignableFrom(TextView::class.java))
                }

                override fun perform(uiController: UiController, view: View) {
                    (view as TextView).text = value
                }

                override fun getDescription(): String {
                    return "replace text"
                }
            }
        }
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

        public fun groupIsIntervalData(intervalData: Matcher<IntervalData>, view: Matcher<View>): Matcher<View>{
            return object : TypeSafeMatcher<View>(){
                override fun describeTo(description: Description) {
                    description.appendText("with interval name: ")
                    intervalData.describeTo(description)
                    description.appendText(" with view: ")
                    view.describeTo(description)
                }

                override fun matchesSafely(item: View): Boolean {
                    return false
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

        /**
         * Check if a follows b
         */
        fun itemFollows(groupPosition: Int, a: IntervalData, b: IntervalData): ViewAssertion {
            return object : ViewAssertion {

                override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
                    if(view !is ExpandableListView){
                        throw AssertionFailedError("Not a ExpandableListView")
                    }
                    if(view.expandableListAdapter !is IntervalListAdapter){
                        throw AssertionFailedError("Adapter is not a IntervalListAdapter")
                    }
                    val adapter = view.expandableListAdapter as IntervalListAdapter
                    if(b.groupPosition - a.groupPosition < 1){
                        val indexDifference = b.groupPosition - a.groupPosition
                        throw AssertionFailedError("$a does not follow $b by index check\nIndex difference: $indexDifference")
                    }
                    val aFromAdapter = adapter.getChild(groupPosition, a.groupPosition.toInt())
                    val bFromAdapter = adapter.getChild(groupPosition, b.groupPosition.toInt())

                    if(aFromAdapter != a){
                        throw AssertionFailedError("$a doesn't equal $aFromAdapter")
                    }
                    if(bFromAdapter != b){
                        throw AssertionFailedError("$b doesn't equal $bFromAdapter")
                    }
                }
            }
        }

        private fun withAdaptedData(dataMatcher: Matcher<Any>): Matcher<View> {
            return object : TypeSafeMatcher<View>() {

                @Override
                override fun describeTo(description: Description) {
                    description.appendText("with class name: ")
                    dataMatcher.describeTo(description)
                }

                @Override
                override fun matchesSafely(view: View): Boolean {
                    if (view !is AdapterView<*>) {
                        return false
                    }

                    val adapter = (view as AdapterView<*>).adapter
                    for (i in 0 until adapter.count) {
                        if (dataMatcher.matches(adapter.getItem(i))) {
                            return true
                        }
                    }

                    return false
                }
            }
        }
    }

}