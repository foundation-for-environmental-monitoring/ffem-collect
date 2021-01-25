package io.ffem.collect.android.util;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Installation related utility methods.
 */
public final class ApkHelper {

    private ApkHelper() {
    }

    /**
     * Checks if the app was installed from the app store or from an install file.
     * source: http://stackoverflow.com/questions/37539949/detect-if-an-app-is-installed-from-play-store
     *
     * @param context The context
     * @return True if app was not installed from the store
     */
    public static boolean isNonStoreVersion(@NonNull Context context) {

        // Valid installer package names
        List<String> validInstallers = new ArrayList<>(
                Arrays.asList("com.android.vending", "com.google.android.feedback"));

        try {
            // The package name of the app that has installed the app
            final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

            // true if the app has been downloaded from Play Store
            return installer == null || !validInstallers.contains(installer);

        } catch (Exception ignored) {
            // do nothing
        }

        return true;
    }
}
