package io.ffem.collect.android.updater;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static io.ffem.collect.android.common.ConstantKey.EXTRA_NOTIFICATION_ID;

public class NotificationCancelReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0));
        }
    }
}