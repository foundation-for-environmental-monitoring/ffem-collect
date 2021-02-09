package io.ffem.collect.android.helper

import android.content.Context
import android.provider.Settings
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R
import timber.log.Timber
import java.util.*

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

    @JvmStatic
    fun getUnitText(appearance: String, question: String, answer: String): String {
        // todo: remove unit hard coding
        if (question.toLowerCase(Locale.ROOT).contains("dilution") ||
                (answer.isNotEmpty() && isAlphanumeric(answer))) {
            return ""
        }
        try {
            var unit = "mg/l"
            val value = appearance.toLowerCase(Locale.ROOT)
            if (value.contains("soil(")) {
                unit = "mg/kg"
            }
            if (value.contains("2636348f3ef2") ||
                    value.contains("9e21e840df93") || value.contains("f5f0b03ff739")) {
                unit = ""
            } else if (value.contains("8afd21b635db")) {
                unit = "M"
            } else if (value.contains("e804cb209ae8")) {
                unit = "%"
            } else if (value.contains("134295bbd289")) {
                unit = "NTU"
            } else if (value.contains("4184f6fc974f")) {
                unit = "kg/ha"
            }

            return String.format(" %s", unit)
        } catch (e: java.lang.Exception) {
            Timber.e(e)
        }
        return ""
    }

    private fun isAlphanumeric(chars: String): Boolean {
        return chars.matches(".*[a-zA-Z]+.*".toRegex())
    }
}