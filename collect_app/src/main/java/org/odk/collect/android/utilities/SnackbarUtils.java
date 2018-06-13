/*

Copyright 2018 Shobhit
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.odk.collect.android.utilities;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import io.ffem.collect.android.util.ApiUtil;

public final class SnackbarUtils {
    public static final int LONG_DURATION_MS = 3500;
    private static final float SNACK_BAR_LINE_SPACING = 1.4f;

    private SnackbarUtils() {

    }

    /**
     * Displays snackbar with {@param message} for {@link SnackbarUtils#LONG_DURATION_MS}
     * and multi-line message enabled.
     *
     * @param view    The view to find a parent from.
     * @param message The text to show.  Can be formatted text.
     */
    public static void showSnackbar(@NonNull View view, @NonNull String message) {
        if (message.isEmpty()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(view, message.trim(), LONG_DURATION_MS);
        TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setSingleLine(false);
        snackbar.show();
    }

    /**
     * Displays snackbar with settings button and {@param message}
     *
     * @param rootView The root view of the activity.
     * @param message  The text to show.
     */
    public static void showSettingsSnackbar(Activity activity, View rootView, String message) {

        Snackbar snackbar = Snackbar
                .make(rootView, message.trim(), LONG_DURATION_MS)
                .setAction("SETTINGS", view -> ApiUtil.startInstalledAppDetailsActivity(activity));

        View snackbarView = snackbar.getView();

        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);

        textView.setTextColor(Color.WHITE);

        textView.setLineSpacing(0, SNACK_BAR_LINE_SPACING);

        snackbar.setActionTextColor(Color.YELLOW);

        snackbar.show();
    }
}
