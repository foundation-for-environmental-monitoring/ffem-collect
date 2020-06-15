package io.ffem.collect.android.preferences;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;

import io.ffem.collect.android.utilities.ListViewUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class TestingPreferenceFragment extends PreferenceFragment {

    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_testing);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_row, container, false);
        setBackgroundColor(view);

        Preference testModeOnPreference = findPreference(getString(R.string.launchExperimentKey));
        if (testModeOnPreference != null) {
            testModeOnPreference.setOnPreferenceClickListener(preference -> {
                setBackgroundColor(view);
                return true;
            });
        }
        return view;
    }

    private void setBackgroundColor(View view) {
//        if (AppPreferences.isTestMode()) {
//            view.setBackgroundColor(Color.rgb(255, 165, 0));
//        } else {
            view.setBackgroundColor(Color.rgb(255, 240, 220));
//        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(android.R.id.list);
        (new Handler()).postDelayed(() -> ListViewUtil.setListViewHeightBasedOnChildren(list, 40), 200);
    }
}
