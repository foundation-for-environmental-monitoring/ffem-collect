package io.ffem.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.AuthDialogUtility;

import io.ffem.collect.android.util.AdjustingViewGlobalLayoutListener;

import static org.odk.collect.android.utilities.PermissionUtils.requestStoragePermissions;

/**
 * Sign in screen shown on app first launch
 */
public class SignInActivity extends Activity {
    private static final boolean EXIT = true;

    private EditText editText;
    private EditText editPassword;
    private TextInputLayout layoutUserName;
    private TextInputLayout layoutPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestStoragePermissions(this, new PermissionListener() {
            @Override
            public void granted() {
                // must be at the beginning of any activity that can be called from an external intent
                try {
                    Collect.createODKDirs();
                    Collect.getInstance().getActivityLogger().open();
                } catch (RuntimeException e) {
                    createErrorDialog(e.getMessage(), EXIT);
                }
            }

            @Override
            public void denied() {
                // The activity has to finish because ODK Collect cannot function without these permissions.
                finish();
            }
        });
        if (isUserSignedIn()) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        }

        setContentView(R.layout.activity_signin);

        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    private void initialize() {
        editText = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);

        findViewById(R.id.textCreateAccount).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getResources().getString(R.string.create_account_link)));
            startActivity(intent);
        });

        findViewById(R.id.textForgot).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getResources().getString(R.string.reset_password_link)));
            startActivity(intent);
        });

        findViewById(R.id.buttonSignIn).setOnClickListener(view -> {
            if (isInputValid()) {
                GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_USERNAME,
                        editText.getText().toString().trim());
                GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_PASSWORD,
                        editPassword.getText().toString().trim());

                AuthDialogUtility.setWebCredentialsFromPreferences();

                startActivity(new Intent(getBaseContext(), MainMenuActivity.class));
                finish();
            }
        });

        layoutUserName = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.passwordLayout);

        AdjustingViewGlobalLayoutListener listen =
                new AdjustingViewGlobalLayoutListener(findViewById(R.id.authLayout));
        getWindow().getDecorView().getRootView().getViewTreeObserver().addOnGlobalLayoutListener(listen);
    }

    private boolean isInputValid() {
        String username = editText.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty()) {
            layoutUserName.setErrorEnabled(true);
            layoutUserName.setError(getResources().getString(R.string.enter_username));
            return false;
        } else {
            layoutUserName.setErrorEnabled(false);
            layoutUserName.setError(null);
        }

        if (password.isEmpty()) {
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError(getResources().getString(R.string.enter_password));
            return false;
        } else {
            layoutPassword.setErrorEnabled(false);
            layoutPassword.setError(null);
        }

        return true;
    }

    private boolean isUserSignedIn() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String storedUsername = settings.getString(PreferenceKeys.KEY_USERNAME, null);
        String storedPassword = settings.getString(PreferenceKeys.KEY_PASSWORD, null);

        return storedUsername != null
                && storedUsername.trim().length() != 0
                && storedPassword != null
                && storedPassword.trim().length() != 0;
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger().logAction(this, "createErrorDialog", "show");
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance().getActivityLogger().logAction(this,
                                "createErrorDialog", "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(getString(R.string.ok), errorListener);
        alertDialog.show();
    }
}