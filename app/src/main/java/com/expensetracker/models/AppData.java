package com.expensetracker.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppData implements Serializable {
    private List<Expense> expenses;
    private List<Budget> budgets;
    private Group group;
    private long lastModified;
    private String version = "1.0";

    public AppData() {
        this.expenses = new ArrayList<>();
        this.budgets = new ArrayList<>();
        this.lastModified = System.currentTimeMillis();
    }

    public List<Expense> getExpenses() { return expenses; }
    public void setExpenses(List<Expense> expenses) { this.expenses = expenses; }

    public List<Budget> getBudgets() { return budgets; }
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public void touch() { this.lastModified = System.currentTimeMillis(); }
}
