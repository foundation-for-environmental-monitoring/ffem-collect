package io.ffem.collect.android.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import io.ffem.collect.android.common.ConstantKey;
import io.ffem.collect.android.util.ApiUtil;

public class CloudMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Context context = getApplicationContext();

        int latestAppVersion;
        int versionCode = ApiUtil.getAppVersionCode(context);

        try {
            latestAppVersion = Integer.parseInt(remoteMessage.getData().get(ConstantKey.LATEST_APP_VERSION));

            if (latestAppVersion > versionCode) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
                settings.edit().putInt(ConstantKey.LATEST_APP_VERSION, latestAppVersion).apply();

                NotificationScheduler.showNotification(this,
                        getString(R.string.updateTitle),
                        getString(R.string.updateAvailable));
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
