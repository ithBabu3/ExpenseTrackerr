package com.expensetracker.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.expensetracker.R;
import com.expensetracker.database.DataManager;
import com.expensetracker.models.Expense;
import com.expensetracker.utils.Categories;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditExpenseActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etNote, etDate;
    private Spinner spinnerCategory;
    private DataManager dataManager;
    private Expense editingExpense;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        dataManager = DataManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etTitle = findViewById(R.id.et_title);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        etDate = findViewById(R.id.et_date);
        spinnerCategory = findViewById(R.id.spinner_category);
        Button btnSave = findViewById(R.id.btn_save);

        // Setup category spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, Categories.ALL);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Date picker
        updateDateField();
        etDate.setOnClickListener(v -> showDatePicker());

        // Check if editing
        editingExpense = (Expense) getIntent().getSerializableExtra("expense");
        if (editingExpense != null) {
            getSupportActionBar().setTitle("Edit Expense");
            populateFields(editingExpense);
        } else {
            getSupportActionBar().setTitle("Add Expense");
        }

        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void populateFields(Expense e) {
        etTitle.setText(e.getTitle());
        etAmount.setText(String.valueOf(e.getAmount()));
        etNote.setText(e.getNote());
        etDate.setText(e.getDate());
        int pos = Categories.ALL.indexOf(e.getCategory());
        if (pos >= 0) spinnerCategory.setSelection(pos);
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate.set(year, month, day);
            updateDateField();
        }, selectedDate.get(Calendar.YEAR),
           selectedDate.get(Calendar.MONTH),
           selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveExpense() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String category = (String) spinnerCategory.getSelectedItem();

        if (title.isEmpty()) { etTitle.setError("Enter title"); return; }
        if (amountStr.isEmpty()) { etAmount.setError("Enter amount"); return; }

        double amount;
        try { amount = Double.parseDouble(amountStr); }
        catch (NumberFormatException e) { etAmount.setError("Invalid amount"); return; }

        // Get current user name
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String currentUser = prefs.getString("user_name", "User");

        if (editingExpense != null) {
            editingExpense.setTitle(title);
            editingExpense.setAmount(amount);
            editingExpense.setCategory(category);
            editingExpense.setDate(date);
            editingExpense.setNote(note);
            editingExpense.setTimestamp(System.currentTimeMillis());
            dataManager.updateExpense(editingExpense);
            Toast.makeText(this, "Expense updated!", Toast.LENGTH_SHORT).show();
        } else {
            Expense expense = new Expense(title, amount, category, date, note, currentUser);
            dataManager.addExpense(expense);
            Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show();
        }

        // Update sync server with latest data
        if (MainActivity.syncServer != null) {
            MainActivity.syncServer.updateLocalData(dataManager.getAppData());
        }

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
