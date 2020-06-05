package io.ffem.collect.android.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.FormManagementPreferences;
import org.odk.collect.android.preferences.ServerPreferencesFragment;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.Objects;

import io.ffem.collect.android.preferences.AdminPreferenceFragment;
import io.ffem.collect.android.preferences.AppPreferences;
import io.ffem.collect.android.preferences.OtherPreferenceFragment;
import io.ffem.collect.android.preferences.TestingPreferenceFragment;

public class SettingsActivity extends CollectAbstractActivity
        implements ResetSettingsResultDialog.ResetSettingsResultDialogListener {

    private static final int PASSWORD_DIALOG = 1;

    private SharedPreferences adminPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();

        adminPreferences = this.getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        setupActivity();
    }

    private void setupActivity() {

        setTitle(R.string.settings);

        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(R.id.layoutFormManagement, new FormManagementPreferences())
                .commit();

        getFragmentManager().beginTransaction()
                .replace(R.id.layoutInfo, new OtherPreferenceFragment())
                .commit();

        getFragmentManager().beginTransaction()
                .replace(R.id.layoutServer, new ServerPreferencesFragment())
                .commit();

        getFragmentManager().beginTransaction()
                .replace(R.id.layoutAdmin, new AdminPreferenceFragment())
                .commit();

        if (AppPreferences.isDiagnosticMode(this)) {
            getFragmentManager().beginTransaction()
                    .add(R.id.layoutTesting, new TestingPreferenceFragment())
                    .commit();

            findViewById(R.id.layoutTesting).setVisibility(View.VISIBLE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        try {
            setSupportActionBar(toolbar);
        } catch (Exception ignored) {
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle(R.string.settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode(this)) {
            getMenuInflater().inflate(R.menu.menu_settings, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PASSWORD_DIALOG:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog passwordDialog = builder.create();
                passwordDialog.setTitle(getString(R.string.enter_admin_password));
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.admin_password_dialog_layout, null);
                passwordDialog.setView(dialogView, 20, 10, 20, 10);
                final CheckBox checkBox = dialogView.findViewById(R.id.checkBox);
                final EditText input = dialogView.findViewById(R.id.editText);
                checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (!checkBox.isChecked()) {
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else {
                        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                });
                passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.ok),
                        (dialog, whichButton) -> {
                            String value = input.getText().toString();
                            String pw = adminPreferences.getString(
                                    AdminKeys.KEY_ADMIN_PW, "");
                            if (pw != null) {
                                if (pw.compareTo(value) == 0) {
                                    Intent i = new Intent(getApplicationContext(),
                                            AdminPreferencesActivity.class);
                                    startActivity(i);
                                    input.setText("");
                                    passwordDialog.dismiss();
                                } else {
                                    ToastUtils.showShortToast(R.string.admin_password_incorrect);
                                }
                            }
                        });

                passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        (dialog, which) -> input.setText(""));

                Objects.requireNonNull(passwordDialog.getWindow()).setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return passwordDialog;

        }
        return null;
    }

    @Override
    public void onDialogClosed() {
        finish();
    }

    public void onDisableDiagnostics(MenuItem item) {
        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode(this);

        changeActionBarStyleBasedOnCurrentMode();

        invalidateOptionsMenu();

        removeAllFragments();
    }

    private void removeAllFragments() {
        findViewById(R.id.layoutTesting).setVisibility(View.GONE);
    }
}
