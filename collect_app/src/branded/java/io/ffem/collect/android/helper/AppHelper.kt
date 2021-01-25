package io.ffem.collect.android.helper

import android.content.Context
import android.provider.Settings
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R

object AppHelper {

    @JvmStatic
    fun getVersionedAppName(context: Context): String {
        return String.format("%s %s", context.getString(R.string.version), BuildConfig.VERSION_NAME)
    }

    fun isTestDevice(context: Context): Boolean {
        try {
            val testLabSetting: String =
                    Settings.System.getString(context.contentResolver, "firebase.test.lab")
            return "true" == testLabSetting
        } catch (ignored: Exception) {
            // do nothing
        }
        return false
    }
}