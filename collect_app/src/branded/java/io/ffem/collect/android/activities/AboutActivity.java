package io.ffem.collect.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.WebViewActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.CustomTabHelper;

import io.ffem.collect.android.preferences.AppPreferences;

/**
 * Activity to display info about the app.
 */
public class AboutActivity extends CollectAbstractActivity {

    private static final int CHANGE_MODE_MIN_CLICKS = 10;
    private static final String LICENSES_HTML_PATH = "file:///android_asset/open_source_licenses.html";

    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);
        initToolbar();

        ((TextView) findViewById(R.id.textVersion))
                .setText(Collect.getInstance().getVersionedAppName());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.about_preferences));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Displays legal information.
     */
    public void onSoftwareNoticesClick(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(CustomTabHelper.OPEN_URL, LICENSES_HTML_PATH);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Turn on diagnostic mode if user clicks on version section CHANGE_MODE_MIN_CLICKS times.
     */
    public void switchToDiagnosticMode(View view) {
        if (!AppPreferences.isDiagnosticMode(this)) {
            clickCount++;

            if (clickCount >= CHANGE_MODE_MIN_CLICKS) {
                clickCount = 0;
                Toast.makeText(getBaseContext(), getString(
                        R.string.diagnosticModeEnabled), Toast.LENGTH_SHORT).show();
                AppPreferences.enableDiagnosticMode(this);

                changeActionBarStyleBasedOnCurrentMode();

                switchLayoutForDiagnosticOrUserMode();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchLayoutForDiagnosticOrUserMode();
    }

    /**
     * Show the diagnostic mode layout.
     */
    private void switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode(this)) {
            findViewById(R.id.layoutDiagnostics).setVisibility(View.VISIBLE);
        } else {
            if (findViewById(R.id.layoutDiagnostics).getVisibility() == View.VISIBLE) {
                findViewById(R.id.layoutDiagnostics).setVisibility(View.GONE);
            }
        }
    }

    /**
     * Disables diagnostic mode.
     */
    public void disableDiagnosticsMode(View view) {
        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode(this);

        switchLayoutForDiagnosticOrUserMode();

        changeActionBarStyleBasedOnCurrentMode();
    }
}
