package io.github.crr0004.intervalme

import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import io.github.crr0004.intervalme.database.IntervalData
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
        fun withChildName(intervalData: IntervalData): Matcher<Any> {
            checkNotNull(intervalData)
            return withChildName(Matchers.equalTo(intervalData))
        }

        fun withChildName(intervalData: Matcher<IntervalData>): Matcher<Any> {
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