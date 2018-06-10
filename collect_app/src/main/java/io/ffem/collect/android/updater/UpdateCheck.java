package io.ffem.collect.android.updater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.ffem.collect.android.common.ConstantKey;
import io.ffem.collect.android.util.PreferencesUtil;

public class UpdateCheck {

    /**
     * Setup alarm manager to check for app updates.
     *
     * @param context  the Context
     * @param interval wait time before next check
     */
    public static void setNextUpdateCheck(Context context, long interval) {

        if (interval > -1) {
            PreferencesUtil.setLong(context, ConstantKey.NEXT_UPDATE_CHECK,
                    System.currentTimeMillis() + interval);
        }

        PendingIntent alarmIntent = PendingIntent.getService(context, 0,
                new Intent(context, AlarmService.class), 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long nextUpdateTime = Math.max(PreferencesUtil.getLong(context, ConstantKey.NEXT_UPDATE_CHECK),
                System.currentTimeMillis() + 10000);

        if (manager != null) {
            manager.setInexactRepeating(AlarmManager.RTC, nextUpdateTime,
                    AlarmManager.INTERVAL_DAY * 3, alarmIntent);
        }
    }
}