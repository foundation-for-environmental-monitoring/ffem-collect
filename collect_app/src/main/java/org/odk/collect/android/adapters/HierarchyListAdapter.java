/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.logic.HierarchyElement;
import org.odk.collect.android.utilities.StringUtils;
import org.odk.collect.android.utilities.FormEntryPromptUtils;

import java.util.ArrayList;
import java.util.List;

public class HierarchyListAdapter extends RecyclerView.Adapter<HierarchyListAdapter.ViewHolder> {

    private final OnElementClickListener listener;

    private final List<HierarchyElement> hierarchyElements;
    private boolean isEnabled;

    public HierarchyListAdapter(List<HierarchyElement> listElements, OnElementClickListener listener, boolean isEnabled) {
        this.hierarchyElements = listElements;
        this.listener = listener;
        this.isEnabled = isEnabled;
    }

    @Override
    public HierarchyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hierarchy_element, parent, false);

        if (!isEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setForeground(null);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HierarchyElement element = hierarchyElements.get(position);

        holder.bind(element, listener);
        if (element.getIcon() != null) {
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageDrawable(hierarchyElements.get(position).getIcon());
        } else {
            holder.icon.setVisibility(View.GONE);
        }
        if (element.getSecondaryText() != null) {
            holder.secondaryText.setVisibility(View.VISIBLE);

            String answer = "";
            ArrayList<HierarchyElement> list = element.getIntentChildren();
            if (list.size() > 0) {
                for (HierarchyElement e : list) {
                    if (e.getSecondaryText() != null) {
                        if (!answer.isEmpty()){
                            answer += "<br/>";
                        }
                        answer += e.getPrimaryText() + ": " + e.getSecondaryText();
                    }
                }
            } else {
                answer = FormEntryPromptUtils
                        .markQuestionIfIsRequired(element.getSecondaryText(),
                                element.isRequired());
            }

            if (answer.isEmpty()){
                addOrRemoveProperty(holder.primaryText, RelativeLayout.CENTER_VERTICAL, true);
                holder.secondaryText.setVisibility(View.GONE);
            } else {
                addOrRemoveProperty(holder.primaryText, RelativeLayout.CENTER_VERTICAL, false);
                holder.secondaryText.setText(StringUtils.textToHtml(answer));
            }
        } else {
            addOrRemoveProperty(holder.primaryText, RelativeLayout.CENTER_VERTICAL, true);
            holder.secondaryText.setVisibility(View.GONE);
        }

        holder.primaryText.setText(StringUtils.textToHtml(FormEntryPromptUtils
                .markQuestionIfIsRequired(element.getPrimaryText(),
                        element.isRequired())));
    }

    private void addOrRemoveProperty(View view, int property, boolean flag){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if(flag){
            layoutParams.addRule(property);
        }else {
            layoutParams.removeRule(property);
        }
        view.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return hierarchyElements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView primaryText;
        TextView secondaryText;

        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            primaryText = v.findViewById(R.id.primary_text);
            secondaryText = v.findViewById(R.id.secondary_text);
        }

        void bind(final HierarchyElement element, final OnElementClickListener listener) {
            itemView.setOnClickListener(v -> listener.onElementClick(element));
        }
    }

    public interface OnElementClickListener {
        void onElementClick(HierarchyElement element);
    }
}

//    public int getItemIndex(HierarchyElement hierarchyElement) {
//        return hierarchyElements.indexOf(hierarchyElement);
//    }

//        holder.primaryText.setText(TextUtils.textToHtml(hierarchyElements.get(position).getPrimaryText()));
//                if (hierarchyElements.get(position).getSecondaryText() != null && !hierarchyElements.get(position).getSecondaryText().isEmpty()) {
//                holder.secondaryText.setVisibility(View.VISIBLE);
//                holder.secondaryText.setText(TextUtils.textToHtml(hierarchyElements.get(position).getSecondaryText()));
//                } else {
//                holder.secondaryText.setVisibility(View.GONE);
//                }