/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.ExternalParamsException;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.external.ExternalAppsUtils;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.TextUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.WidgetFactory;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ffem.collect.android.widget.RowView;
import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * This class is
 *
 * @author carlhartung
 */
@SuppressLint("ViewConstructor")
public class ODKView extends FrameLayout implements OnLongClickListener {

    public static final String FIELD_LIST = "field-list";
    private final LinearLayout view;
    private final LinearLayout.LayoutParams layout;
    private final LinearLayout.LayoutParams bottomMargin;
    private final ArrayList<QuestionWidget> widgets;
    private boolean groupAdded = false;

    public ODKView(Context context, final FormEntryPrompt[] questionPrompts,
                   FormEntryCaption[] groups, boolean advancingPage) {
        super(context);

        for (FormEntryPrompt prompt : questionPrompts) {
            prompt.getQuestion().setAdditionalAttribute("", "done", null);
        }

        setScrollBarSize(10);

        inflate(getContext(), R.layout.nested_scroll_view, this); // keep in an xml file to enable the vertical scrollbar

        widgets = new ArrayList<>();

        view = new LinearLayout(getContext());
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(Gravity.TOP);
//        view.setPadding(0, 7, 0, 0);

        layout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setMargins(12, 3, 12, 5);

        bottomMargin =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomMargin.setMargins(0, 20, 0, 0);

        ThemeUtils themeUtils = new ThemeUtils(context);

        // get the group we are showing -- it will be the last of the groups in the groups list
        if (groups != null && groups.length > 0) {
            final FormEntryCaption c = groups[groups.length - 1];
            IFormElement formElement = c.getFormElement();
            String intentString = getIntentString(formElement);
            if (formElement instanceof GroupDef && intentString != null) {

                addGroupText(groups);
                groupAdded = true;

                List<FormEntryPrompt> formEntryPrompts = new ArrayList<>();
                for (FormEntryPrompt prompt : questionPrompts) {
                    if (prompt != null && prompt.getIndex().getLocalIndex() == c.getIndex().getLocalIndex()) {
                        formEntryPrompts.add(prompt);
                    }
                }

                addExternalQuestion(context, formElement.getLabelInnerText(),
                        formEntryPrompts.toArray(new FormEntryPrompt[0]),
                        themeUtils, c, intentString);

            } else {

                for (int i = 0; i < formElement.getChildren().size(); i++) {
                    if (formElement.getChild(i) instanceof GroupDef) {

                        intentString = getIntentString(formElement.getChild(i));

                        if (intentString != null) {

                            if (!groupAdded) {
                                addGroupText(groups);
                                groupAdded = true;
                            }

                            List<FormEntryPrompt> formEntryPrompts = new ArrayList<>();
                            for (FormEntryPrompt prompt : questionPrompts) {
                                if (prompt != null && prompt.getIndex().getNextLevel().getLocalIndex() == i) {
                                    formEntryPrompts.add(prompt);
                                }
                            }

                            addExternalQuestion(context, formElement.getChildren().get(i).getLabelInnerText(),
                                    formEntryPrompts.toArray(new FormEntryPrompt[0]), themeUtils, c, intentString);
                        }
                    } else {
                        for (FormEntryPrompt prompt : questionPrompts) {
                            if (formElement.getChild(i).getID() == prompt.getQuestion().getID()) {
                                if (prompt.getQuestion().getAdditionalAttribute("", "done") == null) {
                                    addEntryPrompt(groups, prompt);
                                }
                            }
                        }
                    }
                }
            }
        }

        boolean isRequired = false;
        for (FormEntryPrompt p : questionPrompts) {
            if (p.isRequired() && !isRequired) {
                isRequired = true;
            }

            if (p.getQuestion().getAdditionalAttribute("", "done") == null) {
                addEntryPrompt(groups, p);
            }
        }

        ((NestedScrollView) findViewById(R.id.odk_view_container)).addView(view);

        // see if there is an autoplay option.
        // Only execute it during forward swipes through the form
        if (advancingPage && widgets.size() == 1) {
            final String playOption = widgets.get(
                    0).getFormEntryPrompt().getFormElement().getAdditionalAttribute(null, "autoplay");
            if (playOption != null) {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (playOption.equalsIgnoreCase("audio")) {
                        widgets.get(0).playAudio();
                    } else if (playOption.equalsIgnoreCase("video")) {
                        widgets.get(0).playVideo();
                    }
                }, 150);
            }
        }
    }

    private void addEntryPrompt(FormEntryCaption[] groups, FormEntryPrompt p) {
        if (view.getChildCount() > 1) {
            View divider = new View(getContext());
            divider.setBackgroundResource(new ThemeUtils(getContext()).getDivider());
            divider.setMinimumHeight(3);
            view.addView(divider);
        }

        if (!groupAdded) {
            addGroupText(groups);
            groupAdded = true;
        }

        // if question or answer type is not supported, use text widget
        QuestionWidget qw =
                WidgetFactory.createWidgetFromPrompt(p, getContext(), false);
        qw.setLongClickable(true);
        qw.setOnLongClickListener(this);
        qw.setId(ViewIds.generateViewId());
        p.getQuestion().setAdditionalAttribute("", "done", "true");

        widgets.add(qw);
        view.addView(qw, layout);
    }

    private String getIntentString(IFormElement formElement) {
        String intentString = formElement.getAdditionalAttribute(null, "intent");
        if (intentString == null) {
            intentString = formElement.getAppearanceAttr();
        }

        if (intentString != null) {
            if (intentString.startsWith("ex:")) {
                intentString = intentString.replaceFirst("^ex[:]", "");
            }

            if (intentString.length() < 36 || !intentString.startsWith("io.ffem")) {
                return null;
            }
        }

        return intentString;
    }

    private void addExternalQuestion(Context context, String longText, FormEntryPrompt[] formEntryPrompts,
                                     ThemeUtils themeUtils, FormEntryCaption c, String intentString) {
        if (formEntryPrompts.length > 0) {

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setPadding(0, 0, 0, 10);

            if (view.getChildCount() > 1) {
                View divider = new View(getContext());
                divider.setBackgroundResource(new ThemeUtils(getContext()).getDivider());
                divider.setMinimumHeight(3);
                linearLayout.addView(divider);
            }

            linearLayout.setOrientation(LinearLayout.VERTICAL);

            TextView questionText = getQuestionText(formEntryPrompts, themeUtils, longText);
            linearLayout.addView(questionText, layout);

            for (FormEntryPrompt p : formEntryPrompts) {
                QuestionWidget qw =
                        WidgetFactory.createWidgetFromPrompt(p, getContext(), false);
                qw.setContainer(linearLayout);

                widgets.add(qw);
                if (qw.getAnswer() != null) {
                    RowView answerRow = new RowView(context);
                    answerRow.setPrimaryText(qw.getQuestionText() + ": ");
                    answerRow.setSecondaryText(qw.getAnswer().getDisplayText());
                    linearLayout.addView(answerRow, layout);
                }
            }

            Button button = getLauncherButton(context, formEntryPrompts, c, intentString);
            linearLayout.addView(button);

            view.addView(linearLayout);
        }
    }

    @NonNull
    public static String getGroupsPath(FormEntryCaption[] groups) {
        StringBuilder path = new StringBuilder("");
        if (groups != null) {
            String longText;
            int multiplicity;
            int index = 1;
            // list all groups in one string
            for (FormEntryCaption group : groups) {
                multiplicity = group.getMultiplicity() + 1;
                String intentString = group.getFormElement()
                        .getAdditionalAttribute(null, "intent");
                if ((intentString == null || intentString.isEmpty())) {
                    longText = group.getLongText();
                    if (longText != null) {
                        path.append(longText);
                        if (group.repeats() && multiplicity > 0) {
                            path
                                    .append(" (")
                                    .append(multiplicity)
                                    .append(")\u200E");
                        }
                        if (index < groups.length) {
                            path.append(" > ");
                        }
                        index++;
                    }
                }
            }
        }

        return path.toString();
    }

    public Bundle getState() {
        Bundle state = new Bundle();
        for (QuestionWidget qw : getWidgets()) {
            state.putAll(qw.getCurrentState());
        }

        return state;
    }

    /**
     * http://code.google.com/p/android/issues/detail?id=8488
     */
    public void recycleDrawables() {
        this.destroyDrawingCache();
        view.destroyDrawingCache();
        for (QuestionWidget q : widgets) {
            q.recycleDrawables();
        }
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        Collect.getInstance().getActivityLogger().logScrollAction(this, t - oldt);
    }

    /**
     * @return a HashMap of answers entered by the user for this set of widgets
     */
    public HashMap<FormIndex, IAnswerData> getAnswers() {
        HashMap<FormIndex, IAnswerData> answers = new LinkedHashMap<>();
        for (QuestionWidget q : widgets) {
            /*
             * The FormEntryPrompt has the FormIndex, which is where the answer gets stored. The
             * QuestionWidget has the answer the user has entered.
             */
            FormEntryPrompt p = q.getFormEntryPrompt();
            answers.put(p.getIndex(), q.getAnswer());
        }

        return answers;
    }

    /**
     * // * Add a TextView containing the hierarchy of groups to which the question belongs. //
     */
    private void addGroupText(FormEntryCaption[] groups) {
        String path = getGroupsPath(groups);

        // build view
        if (!path.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText(path);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize());
            tv.setPadding(4, 0, 5, 7);
            view.addView(tv, 0, layout);
        }
    }

    private Button getLauncherButton(Context context, FormEntryPrompt[] questionPrompts,
                                     FormEntryCaption c, String intentString) {
        final String buttonText;
        final String errorString;
        String v = c.getSpecialFormQuestionText("buttonText");
        buttonText = (v != null) ? v : context.getString(R.string.launch_app);
        v = c.getSpecialFormQuestionText("noAppErrorString");
        errorString = (v != null) ? v : context.getString(R.string.no_app);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(12, 5, 12, 10);

        // set button formatting
        Button launchIntentButton = new Button(getContext());
        launchIntentButton.setId(ViewIds.generateViewId());
        launchIntentButton.setText(buttonText);
        launchIntentButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                Collect.getQuestionFontsize() + 4);
        launchIntentButton.setPadding(10, 30, 10, 30);
        launchIntentButton.setLayoutParams(params);

        launchIntentButton.setOnClickListener(v1 -> {
            String intentName = ExternalAppsUtils.extractIntentName(intentString);
            Map<String, String> parameters = ExternalAppsUtils.extractParameters(
                    intentString);

            Intent i = new Intent(intentName);
            try {
                ExternalAppsUtils.populateParameters(i, parameters,
                        c.getIndex().getReference());

                for (FormEntryPrompt p : questionPrompts) {
                    IFormElement formElement = p.getFormElement();
                    if (formElement instanceof QuestionDef) {
                        TreeReference reference =
                                (TreeReference) formElement.getBind().getReference();
                        IAnswerData answerValue = p.getAnswerValue();
                        Object value =
                                answerValue == null ? null : answerValue.getValue();
                        switch (p.getDataType()) {
                            case Constants.DATATYPE_TEXT:
                            case Constants.DATATYPE_INTEGER:
                            case Constants.DATATYPE_DECIMAL:
                                i.putExtra(reference.getNameLast(),
                                        (Serializable) value);
                                break;
                        }
                    }
                }

                ((Activity) getContext()).startActivityForResult(i, RequestCodes.EX_GROUP_CAPTURE);
            } catch (ExternalParamsException e) {
                Timber.e(e, "ExternalParamsException");

                ToastUtils.showShortToast(e.getMessage());
            } catch (ActivityNotFoundException e) {
                Timber.d(e, "ActivityNotFoundExcept");

                if (intentName.startsWith("io.ffem.soil") || intentName.startsWith("io.ffem.water")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog);

                    builder.setTitle(R.string.app_not_found)
                            .setMessage(R.string.install_app)
                            .setPositiveButton(R.string.go_to_play_store, (dialogInterface, i1)
                                    -> ODKView.this.getContext().startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/developer?id=Foundation+for+Environmental+Monitoring"))))
                            .setNegativeButton(android.R.string.cancel,
                                    (dialogInterface, i1) -> dialogInterface.dismiss())
                            .setCancelable(false)
                            .show();
                } else {
                    ToastUtils.showShortToast(errorString);
                }
            }
        });

        return launchIntentButton;
    }

    private TextView getQuestionText(FormEntryPrompt[] questionPrompts, ThemeUtils themeUtils, String longText) {
        TextView questionText = new TextView(getContext());

        boolean isRequired = false;

        for (FormEntryPrompt prompt : questionPrompts) {
            prompt.getQuestion().setAdditionalAttribute("", "done", "true");
            if (prompt.isRequired() && !isRequired) {
                isRequired = true;
            }
        }

        questionText.setText(TextUtils.textToHtml(FormEntryPromptUtils
                .markQuestionIfIsRequired(longText, isRequired)));

        questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Collect.getQuestionFontsize() + 2);
        questionText.setTypeface(null, Typeface.BOLD);
        questionText.setPadding(5, 5, 5, 0);
        questionText.setTextColor(themeUtils.getPrimaryTextColor());
        questionText.setMovementMethod(LinkMovementMethod.getInstance());

        // Wrap to the size of the parent view
        questionText.setHorizontallyScrolling(false);

        return questionText;
    }

    public void setFocus(Context context) {
        if (!widgets.isEmpty()) {
            widgets.get(0).setFocus(context);
        }
    }

    /**
     * Called when another activity returns information to answer this question.
     */
    public void setBinaryData(Object answer) {
        boolean set = false;
        for (QuestionWidget q : widgets) {
            if (q instanceof BinaryWidget) {
                BinaryWidget binaryWidget = (BinaryWidget) q;
                if (binaryWidget.isWaitingForData()) {
                    try {
                        binaryWidget.setBinaryData(answer);
                        binaryWidget.cancelWaitingForData();
                    } catch (Exception e) {
                        Timber.e(e);
                        ToastUtils.showLongToast(getContext().getString(R.string.error_attaching_binary_file,
                                e.getMessage()));
                    }
                    set = true;
                    break;
                }
            }
        }

        if (!set) {
            Timber.w("Attempting to return data to a widget or set of widgets not looking for data");
        }
    }

    public void setDataForFields(Bundle bundle) throws JavaRosaException {
        if (bundle == null) {
            return;
        }
        FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            return;
        }
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            for (QuestionWidget questionWidget : widgets) {
                FormEntryPrompt prompt = questionWidget.getFormEntryPrompt();
                TreeReference treeReference =
                        (TreeReference) prompt.getFormElement().getBind().getReference();

                if (treeReference.getNameLast().equals(key)) {

                    switch (prompt.getDataType()) {
                        case Constants.DATATYPE_TEXT:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asStringData(bundle.get(key)));
                            break;
                        case Constants.DATATYPE_INTEGER:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asIntegerData(bundle.get(key)));
                            break;
                        case Constants.DATATYPE_DECIMAL:
                            formController.saveAnswer(prompt.getIndex(),
                                    ExternalAppsUtils.asDecimalData(bundle.get(key)));
                            break;
                        default:
                            throw new RuntimeException(
                                    getContext().getString(R.string.ext_assign_value_error,
                                            treeReference.toString(false)));
                    }

                    break;
                }
            }
        }
    }

    public void cancelWaitingForBinaryData() {
        int count = 0;
        for (QuestionWidget q : widgets) {
            if (q instanceof BinaryWidget) {
                if (q.isWaitingForData()) {
                    q.cancelWaitingForData();
                    ++count;
                }
            }
        }

        if (count != 1) {
            Timber.w("Attempting to cancel waiting for binary data to a widget or set of widgets not looking for data");
        }
    }

    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX,
                                        float velocityY) {
        for (QuestionWidget q : widgets) {
            if (q.suppressFlingGesture(e1, e2, velocityX, velocityY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the answer was cleared, false otherwise.
     */
    public boolean clearAnswer() {
        // If there's only one widget, clear the answer.
        // If there are more, then force a long-press to clear the answer.
        if (widgets.size() == 1 && !widgets.get(0).getFormEntryPrompt().isReadOnly()) {
            widgets.get(0).clearAnswer();
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<QuestionWidget> getWidgets() {
        return widgets;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        for (int i = 0; i < widgets.size(); i++) {
            QuestionWidget qw = widgets.get(i);
            qw.setOnFocusChangeListener(l);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (QuestionWidget qw : widgets) {
            qw.cancelLongPress();
        }
    }

    public void stopAudio() {
        if (widgets.size() > 0) {
            widgets.get(0).stopAudio();
        }
    }

    /**
     * Releases widget resources, such as {@link android.media.MediaPlayer}s
     */
    public void releaseWidgetResources() {
        for (QuestionWidget w : widgets) {
            w.release();
        }
    }

    public void highlightWidget(FormIndex formIndex) {
        QuestionWidget qw = getQuestionWidget(formIndex);
        View viewToHighlight;
        if (qw != null) {
            if (qw.getContainer() != null) {
                viewToHighlight = qw.getContainer();
            } else {
                viewToHighlight = qw;
            }
            // postDelayed is needed because otherwise scrolling may not work as expected in case when
            // answers are validated during form finalization.
            new Handler().postDelayed(() -> {
                findViewById(R.id.odk_view_container).scrollTo(0, qw.getTop());

                ValueAnimator va = new ValueAnimator();
                va.setIntValues(getResources().getColor(R.color.highlight), getDrawingCacheBackgroundColor());
                va.setEvaluator(new ArgbEvaluator());
                va.addUpdateListener(valueAnimator -> viewToHighlight.setBackgroundColor((int) valueAnimator.getAnimatedValue()));
                va.setDuration(3500);
                va.start();
            }, 100);
        }
    }

    private QuestionWidget getQuestionWidget(FormIndex formIndex) {
        for (QuestionWidget qw : widgets) {
            if (formIndex.equals(qw.getFormEntryPrompt().getIndex())) {
                return qw;
            }
        }
        return null;
    }
}