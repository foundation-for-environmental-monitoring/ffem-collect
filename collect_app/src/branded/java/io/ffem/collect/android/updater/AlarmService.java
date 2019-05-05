package io.ffem.collect.android.updater;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import io.ffem.collect.android.common.ConstantKey;
import io.ffem.collect.android.notification.NotificationScheduler;
import io.ffem.collect.android.util.ApiUtil;
import io.ffem.collect.android.util.PreferencesUtil;

public class AlarmService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Collect.getInstance().isNetworkAvailable()) {

            UpdateCheck.setNextUpdateCheck(this, AlarmManager.INTERVAL_DAY * 3);

            if (PreferencesUtil.getInt(this, ConstantKey.LATEST_APP_VERSION,
                    0) > ApiUtil.getAppVersionCode(this)) {

                NotificationScheduler.showNotification(this,
                        getString(R.string.updateTitle),
                        getString(R.string.updateAvailable));
            }

        } else {
            UpdateCheck.setNextUpdateCheck(this, AlarmManager.INTERVAL_HALF_HOUR);
        }
        return START_NOT_STICKY;
    }

}