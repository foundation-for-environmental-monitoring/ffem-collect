package org.odk.collect.android.geo;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;

import com.google.common.collect.ImmutableSet;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PrefUtils;
import org.odk.collect.android.preferences.source.Settings;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_MAPBOX_MAP_STYLE;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_REFERENCE_LAYER;

class MapboxMapConfigurator implements MapConfigurator {
    private final String prefKey;
    private final int sourceLabelId;
    private final MapboxUrlOption[] options;

    /** Constructs a configurator with a few Mapbox style URL options to choose from. */
    MapboxMapConfigurator(String prefKey, int sourceLabelId, MapboxUrlOption... options) {
        this.prefKey = prefKey;
        this.sourceLabelId = sourceLabelId;
        this.options = options;
    }

    // Brand change ----
    @Override public boolean isAvailable(Context context) {
        return false;
    }

    @Override public void showUnavailableMessage(Context context) {
        ToastUtils.showLongToast(context.getString(
                R.string.basemap_source_unavailable, context.getString(sourceLabelId)));
    }

    // Brand change ----
    @Override public MapFragment createMapFragment(Context context) {
        return null;
    }

    @Override public List<Preference> createPrefs(Context context) {
        int[] labelIds = new int[options.length];
        String[] values = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            labelIds[i] = options[i].labelId;
            values[i] = options[i].url;
        }
        String prefTitle = context.getString(
                R.string.map_style_label, context.getString(sourceLabelId));
        return Collections.singletonList(PrefUtils.createListPref(
                context, prefKey, prefTitle, labelIds, values
        ));
    }

    @Override public Set<String> getPrefKeys() {
        return prefKey.isEmpty() ? ImmutableSet.of(KEY_REFERENCE_LAYER) :
                ImmutableSet.of(prefKey, KEY_REFERENCE_LAYER);
    }

    // Brand change ----
    @Override public Bundle buildConfig(Settings prefs) {
        return new Bundle();
    }

    @Override public boolean supportsLayer(File file) {
        // MapboxMapFragment supports any file that MbtilesFile can read.
        return MbtilesFile.readLayerType(file) != null;
    }

    @Override public String getDisplayName(File file) {
        String name = MbtilesFile.readName(file);
        return name != null ? name : file.getName();
    }

    static class MapboxUrlOption {
        final String url;
        final int labelId;

        MapboxUrlOption(String url, int labelId) {
            this.url = url;
            this.labelId = labelId;
        }
    }
}
