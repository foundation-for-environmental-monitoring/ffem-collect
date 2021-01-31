package io.ffem.collect.android.activities

import android.content.Intent
import android.database.Cursor
import android.net.ConnectivityManager
import android.view.View
import androidx.loader.content.Loader
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormListActivity
import org.odk.collect.android.utilities.ToastUtils

abstract class FillBlankFormActivityBranded: FormListActivity() {

    open fun onClickGetBlankForm(view: View?) {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = connectivityManager.activeNetworkInfo
        if (ni == null || !ni.isConnected) {
            ToastUtils.showShortToast(R.string.no_connection)
        } else {
            val localBroadcastManager = LocalBroadcastManager.getInstance(this)
            val localIntent = Intent("DOWNLOAD_FORMS_ACTION")
            localBroadcastManager.sendBroadcast(localIntent)
        }
    }
}