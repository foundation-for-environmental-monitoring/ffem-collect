package io.ffem.collect.android.helper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.ffem.collect.android.model.ExternalResult;
import io.ffem.collect.android.model.TestResult;
import timber.log.Timber;

public class ResultHelper {

    public static String jsonToText(String s) {
        if (s != null && s.startsWith("{")) {
            try {
                final Gson gson = new Gson();
                ExternalResult externalResult = gson.fromJson(s, ExternalResult.class);
                if (externalResult != null && externalResult.getResults() != null) {
                    StringBuilder displayResult = new StringBuilder();
                    for (TestResult result :
                            externalResult.getResults()) {
                        if (!displayResult.toString().isEmpty()) {
                            displayResult.append("\n");
                        }
                        displayResult.append(result.buildResultToDisplay());
                    }
                    s = displayResult.toString();
                }
            } catch (JsonSyntaxException e) {
                Timber.e(e, "Error parsing result: %s", s);
            }
        }
        return s;
    }

}
