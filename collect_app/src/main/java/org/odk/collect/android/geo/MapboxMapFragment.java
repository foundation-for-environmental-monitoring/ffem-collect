package org.odk.collect.android.geo;

import androidx.annotation.VisibleForTesting;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// Brand change ----
public class MapboxMapFragment extends org.odk.collect.android.geo.mapboxsdk.MapFragment {

    // During Robolectric tests, Google Play Services is unavailable; sadly, the
    // "map" field will be null and many operations will need to be stubbed out.
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "This flag is exposed for Robolectric tests to set")
    @VisibleForTesting
    public static boolean testMode;
}
