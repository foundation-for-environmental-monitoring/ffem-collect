/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.MainMenuViewModel;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.storage.migration.StorageMigrationService;
import org.odk.collect.material.MaterialBanner;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPasswordDialogFragment;
import org.odk.collect.android.preferences.AdminPasswordDialogFragment.Action;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.AutoSendPreferenceMigrator;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceSaver;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.preferences.qr.QRCodeTabsActivity;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.storage.migration.StorageMigrationDialog;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.PlayServicesChecker;
import org.odk.collect.android.utilities.SharedPreferencesUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.version.VersionInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import io.ffem.collect.android.activities.SettingsActivity;
import io.ffem.collect.android.common.AppConfig;
import io.ffem.collect.android.preferences.AppPreferences;
import io.ffem.collect.android.util.ApkHelper;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static org.odk.collect.android.utilities.DialogUtils.getDialog;
import static org.odk.collect.android.utilities.DialogUtils.showIfNotShowing;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends CollectAbstractActivity implements AdminPasswordDialogFragment.AdminPasswordDialogCallback {
    private static final boolean EXIT = true;
    // buttons
//     private Button manageFilesButton;
    private Button sendDataButton;
    private Button viewSentFormsButton;
    private Button reviewDataButton;
    private Button getFormsButton;
    private AlertDialog alertDialog;
    private MenuItem qrcodeScannerMenuItem;
    private int completedCount;
    private int savedCount;
    private int viewSentCount;
    private Cursor finalizedCursor;
    private Cursor savedCursor;
    private Cursor viewSentCursor;
    private IncomingHandler handler = new IncomingHandler(this);
    private MyContentObserver contentObserver = new MyContentObserver();
    private TextView reviewDataBadge;
    private TextView sendDataBadge;
    private TextView viewSendFormsBadge;

    @Inject
    public Analytics analytics;

    @BindView(R.id.storageMigrationBanner)
    MaterialBanner storageMigrationBanner;

    @BindView(R.id.version_sha)
    TextView versionSHAView;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    StorageStateProvider storageStateProvider;

    @Inject
    StoragePathProvider storagePathProvider;

    @Inject
    AdminPasswordProvider adminPasswordProvider;

    @Inject
    VersionInformation versionInformation;

    @Inject
    GeneralSharedPreferences generalSharedPreferences;

    private MainMenuViewModel viewModel;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getBlankForm();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collect.getInstance().getComponent().inject(this);
        setContentView(R.layout.main_menu);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        viewModel = ViewModelProviders.of(this, new MainMenuViewModel.Factory(versionInformation)).get(MainMenuViewModel.class);

        initToolbar();
        DaggerUtils.getComponent(this).inject(this);

        storageMigrationRepository.getResult().observe(this, this::onStorageMigrationFinish);

        // enter data button. expects a result.
        Button enterDataButton = findViewById(R.id.enter_data);
        enterDataButton.setText(getString(R.string.enter_data_button));
        enterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            FormChooserListActivity.class);
                    i.putExtra("allowDelete", true);
                    startActivity(i);
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,
                new IntentFilter("DOWNLOAD_FORMS_ACTION")
        );

        reviewDataBadge = findViewById(R.id.text_review_data);
        // review data button. expects a result.
        reviewDataButton = findViewById(R.id.review_data);
        reviewDataButton.setText(getString(R.string.review_data_button));
        reviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                            ApplicationConstants.FormModes.EDIT_SAVED);
                    i.putExtra("allowDelete", true);
                    startActivity(i);
                }
            }
        });

        sendDataBadge = findViewById(R.id.text_send_data);
        // send data button. expects a result.
        sendDataButton = findViewById(R.id.send_data);
        sendDataButton.setText(getString(R.string.send_data_button));
        sendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(),
                            InstanceUploaderListActivity.class);
                    startActivity(i);
                }
            }
        });

        viewSendFormsBadge = findViewById(R.id.text_view_sent);
        //View sent forms
        viewSentFormsButton = findViewById(R.id.view_sent_forms);
        viewSentFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MultiClickGuard.allowClick(getClass().getName())) {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                            ApplicationConstants.FormModes.VIEW_SENT);
                    i.putExtra("allowDelete", true);
                    startActivity(i);
                }
            }
        });

        // manage forms button. no result expected.
//        getFormsButton = findViewById(R.id.get_forms);
//        getFormsButton.setText(getString(R.string.get_forms));
//        getFormsButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (MultiClickGuard.allowClick(getClass().getName())) {
//                    SharedPreferences sharedPreferences = PreferenceManager
//                            .getDefaultSharedPreferences(MainMenuActivity.this);
//                    String protocol = sharedPreferences.getString(
//                            GeneralKeys.KEY_PROTOCOL, getString(R.string.protocol_odk_default));
//                    Intent i = null;
//                    if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
//                        if (new PlayServicesChecker().isGooglePlayServicesAvailable(MainMenuActivity.this)) {
//                            i = new Intent(getApplicationContext(),
//                                    GoogleDriveActivity.class);
//                        } else {
//                            new PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(MainMenuActivity.this);
//                            return;
//                        }
//                    } else {
//                        i = new Intent(getApplicationContext(),
//                                FormDownloadListActivity.class);
//                    }
//                    startActivity(i);
//                }
//            }
//        });
//
//        // manage forms button. no result expected.
//        manageFilesButton = findViewById(R.id.manage_forms);
//        manageFilesButton.setText(getString(R.string.manage_files));
//        manageFilesButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (MultiClickGuard.allowClick(getClass().getName())) {
//                    Intent i = new Intent(getApplicationContext(),
//                            FileManagerTabs.class);
//                    startActivity(i);
//                }
//            }
//        });

        String versionSHA = viewModel.getVersionCommitDescription();
        if (versionSHA != null) {
            versionSHAView.setText(versionSHA);
        } else {
            versionSHAView.setVisibility(View.GONE);
        }

        // must be at the beginning of any activity that can be called from an
        // external intent
        Timber.i("Starting up, creating directories");
        try {
            new StorageInitializer().createOdkDirsOnStorage();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        File f = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings");
        File j = new File(storagePathProvider.getStorageRootDirPath() + "/collect.settings.json");
        // Give JSON file preference
        if (j.exists()) {
            boolean success = SharedPreferencesUtils.loadSharedPreferencesFromJSONFile(j);
            if (success) {
                ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
                j.delete();
                recreate();

                // Delete settings file to prevent overwrite of settings from JSON file on next startup
                if (f.exists()) {
                    f.delete();
                }
            } else {
                ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
            }
        } else if (f.exists()) {
            boolean success = loadSharedPreferencesFromFile(f);
            if (success) {
                ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
                f.delete();
                recreate();
            } else {
                ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
            }
        }

        displayExpiryInfo();
    }

    @Override
    protected void onResume() {
        AppPreferences.checkDiagnosticModeExpiry(this);
        super.onResume();

        countSavedForms();
        updateButtons();
        if (!storageMigrationRepository.isMigrationBeingPerformed()) {
            getContentResolver().registerContentObserver(InstanceColumns.CONTENT_URI, true, contentObserver);
        }

        setButtonsVisibility();
        invalidateOptionsMenu();
        setUpStorageMigrationBanner();
    }

    private void setButtonsVisibility() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        boolean edit = sharedPreferences.getBoolean(AdminKeys.KEY_EDIT_SAVED, true);
        if (!edit) {
            if (reviewDataButton != null) {
                reviewDataButton.setVisibility(View.GONE);
            }
        } else {
            if (reviewDataButton != null) {
                reviewDataButton.setVisibility(View.VISIBLE);
            }
        }

        boolean send = sharedPreferences.getBoolean(AdminKeys.KEY_SEND_FINALIZED, true);
        if (!send) {
            if (sendDataButton != null) {
                sendDataButton.setVisibility(View.GONE);
            }
        } else {
//            if (sendDataButton != null) {
//                sendDataButton.setVisibility(View.VISIBLE);
//            }
        }

        boolean viewSent = sharedPreferences.getBoolean(AdminKeys.KEY_VIEW_SENT, true);
        if (!viewSent) {
            if (viewSentFormsButton != null) {
                viewSentFormsButton.setVisibility(View.GONE);
            }
        } else {
            if (viewSentFormsButton != null) {
                viewSentFormsButton.setVisibility(View.VISIBLE);
            }
        }

        boolean getBlank = sharedPreferences.getBoolean(AdminKeys.KEY_GET_BLANK, true);
        if (!getBlank) {
            if (getFormsButton != null) {
                getFormsButton.setVisibility(View.GONE);
            }
        } else {
            if (getFormsButton != null) {
                getFormsButton.setVisibility(View.VISIBLE);
            }
        }

//        boolean deleteSaved = sharedPreferences.getBoolean(AdminKeys.KEY_DELETE_SAVED, true);
//        if (!deleteSaved) {
//            if (manageFilesButton != null) {
//                manageFilesButton.setVisibility(View.GONE);
//            }
//        } else {
//            if (manageFilesButton != null) {
//                manageFilesButton.setVisibility(View.VISIBLE);
//            }
//        }

        switchLayoutForDiagnosticOrUserMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    public void onDestroy() {
        storageMigrationRepository.clearResult();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        qrcodeScannerMenuItem = menu.findItem(R.id.menu_configure_qr_code);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        qrcodeScannerMenuItem.setVisible(this.getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).getBoolean(AdminKeys.KEY_QR_CODE_SCANNER, true));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 100);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
    }

    private void countSavedForms() {
        InstancesDao instancesDao = new InstancesDao();

        // count for finalized instances
        try {
            finalizedCursor = instancesDao.getFinalizedInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (finalizedCursor != null) {
            startManagingCursor(finalizedCursor);
        }
        completedCount = finalizedCursor != null ? finalizedCursor.getCount() : 0;

        // count for saved instances
        try {
            savedCursor = instancesDao.getUnsentInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (savedCursor != null) {
            startManagingCursor(savedCursor);
        }
        savedCount = savedCursor != null ? savedCursor.getCount() : 0;

        //count for view sent form
        try {
            viewSentCursor = instancesDao.getSentInstancesCursor();
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }
        if (viewSentCursor != null) {
            startManagingCursor(viewSentCursor);
        }
        viewSentCount = viewSentCursor != null ? viewSentCursor.getCount() : 0;
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), errorListener);
        alertDialog.show();
    }

    private void updateButtons() {
        if (finalizedCursor != null && !finalizedCursor.isClosed()) {
            finalizedCursor.requery();
            completedCount = finalizedCursor.getCount();
            if (completedCount > 0) {
                sendDataBadge.setText(String.valueOf(completedCount));
                sendDataBadge.setVisibility(View.VISIBLE);
                sendDataButton.setVisibility(View.VISIBLE);
                sendDataButton.setEnabled(true);
            } else {
                sendDataBadge.setVisibility(View.GONE);
                sendDataButton.setVisibility(View.GONE);
                sendDataButton.setEnabled(false);
            }
            sendDataButton.setText(getString(R.string.outbox));

        } else {
            sendDataButton.setText(getString(R.string.outbox));
            Timber.w("Cannot update \"Send Finalized\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }

        if (savedCursor != null && !savedCursor.isClosed()) {
            savedCursor.requery();
            savedCount = savedCursor.getCount();
            if (savedCount > 0) {
                reviewDataBadge.setText(String.valueOf(savedCount));
                reviewDataBadge.setVisibility(View.VISIBLE);
                reviewDataButton.setAlpha(1);
                reviewDataButton.setEnabled(true);
            } else {
                reviewDataBadge.setVisibility(View.GONE);
                reviewDataButton.setAlpha(0.3f);
                reviewDataButton.setEnabled(false);
            }
            reviewDataButton.setText(getString(R.string.review_data));
        } else {
            reviewDataButton.setText(getString(R.string.review_data));
            Timber.w("Cannot update \"Edit Form\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }

        if (viewSentCursor != null && !viewSentCursor.isClosed()) {
            viewSentCursor.requery();
            viewSentCount = viewSentCursor.getCount();
            if (viewSentCount > 0) {
                viewSendFormsBadge.setText(String.valueOf(viewSentCount));
                viewSendFormsBadge.setVisibility(View.VISIBLE);
                viewSentFormsButton.setAlpha(1);
                viewSentFormsButton.setEnabled(true);
            } else {
                viewSendFormsBadge.setVisibility(View.GONE);
                viewSentFormsButton.setAlpha(0.3f);
                viewSentFormsButton.setEnabled(false);
            }
            viewSentFormsButton.setText(getString(R.string.view_sent_forms));
        } else {
            viewSentFormsButton.setText(getString(R.string.view_sent_forms));
            Timber.w("Cannot update \"View Sent\" button label since the database is closed. "
                    + "Perhaps the app is running in the background?");
        }
    }

    private boolean loadSharedPreferencesFromFile(File src) {
        // this should probably be in a thread if it ever gets big
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));

            // first object is preferences
            Map<String, Object> entries = (Map<String, Object>) input.readObject();

            AutoSendPreferenceMigrator.migrate(entries);
            PreferenceSaver.saveGeneralPrefs(generalSharedPreferences, entries);

            // second object is admin options
            Map<String, Object> adminEntries = (Map<String, Object>) input.readObject();
            PreferenceSaver.saveAdminPrefs(AdminSharedPreferences.getInstance(), adminEntries);

            Collect.getInstance().initializeJavaRosa();
            res = true;
        } catch (IOException | ClassNotFoundException e) {
            Timber.e(e, "Exception while loading preferences from file due to : %s ", e.getMessage());
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "Exception thrown while closing an input stream due to: %s ", ex.getMessage());
            }
        }
        return res;
    }

    @Override
    public void onCorrectAdminPassword(Action action) {
        switch (action) {
            case ADMIN_SETTINGS:
                startActivity(new Intent(this, AdminPreferencesActivity.class));
                break;
            case STORAGE_MIGRATION:
                StorageMigrationDialog dialog = showStorageMigrationDialog();
                if (dialog != null) {
                    dialog.startStorageMigration();
                }

                break;
            case SCAN_QR_CODE:
                startActivity(new Intent(this, QRCodeTabsActivity.class));
                break;
        }
    }

    @Override
    public void onIncorrectAdminPassword() {
        ToastUtils.showShortToast(R.string.admin_password_incorrect);
    }

    /*
     * Used to prevent memory leaks
     */
    static class IncomingHandler extends Handler {
        private final WeakReference<MainMenuActivity> target;

        IncomingHandler(MainMenuActivity target) {
            this.target = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainMenuActivity target = this.target.get();
            if (target != null) {
                target.updateButtons();
            }
        }
    }

    /**
     * notifies us that something changed
     */
    private class MyContentObserver extends ContentObserver {

        MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handler.sendEmptyMessage(0);
        }
    }

    private void onStorageMigrationFinish(StorageMigrationResult result) {
        if (result == StorageMigrationResult.SUCCESS) {
            DialogUtils.dismissDialog(StorageMigrationDialog.class, getSupportFragmentManager());
//            displayBannerWithSuccessStorageMigrationResult();
        } else {
            StorageMigrationDialog dialog = showStorageMigrationDialog();

            if (dialog != null) {
                dialog.handleMigrationError(result);
            }
        }
    }

    @Nullable
    private StorageMigrationDialog showStorageMigrationDialog() {
        Bundle args = new Bundle();
        args.putInt(StorageMigrationDialog.ARG_UNSENT_INSTANCES, savedCount);

        showIfNotShowing(StorageMigrationDialog.class, args, getSupportFragmentManager());
        return getDialog(StorageMigrationDialog.class, getSupportFragmentManager());
    }

    private void setUpStorageMigrationBanner() {
        if (!storageStateProvider.isScopedStorageUsed()) {
            displayStorageMigrationBanner();
        }
    }

    private void displayStorageMigrationBanner() {
        storageMigrationBanner.setVisibility(View.GONE);
        storageMigrationBanner.setText(getText(R.string.scoped_storage_banner_text));
        storageMigrationBanner.setActionText(getString(R.string.scoped_storage_learn_more));
        storageMigrationBanner.setAction(() -> {
            showStorageMigrationDialog();
            getContentResolver().unregisterContentObserver(contentObserver);
        });
    }

    private void displayBannerWithSuccessStorageMigrationResult() {
        storageMigrationBanner.setVisibility(View.VISIBLE);
        storageMigrationBanner.setText(getString(R.string.storage_migration_completed));
        storageMigrationBanner.setActionText(getString(R.string.scoped_storage_dismiss));
        storageMigrationBanner.setAction(() -> {
            storageMigrationBanner.setVisibility(View.GONE);
            storageMigrationRepository.clearResult();
        });
    }

    private void getBlankForm() {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(MainMenuActivity.this);
            String protocol = sharedPreferences.getString(
                    GeneralKeys.KEY_PROTOCOL, getString(R.string.protocol_odk_default));
            Intent i;
            if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
                if (new PlayServicesChecker().isGooglePlayServicesAvailable(MainMenuActivity.this)) {
                    i = new Intent(getApplicationContext(),
                            GoogleDriveActivity.class);
                } else {
                    new PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(MainMenuActivity.this);
                    return;
                }
            } else {
                i = new Intent(getApplicationContext(), FormDownloadListActivity.class);
//                i.putExtra("isDownloadForms", true);
            }
            startActivity(i);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Stop if the app version has expired
        ApkHelper.isAppVersionExpired(this);

        startStorageMigrationService();
    }

    private void displayExpiryInfo() {
        try {

            TextView textVersionExpiry = findViewById(R.id.textVersionExpiry);

            if (AppConfig.APP_EXPIRY && ApkHelper.isNonStoreVersion(this)) {
                final GregorianCalendar appExpiryDate = new GregorianCalendar(AppConfig.APP_EXPIRY_YEAR,
                        AppConfig.APP_EXPIRY_MONTH - 1, AppConfig.APP_EXPIRY_DAY);

                DateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                textVersionExpiry.setText(String.format("Version expiry: %s", df.format(appExpiryDate.getTime())));

                textVersionExpiry.setVisibility(View.VISIBLE);
            } else {
                textVersionExpiry.setText(Collect.getInstance().getVersionedAppName());
                textVersionExpiry.setVisibility(View.VISIBLE);
            }

            // If app has expired then close this activity
            ApkHelper.isAppVersionExpired(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the diagnostic mode layout.
     */
    private void switchLayoutForDiagnosticOrUserMode() {
        if (AppPreferences.isDiagnosticMode(this)) {
            findViewById(R.id.layoutDiagnostics).setVisibility(View.VISIBLE);
        } else {
            if (findViewById(R.id.layoutDiagnostics).getVisibility() == View.VISIBLE) {
                findViewById(R.id.layoutDiagnostics).setVisibility(View.GONE);
            }
        }
    }

    public void onDisableDiagnosticsClick(View view) {
        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode(this);

        switchLayoutForDiagnosticOrUserMode();

        changeActionBarStyleBasedOnCurrentMode();
    }

    private void startStorageMigrationService() {
        if (!storageStateProvider.isScopedStorageUsed()) {
            Intent intent = new Intent(this, StorageMigrationService.class);
            this.startService(intent);
        }
    }
}
