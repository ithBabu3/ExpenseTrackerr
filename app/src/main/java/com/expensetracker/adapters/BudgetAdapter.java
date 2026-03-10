package com.expensetracker.adapters;

import android.content.Context;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.database.DataManager;
import com.expensetracker.models.Budget;
import com.expensetracker.models.Expense;
import com.expensetracker.utils.Categories;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private Context context;
    private List<Budget> budgets;
    private DataManager dataManager;
    private String currentMonth;
    private Runnable onDataChanged;

    public BudgetAdapter(Context context, List<Budget> budgets, DataManager dataManager,
                         String currentMonth, Runnable onDataChanged) {
        this.context = context;
        this.budgets = budgets;
        this.dataManager = dataManager;
        this.currentMonth = currentMonth;
        this.onDataChanged = onDataChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        String category = budget.getCategory();

        double spent = getSpentForCategory(category);
        double limit = budget.getLimit();
        int progress = (int) Math.min((spent / limit) * 100, 100);

        holder.tvCategory.setText(Categories.getIcon(category) + " " + category);
        holder.tvSpent.setText("₹" + String.format("%.0f", spent) + " / ₹" + String.format("%.0f", limit));
        holder.progressBar.setProgress(progress);

        if (progress >= 100) {
            holder.tvStatus.setText("⚠️ Over budget!");
            holder.tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark));
        } else if (progress >= 80) {
            holder.tvStatus.setText("⚡ " + (100 - progress) + "% remaining");
            holder.tvStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark));
        } else {
            holder.tvStatus.setText("✅ " + (100 - progress) + "% remaining");
            holder.tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark));
        }

        holder.itemView.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Budget")
                .setMessage("Remove budget for " + category + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    dataManager.deleteBudget(category, currentMonth);
                    onDataChanged.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        });
    }

    private double getSpentForCategory(String category) {
        double total = 0;
        for (Expense e : dataManager.getAllExpenses()) {
            if (e.getCategory().equals(category) && e.getDate().startsWith(currentMonth)) {
                total += e.getAmount();
            }
        }
        return total;
    }

    @Override
    public int getItemCount() { return budgets.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvSpent, tvStatus;
        ProgressBar progressBar;

        ViewHolder(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_budget_category);
            tvSpent = v.findViewById(R.id.tv_budget_spent);
            tvStatus = v.findViewById(R.id.tv_budget_status);
            progressBar = v.findViewById(R.id.progress_budget);
        }
    }
}
