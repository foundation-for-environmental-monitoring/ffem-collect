package io.ffem.collect.android;

import android.Manifest;
import android.app.Instrumentation.ActivityResult;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.GrantPermissionRule;

import net.bytebuddy.utility.RandomString;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.utilities.ActivityAvailability;

import java.util.ArrayList;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test that runs through a form with all question types.
 * <p>
 * <a href="https://docs.fastlane.tools/actions/screengrab/"> screengrab </a> is used to generate screenshots for
 * documentation and releases. Calls to Screengrab.screenshot("image-name") trigger screenshot
 * creation.
 */
public class AutoFormTest {

    @ClassRule
    public static final LocaleTestRule LOCALE_TEST_RULE = new LocaleTestRule();
    private static final String AUTO_TEST_FORM = "auto-test.xml";
    public final AutoFormTestRule activityTestRule = new AutoFormTestRule();
    private final ActivityAvailability activityAvailability = mock(ActivityAvailability.class);
    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule(new AppDependencyModule() {
                @Override
                public ActivityAvailability providesActivityAvailability(Context context) {
                    return activityAvailability;
                }
            }))
            .around(new CopyFormRule(AUTO_TEST_FORM))
            .around(activityTestRule);

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    @Test
    public void testActivityOpen() {

        int textIndex = 0;

        ArrayList<String> extras = new ArrayList<>();

        onView(withText(R.string.form_forward)).perform(click());

        extras.add("value");
        testExternal("io.ffem.water", "Arsenic (0 - 500)", extras,
                textIndex++, 0, false);
        extras.clear();

        extras.add("value");
        testExternal("io.ffem.water", "Arsenic (0 - 4000)", extras,
                textIndex, 1, true);
        extras.clear();

        test_pH();

        testBoron();

        extras.add("value");
        testExternal("io.ffem.soil", "Calcium and Magnesium", extras,
                4, 0, true);
        extras.clear();

        onView(withText(R.string.form_forward)).perform(click());

        extras.add("value");
        testExternal("io.ffem.water", "Fluoride", extras,
                2, 2, true);
        extras.clear();

        testFreeChlorine();

        testTotalIron();

        testEnd();
    }

    private void testBoron() {
        onView(withText(startsWith("Available Boron"))).perform(swipeLeft());
    }

    private void test_pH() {
        onView(withText(startsWith("pH"))).perform(swipeLeft());
    }

    private void testFreeChlorine() {
        onView(withText(startsWith("Free Chlorine"))).perform(swipeLeft());
    }

    private void testExternal(String action, String title, ArrayList<String> extras,
                              int testIndex, int buttonIndex, boolean goNext) {

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(false);

        onView(withIndex(withText(R.string.launch_app), buttonIndex)).perform(click());

        onView(withId(android.R.id.button2)).perform(click());

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        openWidgetList();
        onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(testIndex, click()));

        // Replace with Intent value:
        String exStringWidgetSecondText = randomString();

        Intent stringIntent = new Intent();
        for (String extra : extras) {
            stringIntent.putExtra(extra + "", exStringWidgetSecondText);
        }

        ActivityResult exStringResult = new ActivityResult(RESULT_OK, stringIntent);

        switch (extras.size()) {
            case 1:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + "", null)
                )).respondWith(exStringResult);
                break;
            case 2:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + "", null),
                        hasExtra(extras.get(1) + "", null)
                )).respondWith(exStringResult);
                break;
            case 3:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + "", null),
                        hasExtra(extras.get(1) + "", null),
                        hasExtra(extras.get(2) + "", null)
                )).respondWith(exStringResult);
                break;
            case 4:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + "", null),
                        hasExtra(extras.get(1) + "", null),
                        hasExtra(extras.get(2) + "", null),
                        hasExtra(extras.get(3) + "", null)
                )).respondWith(exStringResult);
                break;
        }

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        if (extras.size() == 1 && extras.get(0).equals("value")) {
            onView(withIndex(withId(R.id.simple_button), buttonIndex)).perform(click());
            onView(withText(exStringWidgetSecondText)).check(matches(isDisplayed()));
        } else {
            onView(allOf(withText(R.string.launch_app), hasSibling(withText(title)))).perform(click());
            onView(withIndex(withText(extras.get(0)
                    .replace("_", " ") + ": "), 0)).check(matches(isDisplayed()));
            onView(withIndex(withText(exStringWidgetSecondText), 0)).check(matches(isDisplayed()));
        }

        openWidgetList();
        onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(testIndex, click()));

        if (extras.size() == 1 && extras.get(0).equals("value")) {
            onView(withText(exStringWidgetSecondText)).check(matches(isDisplayed()));
        } else {
            onView(withIndex(withText(exStringWidgetSecondText), 0)).check(matches(isDisplayed()));
            onView(withIndex(withText(extras.get(0)
                    .replace("_", " ") + ": "), 0)).check(matches(isDisplayed()));
        }

        if (goNext) {
            onView(withIndex(withText(startsWith(title)), 0)).perform(swipeLeft());
        }
    }

    private void testTotalIron() {
        onView(withText(startsWith("Total Iron"))).perform(swipeLeft());
    }

    private void openWidgetList() {
        SystemClock.sleep(1000);
        onView(withId(R.id.menu_goto)).perform(click());
        SystemClock.sleep(1000);
    }

    private String randomString() {
        return RandomString.make();
    }

    private void testEnd() {
        onView(withText(activityTestRule.getActivity().getString(R.string.save_enter_data_description,
                "Automated Testing"))).check(matches(isDisplayed()));
        onView(withText(R.string.save_form_as)).check(matches(isDisplayed()));
        onView(withText(R.string.mark_finished)).perform(click());
        onView(withText(R.string.quit_entry)).check(matches(isDisplayed()));
    }

    private static class AutoFormTestRule extends FormActivityTestRule {
        AutoFormTestRule() {
            super(AUTO_TEST_FORM);
        }
    }
}
