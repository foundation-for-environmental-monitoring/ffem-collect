package org.odk.collect.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;

import java.util.Objects;

import io.ffem.collect.android.util.ApiUtil;

public class CloudMessagingService extends FirebaseMessagingService {

    private static final int UPDATE_NOTIFICATION = 100;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Context context = getApplicationContext();

        int latestAppVersion;
        int versionCode = ApiUtil.getAppVersionCode(context);

        try {
            latestAppVersion = Integer.parseInt(remoteMessage.getData().get("latestAppVersion"));

            if (latestAppVersion > versionCode) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
                settings.edit().putInt(PreferenceKeys.LATEST_APP_VERSION, latestAppVersion).apply();

                sendNotification(context.getString(R.string.updateTitle),
                        context.getString(R.string.updateAvailable));
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param message FCM message body received.
     */
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }

        Objects.requireNonNull(notificationManager).notify(UPDATE_NOTIFICATION,
                notificationBuilder.build());
    }

}
