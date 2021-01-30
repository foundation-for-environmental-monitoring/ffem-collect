package io.ffem.collect.android.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.DeleteSavedFormActivity;
import org.odk.collect.android.preferences.ResetDialogPreference;
import org.odk.collect.android.preferences.ResetDialogPreferenceFragmentCompat;

public class AdminPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.admin_preferences_custom, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Preference deletePreference = (Preference) findPreference("delete_forms");
        if (deletePreference != null) {
            deletePreference.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(getActivity(), DeleteSavedFormActivity.class);
                getActivity().startActivity(intent);
                return true;
            });
        }
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
