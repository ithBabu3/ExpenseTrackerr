package com.expensetracker.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.expensetracker.R;
import com.expensetracker.activities.SyncActivity;
import com.expensetracker.database.DataManager;
import com.expensetracker.utils.ExcelExporter;
import com.expensetracker.models.Expense;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.*;

public class SettingsFragment extends Fragment {

    private DataManager dataManager;
    private TextView tvUserName, tvStoragePath, tvExpenseCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataManager = DataManager.getInstance(requireContext());

        tvUserName = view.findViewById(R.id.tv_user_name);
        tvStoragePath = view.findViewById(R.id.tv_storage_path);
        tvExpenseCount = view.findViewById(R.id.tv_expense_count);

        MaterialButton btnEditName = view.findViewById(R.id.btn_edit_name);
        MaterialButton btnExportExcel = view.findViewById(R.id.btn_export_excel);
        MaterialButton btnSync = view.findViewById(R.id.btn_go_sync);
        MaterialButton btnClearData = view.findViewById(R.id.btn_clear_data);

        loadInfo();

        btnEditName.setOnClickListener(v -> showEditNameDialog());
        btnExportExcel.setOnClickListener(v -> exportExcel());
        btnSync.setOnClickListener(v ->
            startActivity(new Intent(getActivity(), SyncActivity.class)));
        btnClearData.setOnClickListener(v -> confirmClearData());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInfo();
    }

    private void loadInfo() {
        SharedPreferences prefs = requireContext()
            .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        String name = prefs.getString("user_name", "Set your name");
        tvUserName.setText("👤 " + name);
        tvStoragePath.setText("📁 " + dataManager.getDataFilePath());
        tvExpenseCount.setText("📊 " + dataManager.getAllExpenses().size() + " expenses stored");
    }

    private void showEditNameDialog() {
        TextInputEditText input = new TextInputEditText(requireContext());
        input.setHint("Your name");
        SharedPreferences prefs = requireContext()
            .getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        input.setText(prefs.getString("user_name", ""));

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Your Name")
            .setMessage("This name is shown when you add expenses in group mode.")
            .setView(input)
            .setPositiveButton("Save", (d, w) -> {
                String name = input.getText() != null ? input.getText().toString().trim() : "";
                if (!name.isEmpty()) {
                    prefs.edit().putString("user_name", name).apply();
                    loadInfo();
                    Toast.makeText(getContext(), "Name saved!", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void exportExcel() {
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
            .format(Calendar.getInstance().getTime());
        List<Expense> expenses = dataManager.getAllExpenses();

        String path = ExcelExporter.export(requireContext(), expenses, month);
        if (path != null) {
            Toast.makeText(getContext(),
                "✅ Exported to: " + path, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(),
                "❌ Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmClearData() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("⚠️ Clear All Data")
            .setMessage("This will delete all expenses and budgets. This cannot be undone. Are you sure?")
            .setPositiveButton("Clear Everything", (d, w) -> {
                // Clear by replacing with empty AppData
                dataManager.getAppData().getExpenses().clear();
                dataManager.getAppData().getBudgets().clear();
                dataManager.saveData();
                loadInfo();
                Toast.makeText(getContext(), "All data cleared", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
