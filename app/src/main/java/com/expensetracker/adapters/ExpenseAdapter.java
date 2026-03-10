package com.expensetracker.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.activities.AddEditExpenseActivity;
import com.expensetracker.database.DataManager;
import com.expensetracker.models.Expense;
import com.expensetracker.utils.Categories;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private Context context;
    private List<Expense> expenses;
    private DataManager dataManager;
    private Runnable onDataChanged;

    public ExpenseAdapter(Context context, List<Expense> expenses,
                          DataManager dataManager, Runnable onDataChanged) {
        this.context = context;
        this.expenses = expenses;
        this.dataManager = dataManager;
        this.onDataChanged = onDataChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense e = expenses.get(position);
        holder.tvTitle.setText(e.getTitle());
        holder.tvAmount.setText("₹ " + String.format("%.2f", e.getAmount()));
        holder.tvCategory.setText(Categories.getIcon(e.getCategory()) + " " + e.getCategory());
        holder.tvDate.setText(e.getDate());
        holder.tvAddedBy.setText("by " + (e.getAddedBy() != null ? e.getAddedBy() : "You"));

        holder.card.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditExpenseActivity.class);
            intent.putExtra("expense", e);
            context.startActivity(intent);
        });

        holder.card.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Expense")
                .setMessage("Delete \"" + e.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    dataManager.deleteExpense(e.getId());
                    onDataChanged.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        });
    }

    @Override
    public int getItemCount() { return expenses.size(); }

    public void updateData(List<Expense> newData) {
        this.expenses = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvTitle, tvAmount, tvCategory, tvDate, tvAddedBy;

        ViewHolder(View v) {
            super(v);
            card = v.findViewById(R.id.card_expense);
            tvTitle = v.findViewById(R.id.tv_expense_title);
            tvAmount = v.findViewById(R.id.tv_expense_amount);
            tvCategory = v.findViewById(R.id.tv_expense_category);
            tvDate = v.findViewById(R.id.tv_expense_date);
            tvAddedBy = v.findViewById(R.id.tv_added_by);
        }
    }
}
