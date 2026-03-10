package com.expensetracker.models;

import java.io.Serializable;

public class Budget implements Serializable {
    private String category;
    private double limit;
    private String month;   // "yyyy-MM"

    public Budget() {}

    public Budget(String category, double limit, String month) {
        this.category = category;
        this.limit = limit;
        this.month = month;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
}
