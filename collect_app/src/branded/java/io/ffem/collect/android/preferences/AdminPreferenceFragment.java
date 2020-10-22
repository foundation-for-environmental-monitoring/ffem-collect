package io.ffem.collect.android.preferences;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.ResetDialogPreference;
import org.odk.collect.android.preferences.ResetDialogPreferenceFragmentCompat;

public class AdminPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.admin_preferences_custom, rootKey);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        ResetDialogPreference resetDialogPreference = null;
        if (preference instanceof ResetDialogPreference) {
            resetDialogPreference = (ResetDialogPreference) preference;
        }
        if (resetDialogPreference != null) {
            ResetDialogPreferenceFragmentCompat dialogFragment = ResetDialogPreferenceFragmentCompat.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
