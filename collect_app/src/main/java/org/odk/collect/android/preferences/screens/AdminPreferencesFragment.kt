/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.preferences.screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import org.odk.collect.android.R
import org.odk.collect.android.utilities.MultiClickGuard

class AdminPreferencesFragment :
    BaseAdminPreferencesFragment(), Preference.OnPreferenceClickListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.admin_preferences, rootKey)
        findPreference<Preference>("odk_preferences")!!.onPreferenceClickListener = this
        findPreference<Preference>("main_menu")!!.onPreferenceClickListener = this
        findPreference<Preference>("user_settings")!!.onPreferenceClickListener = this
        findPreference<Preference>("form_entry")!!.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                "odk_preferences" -> {
                    val intent = Intent(activity, GeneralPreferencesActivity::class.java)
                    intent.putExtra(GeneralPreferencesActivity.INTENT_KEY_ADMIN_MODE, true)
                    startActivity(intent)
                }
                "main_menu" -> displayPreferences(MainMenuAccessPreferencesFragment())
                "user_settings" -> displayPreferences(UserSettingsAccessPreferencesFragment())
                "form_entry" -> displayPreferences(FormEntryAccessPreferencesFragment())
            }
            return true
        }
        return false
    }

    private fun displayPreferences(fragment: Fragment?) {
        if (fragment != null) {
            fragment.arguments = arguments
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.preferences_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    fun preventOtherWaysOfEditingForm() {
        val fragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.preferences_fragment_container) as FormEntryAccessPreferencesFragment
        fragment.preventOtherWaysOfEditingForm()
    }
}
