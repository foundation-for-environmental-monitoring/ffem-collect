package io.ffem.collect.android.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import io.ffem.collect.android.preferences.AppPreferences
import io.ffem.collect.android.preferences.OtherPreferenceFragment
import io.ffem.collect.android.preferences.TestingPreferenceFragment
import org.odk.collect.android.R
import org.odk.collect.android.activities.CollectAbstractActivity
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog.ResetSettingsResultDialogListener
import org.odk.collect.android.listeners.OnBackPressedListener
import org.odk.collect.android.preferences.ServerPreferences
import org.odk.collect.android.preferences.screens.AdminPreferencesFragment
import org.odk.collect.android.preferences.screens.FormManagementPreferencesFragment

class PreferencesActivity : CollectAbstractActivity(), ResetSettingsResultDialogListener {
    private var onBackPressedListener: OnBackPressedListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
    }

    public override fun onRestart() {
        super.onRestart()
        setupActivity()
    }

    private fun setupActivity() {
        setTitle(R.string.settings)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction()
            .replace(R.id.layoutFormManagement, FormManagementPreferencesFragment())
            .commitAllowingStateLoss()
        supportFragmentManager.beginTransaction()
            .replace(R.id.layoutInfo, OtherPreferenceFragment())
            .commitAllowingStateLoss()
        supportFragmentManager.beginTransaction()
            .replace(R.id.layoutServer, ServerPreferences())
            .commitAllowingStateLoss()
        supportFragmentManager.beginTransaction()
            .replace(R.id.layoutAdmin, AdminPreferencesFragment())
            .commitAllowingStateLoss()
        if (AppPreferences.isDiagnosticMode(this)) {
            supportFragmentManager.beginTransaction()
                .add(R.id.layoutTesting, TestingPreferenceFragment())
                .commitAllowingStateLoss()
            findViewById<View>(R.id.layoutTesting).visibility = View.VISIBLE
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        try {
            setSupportActionBar(toolbar)
        } catch (ignored: Exception) {
        }
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setTitle(R.string.settings)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (AppPreferences.isDiagnosticMode(this)) {
            menuInflater.inflate(R.menu.menu_settings, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDialogClosed() {
        finish()
    }

    fun onDisableDiagnostics(item: MenuItem?) {
        Toast.makeText(
            baseContext, getString(R.string.diagnosticModeDisabled),
            Toast.LENGTH_SHORT
        ).show()
        AppPreferences.disableDiagnosticMode(this)

//        changeActionBarStyleBasedOnCurrentMode();
        invalidateOptionsMenu()
        removeAllFragments()
    }

    private fun removeAllFragments() {
        findViewById<View>(R.id.layoutTesting).visibility = View.GONE
    }

    fun setOnBackPressedListener(onBackPressedListener: OnBackPressedListener?) {
        this.onBackPressedListener = onBackPressedListener
    }
}