package io.ffem.collect.android;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import io.ffem.collect.android.activities.SignInActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RecommendationFormTest2 {

    private static UiDevice mDevice;

    @Rule
    public ActivityTestRule<SignInActivity> mActivityTestRule = new ActivityTestRule<>(
            SignInActivity.class,
            true,
            false);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex;
            int viewObjHash;

            @SuppressLint("DefaultLocale")
            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("with index: %d ", index));
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (matcher.matches(view) && currentIndex++ == index) {
                    viewObjHash = view.hashCode();
                }
                return view.hashCode() == viewObjHash;
            }
        };
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());
        }
    }

    @Before
    public void setUp() {
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().clear().apply();
        mActivityTestRule.launchActivity(new Intent());
    }

    @Test
    public void signInActivityTest() {

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editUsername),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.layoutUsername),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("f"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editUsername), withText("f"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.layoutUsername),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.editUsername), withText("f"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.layoutUsername),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("ff"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.editUsername), withText("ff"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.layoutUsername),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText4.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.editPassword),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.passwordLayout),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText("p"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonSignIn), withText("Sign In"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.authLayout),
                                        3),
                                4)));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.enter_data), withText("Fill Blank Form"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                0)));
        appCompatButton2.perform(scrollTo(), click());

        onView(withText("Fertilizer Recommendation")).perform(click());

        onView(withText("Go To Start")).perform(click());

        onView(withText("Select date")).perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                allOf(withClassName(is("com.android.internal.widget.ButtonBarLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction editText = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                3),
                        2),
                        isDisplayed()));
        editText.perform(replaceText("Test"), closeSoftKeyboard());

        ViewInteraction editText2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                5),
                        2),
                        isDisplayed()));
        editText2.perform(replaceText("123"), closeSoftKeyboard());

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.form_forward_button), withContentDescription("Next"),
                        childAtPosition(
                                allOf(withId(R.id.buttonholder),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                4)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction editText3 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                1),
                        2),
                        isDisplayed()));
        editText3.perform(replaceText("Abc"), closeSoftKeyboard());

        ViewInteraction editText4 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                5),
                        2),
                        isDisplayed()));
        editText4.perform(replaceText("456"), closeSoftKeyboard());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.form_forward_button), withContentDescription("Next"),
                        childAtPosition(
                                allOf(withId(R.id.buttonholder),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                4)),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.form_forward_button), withContentDescription("Next"),
                        childAtPosition(
                                allOf(withId(R.id.buttonholder),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                4)),
                                1),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        ViewInteraction appCompatImageButton4 = onView(
                allOf(withId(R.id.form_forward_button), withContentDescription("Next"),
                        childAtPosition(
                                allOf(withId(R.id.buttonholder),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                4)),
                                1),
                        isDisplayed()));
        appCompatImageButton4.perform(click());

        ViewInteraction button2 = onView(
                allOf(withText("Launch"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        button2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotNull(mDevice.findObject(By.text("Neem Coated Urea")));
        assertNotNull(mDevice.findObject(By.text("482.61")));
        assertNotNull(mDevice.findObject(By.text("Single Superphosphate (16% P2O5 Granulated)")));
        assertNotNull(mDevice.findObject(By.text("525.00")));
        assertNotNull(mDevice.findObject(By.text("Potassium Chloride (Muriate of Potash)")));
        assertNotNull(mDevice.findObject(By.text("91.67")));
        assertNotNull(mDevice.findObject(By.text("Diammonium Phosphate (16:44:0)")));
        assertNotNull(mDevice.findObject(By.text("190.91")));
        assertNotNull(mDevice.findObject(By.text("(All values in kg/ha)")));
        assertNotNull(mDevice.findObject(By.text("Print")));
        UiObject2 save = mDevice.findObject(By.text("Save"));

        save.click();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withIndex(withText("Neem Coated Urea"), 0)).check(matches(isDisplayed()));

        ViewInteraction textView3 = onView(
                allOf(withText("482.61"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText("482.61")));

        ViewInteraction textView4 = onView(
                allOf(withText("Single Superphosphate (16% P2O5 Granulated)"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView4.check(matches(withText("Single Superphosphate (16% P2O5 Granulated)")));

        ViewInteraction textView5 = onView(
                allOf(withText("525.00"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView5.check(matches(withText("525.00")));

        onView(withIndex(withText("Potassium Chloride (Muriate of Potash)"), 0))
                .check(matches(isDisplayed()));

        onView(withIndex(withText("91.67"), 0)).check(matches(isDisplayed()));

        ViewInteraction textView8 = onView(
                allOf(withText("Diammonium Phosphate (16:44:0)"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView8.check(matches(withText("Diammonium Phosphate (16:44:0)")));

        ViewInteraction textView9 = onView(
                allOf(withText("190.91"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView9.check(matches(withText("190.91")));

        onView(withIndex(withText("Neem Coated Urea"), 1)).check(matches(isDisplayed()));

        ViewInteraction textView11 = onView(
                allOf(withText("416.21"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.TableRow.class),
                                        0),
                                1),
                        isDisplayed()));
        textView11.check(matches(withText("416.21")));

        onView(withIndex(withText("Potassium Chloride (Muriate of Potash)"), 1))
                .check(matches(isDisplayed()));

        onView(withIndex(withText("91.67"), 1)).check(matches(isDisplayed()));

        ViewInteraction textView14 = onView(
                allOf(withText("Crop Recommendation"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        textView14.check(matches(withText("Crop Recommendation")));
    }
}
