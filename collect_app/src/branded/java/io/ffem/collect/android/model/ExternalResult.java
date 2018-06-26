package io.ffem.collect.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ExternalResult {

    @SerializedName("result")
    private List<TestResult> results;

    public ExternalResult(List<TestResult> results) {
        this.results = results;
    }

    public List<TestResult> getResults() {
        return results;
    }
}
