package com.expensetracker.activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.adapters.BudgetAdapter;
import com.expensetracker.database.DataManager;
import com.expensetracker.models.Budget;
import com.expensetracker.utils.Categories;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BudgetActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private DataManager dataManager;
    private String currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        dataManager = DataManager.getInstance(this);
        currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
            .format(Calendar.getInstance().getTime());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Budget Limits — " + currentMonth);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.rv_budgets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadBudgets();

        findViewById(R.id.btn_add_budget).setOnClickListener(v -> showAddBudgetDialog());
    }

    private void loadBudgets() {
        List<Budget> budgets = dataManager.getAllBudgets();
        // Filter for current month
        java.util.List<Budget> monthBudgets = new java.util.ArrayList<>();
        for (Budget b : budgets) {
            if (b.getMonth().equals(currentMonth)) monthBudgets.add(b);
        }
        adapter = new BudgetAdapter(this, monthBudgets, dataManager, currentMonth, this::loadBudgets);
        recyclerView.setAdapter(adapter);
    }

    private void showAddBudgetDialog() {
        String[] categoriesArr = Categories.ALL.toArray(new String[0]);
        final int[] selected = {0};

        new MaterialAlertDialogBuilder(this)
            .setTitle("Select Category")
            .setSingleChoiceItems(categoriesArr, 0, (d, which) -> selected[0] = which)
            .setPositiveButton("Next", (d, w) -> showAmountDialog(categoriesArr[selected[0]]))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAmountDialog(String category) {
        TextInputEditText input = new TextInputEditText(this);
        input.setHint("Budget limit (₹)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        Budget existing = dataManager.getBudgetForCategory(category, currentMonth);
        if (existing != null) input.setText(String.valueOf(existing.getLimit()));

        new MaterialAlertDialogBuilder(this)
            .setTitle("Set budget for " + category)
            .setView(input)
            .setPositiveButton("Save", (d, w) -> {
                String val = input.getText() != null ? input.getText().toString() : "";
                if (!val.isEmpty()) {
                    double limit = Double.parseDouble(val);
                    dataManager.setBudget(new Budget(category, limit, currentMonth));
                    loadBudgets();
                    Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
