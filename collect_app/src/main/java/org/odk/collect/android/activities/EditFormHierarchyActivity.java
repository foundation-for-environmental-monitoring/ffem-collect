/*
 * Copyright (C) 2017 Shobhit
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

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.HierarchyListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.HierarchyElement;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;

public class EditFormHierarchyActivity extends FormHierarchyActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isEnabled = true;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onElementClick(HierarchyElement element) {
        int position = formList.indexOf(element);
        FormIndex index = element.getFormIndex();
        if (index == null) {
            goUpLevel();
            return;
        }

        switch (element.getType()) {
            case EXPANDED:
                element.setType(COLLAPSED);
                ArrayList<HierarchyElement> children = element.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    formList.remove(position + 1);
                }
                element.setIcon(ContextCompat.getDrawable(this, R.drawable.expander_ic_minimized));
                break;
            case COLLAPSED:
                element.setType(EXPANDED);
                ArrayList<HierarchyElement> children1 = element.getChildren();
                for (int i = 0; i < children1.size(); i++) {
                    Timber.i("adding child: %s", children1.get(i).getFormIndex());
                    formList.add(position + 1 + i, children1.get(i));

                }
                element.setIcon(ContextCompat.getDrawable(this, R.drawable.expander_ic_maximized));
                break;
            case QUESTION:
            case EVENT_GROUP:
                Collect.getInstance().getFormController().jumpToIndex(index);

                String intentString = null;
                if (Collect.getInstance().getFormController().getGroupsForCurrentIndex().length > 0) {
                    intentString = Collect.getInstance().getFormController().getGroupsForCurrentIndex()[0]
                            .getFormElement().getAdditionalAttribute(null, "intent");
                }

                if (Collect.getInstance().getFormController().indexIsInFieldList()
                        && intentString == null) {
                    try {
                        Collect.getInstance().getFormController().stepToPreviousScreenEvent();
                    } catch (JavaRosaException e) {
                        Timber.d(e);
                        createErrorDialog(e.getCause().getMessage());
                        return;
                    }
                }
                setResult(RESULT_OK);
                finish();
                return;
            case CHILD:
                Collect.getInstance().getFormController().jumpToIndex(element.getFormIndex());
                setResult(RESULT_OK);
                refreshView();
                return;
        }

        List<HierarchyElement> newFormList = new ArrayList<>();

        int i = 0;
        while (i < formList.size()) {
            boolean isRequired = false;
            HierarchyElement item = formList.get(i);
            item.getIntentChildren().clear();
            newFormList.add(item);

            if (item.getType() == EVENT_GROUP) {
                for (int ii = i + 1; ii < formList.size(); ii++) {
                    HierarchyElement childItem = formList.get(ii);
                    if (childItem.getType() != EVENT_GROUP) {
                        if (childItem.getFormIndex().toString().startsWith(item.getFormIndex().toString())) {
                            if (!isRequired && childItem.isRequired()) {
                                isRequired = true;
                                childItem.setRequired(false);
                            }
                            item.addIntentChild(childItem);
                            i++;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }

                }
                item.setRequired(isRequired);
            }
            i++;
        }

        recyclerView.setAdapter(new HierarchyListAdapter(newFormList, this::onElementClick, isEnabled));
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                FormController fc = Collect.getInstance().getFormController();
                if (fc != null) {
                    fc.getTimerLogger().exitView();
                    fc.jumpToIndex(startIndex);
                }
        }
        return super.onKeyDown(keyCode, event);
    }
}
