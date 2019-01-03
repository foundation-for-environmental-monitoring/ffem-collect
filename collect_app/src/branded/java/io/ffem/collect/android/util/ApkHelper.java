package io.ffem.collect.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.ffem.collect.android.common.AppConfig;

/**
 * Installation related utility methods.
 */
public final class ApkHelper {

    private ApkHelper() {
    }

    /**
     * Checks if app version has expired and if so displays an expiry message and closes activity.
     *
     * @param activity The activity
     * @return True if the app has expired
     */
    public static boolean isAppVersionExpired(@NonNull final Activity activity) {
        if (AppConfig.APP_EXPIRY && isNonStoreVersion(activity)) {
            final Uri marketUrl = Uri.parse("market://details?id=" + activity.getPackageName());

            final GregorianCalendar appExpiryDate = new GregorianCalendar(AppConfig.APP_EXPIRY_YEAR,
                    AppConfig.APP_EXPIRY_MONTH - 1, AppConfig.APP_EXPIRY_DAY);

            appExpiryDate.add(Calendar.DAY_OF_MONTH, 1);

            GregorianCalendar now = new GregorianCalendar();
            if (now.after(appExpiryDate)) {

                String message = String.format("%s%n%n%s", activity.getString(R.string.thisVersionHasExpired),
                        activity.getString(R.string.uninstallAndInstallFromStore));

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(activity);

                builder.setTitle(R.string.versionExpired)
                        .setMessage(message)
                        .setCancelable(false);

                builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, marketUrl));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        activity.finishAndRemoveTask();
                    } else {
                        activity.finish();
                    }
                });

                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        }
        return false;
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
