package com.expensetracker.models;

import java.io.Serializable;
import java.util.UUID;

public class Expense implements Serializable {
    private String id;
    private String title;
    private double amount;
    private String category;
    private String date;        // "yyyy-MM-dd"
    private String note;
    private String addedBy;     // user who added this
    private long timestamp;

    public Expense() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public Expense(String title, double amount, String category, String date, String note, String addedBy) {
        this();
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.addedBy = addedBy;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
