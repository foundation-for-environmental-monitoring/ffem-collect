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

package org.odk.collect.android.logic;

import android.graphics.drawable.Drawable;

import org.javarosa.core.model.FormIndex;

import java.util.ArrayList;

public class HierarchyElement {
    private final ArrayList<HierarchyElement> children = new ArrayList<>();
    private final ArrayList<HierarchyElement> intentChildren = new ArrayList<>();

    private int type;
    private final FormIndex formIndex;
    private final String primaryText;
    private final String secondaryText;
    private Drawable icon;
    private boolean isRequired;

    public HierarchyElement(String primaryText, String secondaryText, Drawable icon,
                            int type, FormIndex formIndex, boolean isRequired) {
        this.primaryText = primaryText;
        this.secondaryText = secondaryText;
        this.icon = icon;
        this.type = type;
        this.formIndex = formIndex;
        this.isRequired = isRequired;
    }

    public String getPrimaryText() {
        return primaryText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public FormIndex getFormIndex() {
        return formIndex;
    }

    public int getType() {
        return type;
    }

    public void setType(int newType) {
        type = newType;
    }

    public ArrayList<HierarchyElement> getChildren() {
        return children;
    }

    public ArrayList<HierarchyElement> getIntentChildren() {
        return intentChildren;
    }

    public void addChild(HierarchyElement h) {
        children.add(h);
    }

    public void addIntentChild(HierarchyElement h) {
        intentChildren.add(h);
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }
}
