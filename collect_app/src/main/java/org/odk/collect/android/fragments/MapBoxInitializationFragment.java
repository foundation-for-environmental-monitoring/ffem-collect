package org.odk.collect.android.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.odk.collect.android.injection.DaggerUtils;

// Brand change ----
public class MapBoxInitializationFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(getActivity()).inject(this);
    }
}
