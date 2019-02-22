package io.ffem.collect.android.preferences;

import android.content.Context;

import org.odk.collect.android.R;

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
    }

    public static void disableDiagnosticMode(Context context) {
        PreferencesUtil.setBoolean(context, R.string.diagnosticModeKey, false);
    }
}
