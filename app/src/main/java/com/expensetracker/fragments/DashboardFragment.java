package com.expensetracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.activities.BudgetActivity;
import com.expensetracker.activities.SyncActivity;
import com.expensetracker.adapters.ExpenseAdapter;
import com.expensetracker.database.DataManager;
import com.expensetracker.models.Budget;
import com.expensetracker.models.Expense;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardFragment extends Fragment {

    private DataManager dataManager;
    private TextView tvMonthTotal, tvMonthName, tvBudgetSummary, tvGreeting;
    private RecyclerView rvRecent;
    private ExpenseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataManager = DataManager.getInstance(requireContext());

        tvMonthTotal = view.findViewById(R.id.tv_month_total);
        tvMonthName = view.findViewById(R.id.tv_month_name);
        tvBudgetSummary = view.findViewById(R.id.tv_budget_summary);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        rvRecent = view.findViewById(R.id.rv_recent_expenses);

        MaterialButton btnBudget = view.findViewById(R.id.btn_manage_budget);
        MaterialButton btnSync = view.findViewById(R.id.btn_sync);

        btnBudget.setOnClickListener(v ->
            startActivity(new Intent(getActivity(), BudgetActivity.class)));
        btnSync.setOnClickListener(v ->
            startActivity(new Intent(getActivity(), SyncActivity.class)));

        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
            .format(Calendar.getInstance().getTime());
        String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(Calendar.getInstance().getTime());

        tvMonthName.setText(monthName);

        // Greeting
        android.content.SharedPreferences prefs =
            requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        String user = prefs.getString("user_name", "Friend");
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        tvGreeting.setText(greeting + ", " + user + " 👋");

        // Monthly total
        List<Expense> allExpenses = dataManager.getAllExpenses();
        double monthTotal = 0;
        List<Expense> monthExpenses = new ArrayList<>();
        for (Expense e : allExpenses) {
            if (e.getDate().startsWith(currentMonth)) {
                monthTotal += e.getAmount();
                monthExpenses.add(e);
            }
        }
        tvMonthTotal.setText("₹ " + String.format("%.2f", monthTotal));

        // Budget summary
        List<Budget> budgets = dataManager.getAllBudgets();
        double totalBudget = 0;
        for (Budget b : budgets) {
            if (b.getMonth().equals(currentMonth)) totalBudget += b.getLimit();
        }
        if (totalBudget > 0) {
            double pct = (monthTotal / totalBudget) * 100;
            tvBudgetSummary.setText(String.format("%.0f%%", pct) + " of ₹"
                + String.format("%.0f", totalBudget) + " budget used");
        } else {
            tvBudgetSummary.setText("No budget set for this month");
        }

        // Recent expenses (last 5)
        monthExpenses.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        List<Expense> recent = monthExpenses.subList(0, Math.min(5, monthExpenses.size()));

        if (adapter == null) {
            adapter = new ExpenseAdapter(getContext(), new ArrayList<>(recent),
                dataManager, this::loadData);
            rvRecent.setAdapter(adapter);
        } else {
            adapter.updateData(new ArrayList<>(recent));
        }
    }
}
