package io.ffem.collect.android.model;

public class TestResult {

    private int id;
    private String name;
    private String value;
    private String unit;

    public TestResult(int id, String name, String value, String unit) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public String buildResultToDisplay() {
        return name + ": " + value + " " + unit;
    }
}
