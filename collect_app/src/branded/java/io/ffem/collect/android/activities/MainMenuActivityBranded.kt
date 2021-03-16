package io.ffem.collect.android.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.ffem.collect.android.util.ApkHelper.isNonStoreVersion
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormDownloadListActivity
import org.odk.collect.android.application.Collect
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.configure.qr.QRCodeDecoder
import org.odk.collect.android.gdrive.GoogleDriveActivity
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.utilities.MultiClickGuard
import org.odk.collect.android.utilities.PlayServicesChecker
import org.odk.collect.android.utilities.WebCredentialsUtils
import java.util.*
import javax.inject.Inject

open class MainMenuActivityBranded : AppUpdateActivity() {

    @Inject
    lateinit var webCredentialsUtils: WebCredentialsUtils

    @Inject
    lateinit var qrCodeDecoder: QRCodeDecoder

    @Inject
    lateinit var settingsImporter: SettingsImporter

    private var toolbar: Toolbar? = null

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            getBlankForm()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Collect.getInstance().component.inject(this)

        if (hasAppVersionExpired()) {
            return
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,
                IntentFilter("DOWNLOAD_FORMS_ACTION")
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, 100)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hasAppVersionExpired(): Boolean {
        if (BuildConfig.BUILD_TYPE == "release" && isNonStoreVersion(this)) {
            val appExpiryDate = GregorianCalendar.getInstance()
            appExpiryDate.time = BuildConfig.BUILD_TIME
            appExpiryDate.add(Calendar.DAY_OF_YEAR, 15)

            if (GregorianCalendar().after(appExpiryDate)) {
                val marketUrl =
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                val message = String.format(
                        "%s%n%n%s", getString(R.string.version_has_expired),
                        getString(R.string.uninstall_install_from_store)
                )

                val builder: AlertDialog.Builder = AlertDialog.Builder(this, themeUtils.materialDialogTheme)

                builder.setTitle(R.string.version_expired)
                        .setMessage(message)
                        .setCancelable(false)

                builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = marketUrl
                        intent.setPackage("com.android.vending")
                        startActivity(intent)
                    } catch (e: Exception) {
                    }
                    finishAndRemoveTask()
                }

                val alertDialog = builder.create()
                alertDialog.show()
                return true
            }
        }
        return false
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    fun getBlankForm() {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val protocol = sharedPreferences.getString(
                    GeneralKeys.KEY_PROTOCOL, getString(R.string.protocol_odk_default))
            val i: Intent = if (protocol.equals(getString(R.string.protocol_google_sheets), ignoreCase = true)) {
                if (PlayServicesChecker().isGooglePlayServicesAvailable(this)) {
                    Intent(applicationContext,
                            GoogleDriveActivity::class.java)
                } else {
                    PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(this)
                    return
                }
            } else {
                Intent(applicationContext, FormDownloadListActivity::class.java)
                //                i.putExtra("isDownloadForms", true);
            }
            startActivity(i)
        }
    }
}