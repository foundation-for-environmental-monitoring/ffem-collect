package io.ffem.collect.android.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionsDelegate {

    private static final int REQUEST_CODE = 100;
    private final String[] cameraPermission = {Manifest.permission.CAMERA};
    private final String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
    private final Activity activity;

    public PermissionsDelegate(Activity activity) {
        this.activity = activity;
    }

    public static boolean resultGranted(int requestCode, int[] grantResults) {

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

    public static boolean hasCameraPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean hasLocationPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    public boolean hasPermissions(String[] permissions) {

        for (String permission : permissions) {
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

    public void requestLocationPermission() {
        requestPermissions(locationPermission);
    }

    public void requestCameraPermission() {
        requestPermissions(cameraPermission);
    }
}
