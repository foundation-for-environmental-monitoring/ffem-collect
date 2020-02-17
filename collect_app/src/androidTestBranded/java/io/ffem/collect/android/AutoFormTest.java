package io.ffem.collect.android;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import net.bytebuddy.utility.RandomString;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.ActivityAvailability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;
import static org.odk.collect.android.application.Collect.APP_FOLDER;

/**
 * Integration test that runs through a form with all question types.
 * <p>
 * <a href="https://docs.fastlane.tools/actions/screengrab/"> screengrab </a> is used to generate screenshots for
 * documentation and releases. Calls to Screengrab.screenshot("image-name") trigger screenshot
 * creation.
 */

@RunWith(AndroidJUnit4.class)
public class AutoFormTest {

    @ClassRule
    public static final LocaleTestRule LOCALE_TEST_RULE = new LocaleTestRule();
    private static final String ALL_WIDGETS_FORM = "auto-test.xml";
    private static final String FORMS_DIRECTORY = File.separator + APP_FOLDER + "/forms/";
    private final Random random = new Random();
    private final ActivityResult okResult = new ActivityResult(RESULT_OK, new Intent());
    @Rule
    public FormEntryActivityTestRule activityTestRule = new FormEntryActivityTestRule();

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    private ActivityAvailability activityAvailability;

    //region Test prep.
    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        String pathname = formPath();
        if (new File(pathname).exists()) {
            return;
        }

        AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
        InputStream inputStream = assetManager.open(ALL_WIDGETS_FORM);

        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    //region Helper methods.
    private static String formPath() {
        return Environment.getExternalStorageDirectory().getPath()
                + FORMS_DIRECTORY
                + ALL_WIDGETS_FORM;
    }

    //endregion

    public static Matcher<View> withProgress(final int expectedProgress) {
        return new BoundedMatcher<View, SeekBar>(SeekBar.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("expected: ");
                description.appendText(String.valueOf(expectedProgress));
            }

            @Override
            public boolean matchesSafely(SeekBar seekBar) {
                return seekBar.getProgress() == expectedProgress;
            }
        };
    }
    //endregion

    //region Widget tests.

    public static ViewAction setProgress(final int progress) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                SeekBar seekBar = (SeekBar) view;
                seekBar.setProgress(progress);
            }

            @Override
            public String getDescription() {
                return "Set a progress on a SeekBar";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }
        };
    }

    public static ViewAction setNumberPickerValue(final int value) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker numberPickerDialog = (NumberPicker) view;
                numberPickerDialog.setValue(value);
            }

            @Override
            public String getDescription() {
                return "Set a value on a Number Picker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
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

    @Before
    public void prepareDependencies() {
        FormEntryActivity activity = activityTestRule.getActivity();
//        activity.setActivityAvailability(activityAvailability);
//        activity.setShouldOverrideAnimations(true);
    }

    //region Main test block.
    @Test
    public void testActivityOpen() {

        int textIndex = 0;

        ArrayList<String> extras = new ArrayList<>();

        skipInitialLabel();

        extras.add("value");
        testExternal("io.ffem.experiment", "Coliforms 1", extras,
                "", ++textIndex, 0, false);
        extras.clear();

        extras.add("Result");
        extras.add("Sample_volume");
        extras.add("Broth");
        extras.add("Time_to_detect");
        testExternal("io.ffem.experiment", "Coliforms 2", extras,
                "", ++textIndex, 0, true);
        extras.clear();

        extras.add("value");
        testExternal("io.ffem.water", "Arsenic (0 - 500)", extras,
                "", ++textIndex, 0, false);
        extras.clear();

        ++textIndex;

        extras.add("Arsenic");
        extras.add("Arsenic_Unit");
        testExternal("io.ffem.water", "Arsenic (0 - 500)", extras,
                "", ++textIndex, 2, true);
        extras.clear();

        testpH();

        testBoron();

        extras.add("Exchangeable_Calcium");
        extras.add("Exchangeable_Magnesium");
        testExternal("io.ffem.soil", "Exchangeable Calcium & Magnesium", extras,
                "_001", 12, 0, true);
        extras.clear();

        extras.add("value");
        testExternal("io.ffem.water", "Fluoride", extras,
                "", 15, 2, true);
        extras.clear();

        testFreeChlorine();

        testTotalIron();

        extras.add("Fluoride");
        extras.add("Fluoride_Unit");
        extras.add("Fluoride_Dilution");
        testExternal("io.ffem.water", "Fluoride - Borewell", extras,
                "_xBorewell", 20, 1, true);
        extras.clear();

        testEnd();

    }

    private void testpH() {
        onView(withText(startsWith("pH"))).perform(swipeLeft());
    }

    private void testBoron() {
        onView(withText(startsWith("Available Boron"))).perform(swipeLeft());
    }

    private void testExternal(String action, String title, ArrayList<String> extras,
                              String suffix, int testIndex, int buttonIndex, boolean goNext) {

//        String exStringWidgetFirstText = randomString();

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(false);

//        try {
//            onView(allOf(withIndex(withText(""), buttonIndex) ,hasSibling(withText("Launch"))))
//                    .perform(setTextInTextView(exStringWidgetFirstText));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        openWidgetList();
        onView(withId(R.id.list))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(testIndex, click()));

//        Screengrab.screenshot("ex-string");

        // Replace with Intent value:
        String exStringWidgetSecondText = randomString();

        Intent stringIntent = new Intent();
        for (String extra : extras) {
            stringIntent.putExtra(extra + suffix, exStringWidgetSecondText);
        }

        ActivityResult exStringResult = new ActivityResult(RESULT_OK, stringIntent);

        switch (extras.size()) {
            case 1:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + suffix, null)
                )).respondWith(exStringResult);
                break;
            case 2:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + suffix, null),
                        hasExtra(extras.get(1) + suffix, null)
                )).respondWith(exStringResult);
                break;
            case 3:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + suffix, null),
                        hasExtra(extras.get(1) + suffix, null),
                        hasExtra(extras.get(2) + suffix, null)
                )).respondWith(exStringResult);
                break;
            case 4:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + suffix, null),
                        hasExtra(extras.get(1) + suffix, null),
                        hasExtra(extras.get(2) + suffix, null),
                        hasExtra(extras.get(3) + suffix, null)
                )).respondWith(exStringResult);
                break;
        }

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        if (extras.size() == 1 && extras.get(0).equals("value")) {
            onView(withIndex(withId(R.id.simple_button), buttonIndex)).perform(click());
            onView(withText(exStringWidgetSecondText)).check(matches(isDisplayed()));
        } else {
            onView(allOf(withText("Launch"), hasSibling(withText(title)))).perform(click());
            onView(withIndex(withText(extras.get(0)
                    .replace("_", " ") + ": "), 0)).check(matches(isDisplayed()));
            onView(withIndex(withText(exStringWidgetSecondText), 0)).check(matches(isDisplayed()));
        }

//        Screengrab.screenshot("ex-string2");

        openWidgetList();
        onView(withId(R.id.list))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(testIndex, click()));

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

    private void testFreeChlorine() {
        onView(withText(startsWith("Free Chlorine"))).perform(swipeLeft());
    }

    private void testTotalIron() {
//        onView(withText(startsWith("Total Iron"))).perform(swipeLeft());
    }

    private void testEnd() {
        onView(withText("You are at the end of Automated Testing.")).check(matches(isDisplayed()));
        onView(withText("Mark form as finalized")).check(matches(isDisplayed()));
        onView(withText("Send Form")).check(matches(isDisplayed()));
        onView(withText("Mark form as finalized")).perform(click());
        onView(withText("Save Form and Exit")).check(matches(isDisplayed()));
    }

    public void skipInitialLabel() {
        onView(withText(startsWith("\nWelcome to automated testing survey form!"))).perform(swipeLeft());
    }

    public void testStringWidget() {
        String stringWidgetText = randomString();

        onVisibleEditText().perform(replaceText(stringWidgetText));

        // captures screenshot of string widget
        Screengrab.screenshot("string-input");

        openWidgetList();
        onView(withText("String widget")).perform(click());

        onVisibleEditText().check(matches(withText(stringWidgetText)));

        onView(withText("String widget")).perform(swipeLeft());
    }

    public void testStringNumberWidget() {
        String stringNumberWidgetText = randomIntegerString();

        onVisibleEditText().perform(replaceText(stringNumberWidgetText));

        Screengrab.screenshot("string-number");

        openWidgetList();

        onView(withText("String number widget")).perform(click());

        onVisibleEditText().check(matches(withText(stringNumberWidgetText)));

        onView(withText("String number widget")).perform(swipeLeft());

    }

    public void testUrlWidget() {
        Uri uri = Uri.parse("http://opendatakit.org/");

        intending(allOf(hasAction(Intent.ACTION_VIEW), hasData(uri)))
                .respondWith(okResult);

        Screengrab.screenshot("url");

        onView(withId(R.id.simple_button)).perform(click());
        onView(withText("URL widget")).perform(swipeLeft());
    }

    public void testLabelWidget() {

        Screengrab.screenshot("label-widget");

        onView(withText("Label widget")).perform(swipeLeft());
    }


    public void testSubmission() {

    }

    private ViewInteraction onVisibleEditText() {
        return onView(withClassName(endsWith("EditText")));
    }

    private void openWidgetList() {
        onView(withId(R.id.menu_goto)).perform(click());
    }

    // private void saveForm() {
    //    onView(withId(R.id.menu_save)).perform(click());
    // }

    private String randomString() {
        return RandomString.make();
    }

    private int randomInt() {
        return Math.abs(random.nextInt());
    }

    private String randomIntegerString() {
        String s = Integer.toString(randomInt());
        while (s.length() > 9) {
            s = s.substring(1);
        }

        // Make sure the result is a valid Integer String:
        return Integer.toString(Integer.parseInt(s));
    }

    //    private ActivityResult cancelledResult() {
    //        return new ActivityResult(RESULT_CANCELED, null);
    //    }
    //
    //    private ActivityResult okResult(@Nullable Intent data) {
    //        return new ActivityResult(RESULT_OK, data);
    //    }

    //endregion

    public Activity getActivityInstance() {
        final Activity[] currentActivity = new Activity[1];
        getInstrumentation().runOnMainSync(() -> {
            Collection resumedActivities = ActivityLifecycleMonitorRegistry
                    .getInstance().getActivitiesInStage(Stage.RESUMED);
            if (resumedActivities.iterator().hasNext()) {
                currentActivity[0] = (Activity) resumedActivities.iterator().next();
            }
        });

        return currentActivity[0];
    }
    //endregion

    //region Custom TestRule.
    private class FormEntryActivityTestRule extends IntentsTestRule<FormEntryActivity> {

        FormEntryActivityTestRule() {
            super(FormEntryActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Context context = getInstrumentation().getTargetContext();
            Intent intent = new Intent(context, FormEntryActivity.class);

            intent.putExtra(EXTRA_TESTING_PATH, formPath());

            return intent;
        }
    }

}
