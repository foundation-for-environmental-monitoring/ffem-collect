/*
 * Copyright (C) 2017 University of Washington
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

package io.ffem.collect.android.activities;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.utilities.ApplicationConstants;

public class DeleteFormsActivity extends CollectAbstractActivity {

//    private DataManagerList dataManagerList = DataManagerList.newInstance();
//    private FormManagerList formManagerList = FormManagerList.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.file_manager_layout_custom);
        Toolbar toolbar = findViewById(R.id.toolbar);

        String formMode = getIntent().getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
//        if (ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
//            setTitle(getString(R.string.manage_files));
//            dataManagerList.setFormMode(ApplicationConstants.FormModes.EDIT_SAVED);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, dataManagerList)
//                    .commit();
//        } else if (ApplicationConstants.FormModes.VIEW_SENT.equalsIgnoreCase(formMode)) {
//            setTitle(getString(R.string.delete_sent_forms));
//            dataManagerList.setFormMode(ApplicationConstants.FormModes.VIEW_SENT);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, dataManagerList)
//                    .commit();
//        } else {
//            setTitle(getString(R.string.delete_yes));
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, formManagerList)
//                    .commit();
//        }
        setSupportActionBar(toolbar);
    }
}
