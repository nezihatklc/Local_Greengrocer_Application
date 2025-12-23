package com.group18.greengrocer.model;

/**
 * Data Transfer Object for Report data.
 * Used to populate charts in the Owner interface.
 */
public class ReportData {
    
    /**
     * The label for the chart data point (e.g., "Product A", "2023-10-01").
     */
    private String label;
    
    /**
     * The numerical value for the chart data point.
     */
    private double value;

    /**
     * Default constructor.
     */
    public ReportData() {
    }

    /**
     * Constructor with fields.
     * 
     * @param label The data label.
     * @param value The data value.
     */
    public ReportData(String label, double value) {
        this.label = label;
        this.value = value;
    }

    /**
     * Gets the data label.
     * 
     * @return The label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the data label.
     * 
     * @param label The new label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the data value.
     * 
     * @return The value.
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the data value.
     * 
     * @param value The new value.
     */
    public void setValue(double value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "ReportData{label='" + label + "', value=" + value + "}";
    }
}
