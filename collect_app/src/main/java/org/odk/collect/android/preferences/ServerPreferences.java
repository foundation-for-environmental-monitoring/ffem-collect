/*
 * Copyright 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.odk.collect.android.R;

import io.ffem.collect.android.activities.SignInActivity;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;

public class ServerPreferences extends ServerPreferencesFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        addPreferencesFromResource(R.xml.server_preferences);

        initProtocolPrefs();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_row, container, false);

        Preference serverPreferences = findPreference("server_credentials");
        if (serverPreferences != null) {
            serverPreferences.setSummary(R.string.sign_in_to_account);
            serverPreferences.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(getActivity(), SignInActivity.class);
                intent.putExtra("isSettings", true);
                getActivity().startActivity(intent);
                return true;
            });
        }
        return rootView;
    }

    private void initProtocolPrefs() {
        ListPreference protocolPref = (ListPreference) findPreference(KEY_PROTOCOL);

        if (protocolPref != null) {
            protocolPref.setSummary(protocolPref.getEntry());
            protocolPref.setOnPreferenceChangeListener(createChangeListener());

            addPreferencesResource(protocolPref.getValue());
        } else {
            addPreferencesResource(getString(R.string.protocol_odk_default));
        }
    }

    private void addPreferencesResource(CharSequence value) {
        if (value == null || value.equals(getString(R.string.protocol_odk_default))) {
            setDefaultAggregatePaths();
            addAggregatePreferences();
        } else if (value.equals(getString(R.string.protocol_google_sheets))) {
            addGooglePreferences();
        } else {
            // other
            addOtherPreferences();
        }
    }

    private Preference.OnPreferenceChangeListener createChangeListener() {
        return (preference, newValue) -> {
            if (preference.getKey().equals(KEY_PROTOCOL)) {
                String stringValue = (String) newValue;
                ListPreference lpref = (ListPreference) preference;
                String oldValue = lpref.getValue();
                lpref.setValue(stringValue);

                if (!newValue.equals(oldValue)) {
                    removeTypeSettings();
                    initProtocolPrefs();
                }
            }
            return true;
        };
    }

    private void removeTypeSettings() {
        getPreferenceScreen().removeAll();
//        addPreferencesFromResource(R.xml.server_preferences);
    }
}
