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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.javarosa.core.model.FormIndex;

import java.util.ArrayList;

/**
 * Represents a question or repeat to be shown in
 * {@link org.odk.collect.android.activities.FormHierarchyActivity}.
 */
public class HierarchyElement {
    private final ArrayList<HierarchyElement> intentChildren = new ArrayList<>();

    /**
     * The type and state of this element. See {@link Type}.
     */
    @NonNull
    private Type type;

    /**
     * The form index of this element.
     */
    @NonNull
    private final FormIndex formIndex;

    /**
     * The primary text this element should be displayed with.
     */
    @NonNull
    private final String primaryText;

    /**
     * The secondary text this element should be displayed with.
     */
    @Nullable
    private final String secondaryText;

    /**
     * An optional icon.
     */
    @Nullable
    private Drawable icon;
    private boolean isRequired;

    public HierarchyElement(@NonNull String primaryText, @Nullable String secondaryText,
                            @Nullable Drawable icon, @NonNull Type type, @NonNull FormIndex formIndex, boolean isRequired) {
        this.primaryText = primaryText;
        this.secondaryText = secondaryText;
        this.icon = icon;
        this.type = type;
        this.formIndex = formIndex;
        this.isRequired = isRequired;
    }

    @NonNull
    public String getPrimaryText() {
        return primaryText;
    }

    @Nullable
    public String getSecondaryText() {
        return secondaryText;
    }

    @Nullable
    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(@Nullable Drawable icon) {
        this.icon = icon;
    }

    @NonNull
    public FormIndex getFormIndex() {
        return formIndex;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    public void setType(@NonNull Type newType) {
        type = newType;
    }

    public ArrayList<HierarchyElement> getIntentChildren() {
        return intentChildren;
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

    /**
     * The type and state of this element.
     */
    public enum Type {
        QUESTION,
        VISIBLE_GROUP,
        REPEATABLE_GROUP,
        REPEAT_INSTANCE,
        COLLAPSED,
        PROPERTY
    }
}
