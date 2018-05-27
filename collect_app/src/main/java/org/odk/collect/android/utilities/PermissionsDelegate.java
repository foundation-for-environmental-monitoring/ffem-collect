package org.odk.collect.android.utilities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionsDelegate {

    private static final int REQUEST_CODE = 100;
    private final Activity activity;

    public PermissionsDelegate(Activity activity) {
        this.activity = activity;
    }

    public boolean hasPermissions(String[] permissions) {

        for (String permission :
                permissions) {
            int permissionCheckResult = ContextCompat.checkSelfPermission(
                    activity, permission
            );

            if (permissionCheckResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

        }
        return true;
    }

    public void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(
                activity,
                permissions,
                REQUEST_CODE
        );
    }

    public void requestPermissions(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(
                activity,
                permissions,
                requestCode
        );
    }

    public boolean resultGranted(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean resultGranted(int requestCode, int[] grantResults) {

        if (requestCode != REQUEST_CODE) {
            return false;
        }

        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
