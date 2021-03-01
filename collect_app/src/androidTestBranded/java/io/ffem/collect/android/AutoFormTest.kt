package io.ffem.collect.android

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import io.ffem.collect.android.util.TestHelper
import io.ffem.collect.android.util.mDevice
import net.bytebuddy.utility.RandomString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CopyFormRule
import org.odk.collect.android.support.FormActivityTestRule
import org.odk.collect.android.support.ResetStateRule
import org.odk.collect.android.utilities.ActivityAvailability
import java.util.*

class AutoFormTest {
    val activityTestRule = AutoFormTestRule()
    private val activityAvailability = Mockito.mock(ActivityAvailability::class.java)

    @Rule
    @JvmField
    var copyFormChain: RuleChain = RuleChain
            .outerRule(GrantPermissionRule.grant()
            )
            .around(ResetStateRule(object : AppDependencyModule() {
                override fun providesActivityAvailability(context: Context): ActivityAvailability {
                    return activityAvailability
                }
            }))
            .around(CopyFormRule(AUTO_TEST_FORM))
            .around(activityTestRule)

    @Test
    fun testActivityOpen() {
        val extras = ArrayList<String>()
        onView(ViewMatchers.withText(R.string.form_forward)).perform(ViewActions.click())
        onView(ViewMatchers.withText(R.string.form_forward)).perform(ViewActions.click())
        test_pH()
        testBoron()
        extras.add("value")
        testExternal("io.ffem.soil", "Calcium & Magnesium", extras,
                4, 0, true)
        extras.clear()
        onView(ViewMatchers.withText(R.string.form_forward)).perform(ViewActions.click())
        extras.add("value")
        testExternal("io.ffem.water", "Fluoride", extras,
                2, 2, true)
        extras.clear()
        testFreeChlorine()
        testTotalIron()
        testEnd()
    }

    private fun testBoron() {
        onView(ViewMatchers.withText(Matchers.startsWith("Available Boron"))).perform(ViewActions.swipeLeft())
    }

    private fun test_pH() {
        onView(ViewMatchers.withText(Matchers.startsWith("pH"))).perform(ViewActions.swipeLeft())
    }

    private fun testFreeChlorine() {
        onView(ViewMatchers.withText(Matchers.startsWith("Free Chlorine"))).perform(ViewActions.swipeLeft())
    }

    @Suppress("SameParameterValue")
    private fun testExternal(action: String, title: String, extras: ArrayList<String>,
                             testIndex: Int, buttonIndex: Int, goNext: Boolean) {
        Mockito.`when`(activityAvailability.isActivityAvailable(ArgumentMatchers.any(Intent::class.java)))
                .thenReturn(false)
        onView(withIndex(ViewMatchers.withText(title), buttonIndex)).perform(ViewActions.click())
        onView(ViewMatchers.withId(android.R.id.button2)).perform(ViewActions.click())
        Mockito.`when`(activityAvailability.isActivityAvailable(ArgumentMatchers.any(Intent::class.java)))
                .thenReturn(true)
        openWidgetList()
        onView(ViewMatchers.withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(testIndex, ViewActions.click()))

        // Replace with Intent value:
        val exStringWidgetSecondText = randomString()
        val stringIntent = Intent()
        for (extra in extras) {
            stringIntent.putExtra(extra + "", exStringWidgetSecondText)
        }
        val exStringResult = Instrumentation.ActivityResult(Activity.RESULT_OK, stringIntent)
        when (extras.size) {
            1 -> Intents.intending(Matchers.allOf(
                    IntentMatchers.hasAction(action),
                    IntentMatchers.hasExtra<Any?>(extras[0] + "", null)
            )).respondWith(exStringResult)
            2 -> Intents.intending(Matchers.allOf(
                    IntentMatchers.hasAction(action),
                    IntentMatchers.hasExtra<Any?>(extras[0] + "", null),
                    IntentMatchers.hasExtra<Any?>(extras[1] + "", null)
            )).respondWith(exStringResult)
            3 -> Intents.intending(Matchers.allOf(
                    IntentMatchers.hasAction(action),
                    IntentMatchers.hasExtra<Any?>(extras[0] + "", null),
                    IntentMatchers.hasExtra<Any?>(extras[1] + "", null),
                    IntentMatchers.hasExtra<Any?>(extras[2] + "", null)
            )).respondWith(exStringResult)
            4 -> Intents.intending(Matchers.allOf(
                    IntentMatchers.hasAction(action),
                    IntentMatchers.hasExtra<Any?>(extras[0] + "", null),
                    IntentMatchers.hasExtra<Any?>(extras[1] + "", null),
                    IntentMatchers.hasExtra<Any?>(extras[2] + "", null),
                    IntentMatchers.hasExtra<Any?>(extras[3] + "", null)
            )).respondWith(exStringResult)
        }
        Mockito.`when`(activityAvailability.isActivityAvailable(ArgumentMatchers.any(Intent::class.java)))
                .thenReturn(true)
        if (extras.size == 1 && extras[0] == "value") {
            onView(withIndex(ViewMatchers.withId(R.id.simple_button), buttonIndex)).perform(ViewActions.click())
            onView(ViewMatchers.withText(exStringWidgetSecondText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        } else {
            onView(Matchers.allOf(ViewMatchers.withText(title), ViewMatchers.hasSibling(ViewMatchers.withText(title)))).perform(ViewActions.click())
            onView(withIndex(ViewMatchers.withText(extras[0]
                    .replace("_", " ") + ": "), 0)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            onView(withIndex(ViewMatchers.withText(exStringWidgetSecondText), 0)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
        openWidgetList()
        onView(ViewMatchers.withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(testIndex, ViewActions.click()))
        if (extras.size == 1 && extras[0] == "value") {
            onView(ViewMatchers.withText(exStringWidgetSecondText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        } else {
            onView(withIndex(ViewMatchers.withText(exStringWidgetSecondText), 0)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            onView(withIndex(ViewMatchers.withText(extras[0]
                    .replace("_", " ") + ": "), 0)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
        if (goNext) {
            onView(withIndex(ViewMatchers.withText(Matchers.startsWith(title)), 0)).perform(ViewActions.swipeLeft())
        }
    }

    private fun testTotalIron() {
        onView(ViewMatchers.withText(Matchers.startsWith("Total Iron"))).perform(ViewActions.swipeLeft())
    }

    private fun openWidgetList() {
        SystemClock.sleep(1000)
        onView(ViewMatchers.withId(R.id.menu_goto)).perform(ViewActions.click())
        SystemClock.sleep(1000)
    }

    private fun randomString(): String {
        return RandomString.make()
    }

    private fun testEnd() {
        onView(ViewMatchers.withText(activityTestRule.activity.getString(R.string.save_enter_data_description,
                "Automated Testing"))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText(R.string.save_form_as)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText(R.string.mark_finished)).perform(ViewActions.click())
        onView(ViewMatchers.withText(R.string.quit_entry)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    class AutoFormTestRule internal constructor() : FormActivityTestRule(AUTO_TEST_FORM)
    companion object {

        private const val AUTO_TEST_FORM = "auto-test.xml"

        fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                var currentIndex = 0
                override fun describeTo(description: Description) {
                    description.appendText("with index: ")
                    description.appendValue(index)
                    matcher.describeTo(description)
                }

                public override fun matchesSafely(view: View?): Boolean {
                    return matcher.matches(view) && currentIndex++ == index
                }
            }
        }

        @JvmStatic
        @BeforeClass
        fun initialize() {
            if (!TestHelper.isDeviceInitialized()) {
                mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            }
        }
    }
}