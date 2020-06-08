package io.ffem.collect.android.preferences;

import android.content.Context;

import org.odk.collect.android.R;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import io.ffem.collect.android.util.PreferencesUtil;

/**
 * Static functions to get or set values of various preferences.
 */
public final class AppPreferences {

    private AppPreferences() {
    }

    public static boolean isDiagnosticMode(Context context) {
        return PreferencesUtil.getBoolean(context, R.string.diagnosticModeKey, false);
    }

    public static void enableDiagnosticMode(Context context) {
        PreferencesUtil.setBoolean(context, R.string.diagnosticModeKey, true);
        PreferencesUtil.setLong(
                context, R.string.diagnosticEnableTimeKey,
                Calendar.getInstance().getTimeInMillis()
        );
    }

    public static void disableDiagnosticMode(Context context) {
        PreferencesUtil.setBoolean(context, R.string.diagnosticModeKey, false);
    }

    public static boolean launchExperiment(Context context) {
        return isDiagnosticMode(context) && PreferencesUtil.getBoolean(context,
                R.string.launchExperimentKey, false);
    }

    public static void checkDiagnosticModeExpiry(Context context) {
        if (isDiagnosticMode(context)) {
            long lastCheck = PreferencesUtil.getLong(context, R.string.diagnosticEnableTimeKey);
            if (TimeUnit.MILLISECONDS.toMinutes(Calendar.getInstance().getTimeInMillis() - lastCheck) > 20) {
                disableDiagnosticMode(context);
            } else {
                PreferencesUtil.setLong(
                        context, R.string.diagnosticEnableTimeKey,
                        Calendar.getInstance().getTimeInMillis()
                );
            }
        }
    }
}
