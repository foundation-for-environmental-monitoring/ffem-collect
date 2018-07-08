/*
 * Copyright 2016 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;

import timber.log.Timber;

/**
 * Used to present auth dialog and update credentials in the system as needed.
 */
public class AuthDialogUtility {
    private static final String TAG = "AuthDialogUtility";

    private EditText username;
    private EditText password;

    public static void setWebCredentialsFromPreferences() {
        String username = getUserNameFromPreferences();
        String password = getPasswordFromPreferences();

        if (username == null || username.isEmpty()) {
            return;
        }

        if (password == null || password.isEmpty()) {
            return;
        }

        String host = Uri.parse(getServerFromPreferences()).getHost();
        WebUtils.addCredentials(username, password, host);
    }

    public AlertDialog createDialog(final Context context,
                                    final AuthDialogUtilityResultListener resultListener, String url) {

        final View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.server_auth_dialog, null);

        String overriddenUrl = null;
        if (url != null) {
            if (!url.startsWith(getServerFromPreferences())) {
                overriddenUrl = url;
                if (overriddenUrl.contains("?deviceID=")) {
                    overriddenUrl = overriddenUrl.substring(0, overriddenUrl.indexOf("?deviceID="));
                }
            }
        }

        username = dialogView.findViewById(R.id.username_edit);
        password = dialogView.findViewById(R.id.password_edit);

        String userNameVal = overriddenUrl != null ? null : getUserNameFromPreferences();
        String passwordVal = overriddenUrl != null ? null : getPasswordFromPreferences();

        username.setText(userNameVal);
        password.setText(passwordVal);

        String invalidMessage = context.getString(R.string.server_auth_credentials,
                overriddenUrl != null ? overriddenUrl : getServerFromPreferences());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.server_requires_auth));
        if ((userNameVal != null && !userNameVal.isEmpty())
                || (passwordVal != null && !passwordVal.isEmpty())) {
            builder.setMessage(invalidMessage);
        }
        builder.setView(dialogView);
        String finalOverriddenUrl = overriddenUrl;
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
        });
        builder.setNegativeButton(context.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Collect.getInstance().getActivityLogger().logAction(this, TAG, "Cancel");

                        closeKeyboard(context, username);
                        closeKeyboard(context, password);

                        resultListener.cancelledUpdatingCredentials();
                    }
                });

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        showKeyboard(context);

        dialog.setOnShowListener(dialogInterface -> {

            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {

                closeKeyboard(context, username);
                closeKeyboard(context, password);

                Collect.getInstance().getActivityLogger().logAction(this, TAG, "OK");

                String userNameValue = username.getText().toString();
                String passwordValue = password.getText().toString();

                if (userNameValue.isEmpty()) {
                    username.requestFocus();
                    username.setError(context.getString(R.string.required_answer_error));
                    return;
                }

                if (passwordValue.isEmpty()) {
                    password.requestFocus();
                    password.setError(context.getString(R.string.required_answer_error));
                    return;
                }

                if (finalOverriddenUrl == null) {
                    saveCredentials(userNameValue, passwordValue);
                    setWebCredentialsFromPreferences();
                } else {
                    setWebCredentials(finalOverriddenUrl);
                }

                resultListener.updatedCredentials();

                dialog.dismiss();

            });
        });
        return dialog;
    }

    private void setWebCredentials(String url) {
        if (username == null || username.getText().toString().isEmpty()) {
            return;
        }

        String host = Uri.parse(url).getHost();
        WebUtils.addCredentials(username.getText().toString(), password.getText().toString(), host);
    }

    private static String getServerFromPreferences() {
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_SERVER_URL);
    }

    public static String getPasswordFromPreferences() {
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_PASSWORD);
    }

    public static String getUserNameFromPreferences() {
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_USERNAME);
    }

    private void saveCredentials(String userName, String password) {
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_USERNAME, userName);
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_PASSWORD, password);
    }

    public interface AuthDialogUtilityResultListener {
        void updatedCredentials();

        void cancelledUpdatingCredentials();
    }

    private void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    /**
     * Hides the keyboard.
     *
     * @param input the EditText for which the keyboard is open
     */
    private void closeKeyboard(Context context, EditText input) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
