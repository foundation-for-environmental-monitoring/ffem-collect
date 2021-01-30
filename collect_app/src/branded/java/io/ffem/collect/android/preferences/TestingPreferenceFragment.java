package io.ffem.collect.android.preferences;

import android.app.Fragment;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.odk.collect.android.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestingPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_testing, rootKey);
    }
}
