package com.group18.greengrocer.model;

/**
 * Data Transfer Object for Report data.
 * Used to populate charts.
 */
public class ReportData {
    private String label;
    private double value;

    public ReportData(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
