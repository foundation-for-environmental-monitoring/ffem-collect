package io.ffem.collect.android.activities


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import io.ffem.collect.android.util.TestHelper.clearPreferences
import io.ffem.collect.android.util.TestUtil.sleep
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.runner.RunWith
import org.odk.collect.android.R

@LargeTest
@RunWith(AndroidJUnit4::class)
class SignInActivityTest {

    @get:Rule
    val mActivityTestRule = activityScenarioRule<SignInActivity>()

    @Before
    fun setUp() {
        clearPreferences()
    }

    @Test
    fun signInActivityTest() {
        val appCompatEditText = onView(
                allOf(withId(R.id.editUsername),
                        isDisplayed()))

        sleep(2000)

        appCompatEditText.perform(replaceText("test"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
                allOf(withId(R.id.editPassword),
                        isDisplayed()))
        appCompatEditText2.perform(replaceText("test"), closeSoftKeyboard())

        val materialButton = onView(
                allOf(withId(R.id.buttonSignIn), withText("Sign In"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.authLayout),
                                        3),
                                4)))
        materialButton.perform(scrollTo(), click())

        val multiClickSafeButton = onView(
                allOf(withId(R.id.enter_data), withText("Fill Blank Form"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(`is`("android.widget.LinearLayout")),
                                        1),
                                0)))
        multiClickSafeButton.perform(scrollTo(), click())

        val materialButton2 = onView(
                allOf(withId(R.id.get_forms), withText("Get Blank Form"),
                        childAtPosition(
                                allOf(withId(android.R.id.empty),
                                        childAtPosition(
                                                withId(R.id.llParent),
                                                1)),
                                0),
                        isDisplayed()))
        materialButton2.perform(click())

        val appCompatButton = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)))
        appCompatButton.perform(scrollTo(), click())

        val appCompatButton2 = onView(
                allOf(withId(android.R.id.button2), withText("Cancel"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                2)))
        appCompatButton2.perform(scrollTo(), click())

        pressBack()

        val multiClickSafeButton2 = onView(
                allOf(withId(R.id.get_forms), withText("Get Blank Form"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(`is`("android.widget.LinearLayout")),
                                        1),
                                3)))
        multiClickSafeButton2.perform(scrollTo(), click())

        val appCompatButton3 = onView(
                allOf(withId(android.R.id.button2), withText("Cancel"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                2)))
        appCompatButton3.perform(scrollTo(), click())

        sleep(2000)

        val actionMenuItemView = onView(
                allOf(withId(R.id.menu_settings), withContentDescription("Settings"),
                        isDisplayed()))

        sleep(2000)

        actionMenuItemView.perform(click())

        sleep(2000)

        onView(withText("Server credentials")).perform(click())

        val materialButton3 = onView(
                allOf(withId(R.id.buttonSignIn), withText("Sign In"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.authLayout),
                                        3),
                                4)))
        materialButton3.perform(scrollTo(), click())

        sleep(2000)

        onView(
                allOf(withId(R.id.menu_settings), withContentDescription("Settings"),
                        isDisplayed())).perform(click())

        sleep(2000)

        onView(withText("About")).perform(click())

        val appCompatTextView = onView(
                allOf(withId(R.id.textLinkSoftwareNotices), withText("Legal Information"),
                        isDisplayed()))
        appCompatTextView.perform(click())

        val appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("android.widget.RelativeLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val appCompatImageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withId(R.id.appBarLayout),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton2.perform(click())

        pressBack()
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    companion object {

        @JvmStatic
        private lateinit var context: Context

        @JvmStatic
        @AfterClass
        fun teardown() {
            clearPreferences()
        }

        @JvmStatic
        @BeforeClass
        fun initialize() {
            context = InstrumentationRegistry.getInstrumentation().targetContext
        }
    }
}
