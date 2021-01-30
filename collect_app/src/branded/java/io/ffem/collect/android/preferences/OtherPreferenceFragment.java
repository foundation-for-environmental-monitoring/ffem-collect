package io.ffem.collect.android.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.odk.collect.android.R;

import io.ffem.collect.android.activities.AboutActivity;

import static io.ffem.collect.android.helper.AppHelper.getVersionedAppName;

public class OtherPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_other, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Preference aboutPreference = (Preference) findPreference("about");
        if (aboutPreference != null) {
            aboutPreference.setSummary(getVersionedAppName(view.getContext()));
            aboutPreference.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(getActivity(), AboutActivity.class);
                getActivity().startActivity(intent);
                return true;
            });
        }
    }
}
