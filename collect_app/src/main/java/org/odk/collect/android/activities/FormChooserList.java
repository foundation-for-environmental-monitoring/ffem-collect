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

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import android.view.View;
import android.widget.AdapterView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.tasks.DiskSyncTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.VersionHidingCursorAdapter;

import io.ffem.collect.android.activities.DeleteFormsActivity;
import timber.log.Timber;

import static org.odk.collect.android.utilities.PermissionUtils.finishAllActivities;

/**
 * Responsible for displaying all the valid forms in the forms directory. Stores the path to
 * selected form for use by {@link MainMenuActivity}.
 *
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FormChooserList extends FormListActivity implements
        DiskSyncListener, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String FORM_CHOOSER_LIST_SORTING_ORDER = "formChooserListSortingOrder";

    private static final boolean EXIT = true;
    private DiskSyncTask diskSyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooser_blank_list_layout);

        setTitle(getString(R.string.enter_data));

        findViewById(R.id.buttonGetBlankForm).setOnClickListener(view -> onClickGetBlankForm(view));

        new PermissionUtils(this).requestStoragePermissions(new PermissionListener() {
            @Override
            public void granted() {
                // must be at the beginning of any activity that can be called from an external intent
                try {
                    Collect.createODKDirs();
                    init();
                } catch (RuntimeException e) {
                    createErrorDialog(e.getMessage(), EXIT);
                }
            }

            @Override
            public void denied() {
                // The activity has to finish because ODK Collect cannot function without these permissions.
                finishAllActivities(FormChooserList.this);
            }
        });
    }

    private void init() {
//        setupAdapter();

        // DiskSyncTask checks the disk for any forms not already in the content provider
        // that is, put here by dragging and dropping onto the SDCard
        diskSyncTask = (DiskSyncTask) getLastCustomNonConfigurationInstance();
        if (diskSyncTask == null) {
            Timber.i("Starting new disk sync task");
            diskSyncTask = new DiskSyncTask();
            diskSyncTask.setDiskSyncListener(this);
            diskSyncTask.execute((Void[]) null);
        }
        sortingOptions = new int[] {
                R.string.sort_by_name_asc, R.string.sort_by_name_desc,
                R.string.sort_by_date_asc, R.string.sort_by_date_desc,
        };

        setupAdapter();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        // pass the thread on restart
        return diskSyncTask;
    }

    /**
     * Stores the path of selected form and finishes.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (Collect.allowClick(getClass().getName())) {
            // get uri to form
            long idFormsTable = listView.getAdapter().getItemId(position);
            Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, idFormsTable);

            String action = getIntent().getAction();
            if (Intent.ACTION_PICK.equals(action)) {
                // caller is waiting on a picked form
                setResult(RESULT_OK, new Intent().setData(formUri));
            } else {
                // caller wants to view/edit a form, so launch formentryactivity
                Intent intent = new Intent(Intent.ACTION_EDIT, formUri);
                intent.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                startActivity(intent);
            }

//            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (diskSyncTask != null) {
            diskSyncTask.setDiskSyncListener(this);
            if (diskSyncTask.getStatus() == AsyncTask.Status.FINISHED) {
                syncComplete(diskSyncTask.getStatusMessage());
            }
        }
    }

    @Override
    protected void onPause() {
        if (diskSyncTask != null) {
            diskSyncTask.setDiskSyncListener(null);
        }
        super.onPause();
    }

    /**
     * Called by DiskSyncTask when the task is finished
     */
    @Override
    public void syncComplete(@NonNull String result) {
        Timber.i("Disk scan complete");
        hideProgressBarAndAllow();
        showSnackbar(result);
    }

    private void setupAdapter() {
        String[] data = new String[]{
                FormsColumns.DISPLAY_NAME, FormsColumns.JR_VERSION, FormsColumns.DISPLAY_SUBTEXT
        };
        int[] view = new int[]{
                R.id.form_title, R.id.form_subtitle, R.id.form_subtitle2
        };

        listAdapter = new VersionHidingCursorAdapter(FormsColumns.JR_VERSION, this, R.layout.form_chooser_list_item, null, data, view);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected String getSortingOrderKey() {
        return FORM_CHOOSER_LIST_SORTING_ORDER;
    }

    @Override
    protected void updateAdapter() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    /**
     * Creates a dialog with the given message. Will exit the activity when the user preses "ok" if
     * shouldExit is set to true.
     */
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//        alertDialog.setIcon(R.drawable.ic_dialog_info);
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
        alertDialog.setButton(getString(R.string.ok), errorListener);
        alertDialog.show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressBar();

        boolean newestByFormId = GeneralSharedPreferences.getInstance().getBoolean(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS, false);
        return new FormsDao().getFormsCursorLoader(getFilterText(), getSortingOrder(), newestByFormId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        hideProgressBarIfAllowed();
        listAdapter.swapCursor(cursor);
        if (isSearching()) {
            findViewById(R.id.buttonGetBlankForm).setVisibility(View.GONE);
        } else {
            cursor.requery();
            if (listAdapter.getCount() == 0) {
                findViewById(R.id.buttonGetBlankForm).setVisibility(View.GONE);
            } else {
                findViewById(R.id.buttonGetBlankForm).setVisibility(View.VISIBLE);
            }
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        listAdapter.swapCursor(null);
    }

    public void onClickGetBlankForm(View view) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                ToastUtils.showShortToast(R.string.no_connection);
            } else {
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                Intent localIntent = new Intent("DOWNLOAD_FORMS_ACTION");
                localBroadcastManager.sendBroadcast(localIntent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                Intent i = new Intent(this, DeleteFormsActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (findViewById(R.id.buttonGetBlankForm).getVisibility() == View.GONE) {
            MenuItem deleteItem = (menu.findItem(R.id.menu_delete));
            deleteItem.setEnabled(false);
            deleteItem.getIcon().setAlpha(50);
            MenuItem sortItem = (menu.findItem(R.id.menu_sort));
            sortItem.setEnabled(false);
            sortItem.getIcon().setAlpha(60);
            MenuItem filterItem = (menu.findItem(R.id.menu_filter));
            filterItem.setEnabled(false);
            filterItem.getIcon().setAlpha(60);
        } else {
            MenuItem deleteItem = (menu.findItem(R.id.menu_delete));
            deleteItem.setEnabled(true);
            deleteItem.getIcon().setAlpha(255);
            MenuItem sortItem = (menu.findItem(R.id.menu_sort));
            sortItem.setEnabled(true);
            sortItem.getIcon().setAlpha(255);
            MenuItem filterItem = (menu.findItem(R.id.menu_filter));
            filterItem.setEnabled(true);
            filterItem.getIcon().setAlpha(255);
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
