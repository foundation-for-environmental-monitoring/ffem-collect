package io.ffem.collect.android.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.odk.collect.android.R;

import io.ffem.collect.android.updater.NotificationCancelReceiver;
import io.ffem.collect.android.updater.UpdateAppReceiver;

import static io.ffem.collect.android.common.ConstantKey.EXTRA_NOTIFICATION_ID;

public class NotificationScheduler {
    private static final int DAILY_REMINDER_REQUEST_CODE = 100;

    public static void showNotification(Context context, String title, String content) {

        String channelId = context.getString(R.string.default_notification_channel_id);

        Intent updateIntent = new Intent(context, UpdateAppReceiver.class);
        updateIntent.setAction(Integer.toString(DAILY_REMINDER_REQUEST_CODE));
        updateIntent.putExtra(EXTRA_NOTIFICATION_ID, DAILY_REMINDER_REQUEST_CODE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, updateIntent, 0);

        Intent snoozeIntent = new Intent(context, NotificationCancelReceiver.class);
        snoozeIntent.setAction(Integer.toString(DAILY_REMINDER_REQUEST_CODE));
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, DAILY_REMINDER_REQUEST_CODE);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = builder.setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setSmallIcon(R.mipmap.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .addAction(0, context.getString(R.string.remindLater), snoozePendingIntent)
                .addAction(0, context.getString(R.string.update), pendingIntent)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "App updates",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(DAILY_REMINDER_REQUEST_CODE, notification);
        }
    }
}