package io.ffem.collect.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import io.ffem.collect.android.helper.AppHelper.getVersionedAppName
import io.ffem.collect.android.helper.AppHelper.isTestDevice
import org.odk.collect.android.R
import org.odk.collect.android.activities.WebViewActivity
import org.odk.collect.android.utilities.CustomTabHelper

/**
 * Activity to display info about the app.
 */
class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_layout)
        initToolbar()
        findViewById<TextView>(R.id.version_txt).text = getVersionedAppName(this)
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        title = getString(R.string.about_preferences)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * Displays legal information.
     */
    fun onSoftwareNoticesClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        if (!isTestDevice(this)) {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(CustomTabHelper.OPEN_URL, LICENSES_HTML_PATH)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val LICENSES_HTML_PATH = "file:///android_asset/open_source_licenses.html"
    }
}