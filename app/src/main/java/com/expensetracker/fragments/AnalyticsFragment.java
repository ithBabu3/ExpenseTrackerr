package com.expensetracker.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.expensetracker.R;
import com.expensetracker.database.DataManager;
import com.expensetracker.models.Expense;
import com.expensetracker.utils.Categories;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class AnalyticsFragment extends Fragment {

    private DataManager dataManager;
    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvTopCategory, tvAvgDaily, tvTotalMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataManager = DataManager.getInstance(requireContext());

        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);
        tvTopCategory = view.findViewById(R.id.tv_top_category);
        tvAvgDaily = view.findViewById(R.id.tv_avg_daily);
        tvTotalMonth = view.findViewById(R.id.tv_total_month);

        loadAnalytics();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalytics();
    }

    private void loadAnalytics() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
            .format(Calendar.getInstance().getTime());

        List<Expense> expenses = dataManager.getAllExpenses();
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        double monthTotal = 0;
        int daysWithExpenses = 0;
        Set<String> days = new HashSet<>();

        for (Expense e : expenses) {
            if (e.getDate().startsWith(currentMonth)) {
                categoryTotals.merge(e.getCategory(), e.getAmount(), Double::sum);
                monthTotal += e.getAmount();
                days.add(e.getDate());
            }
        }
        daysWithExpenses = days.size();

        // Stats
        tvTotalMonth.setText("₹ " + String.format("%.2f", monthTotal));
        tvAvgDaily.setText(daysWithExpenses > 0 ?
            "₹ " + String.format("%.2f", monthTotal / daysWithExpenses) : "₹ 0");

        String topCat = categoryTotals.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        tvTopCategory.setText(Categories.getIcon(topCat) + " " + topCat);

        // Pie Chart - by category
        setupPieChart(categoryTotals);

        // Bar chart - last 7 days
        setupBarChart(expenses);
    }

    private void setupPieChart(Map<String, Double> data) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(),
                Categories.getIcon(entry.getKey())));
        }

        if (entries.isEmpty()) { pieChart.setVisibility(View.GONE); return; }
        pieChart.setVisibility(View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(14f);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private void setupBarChart(List<Expense> expenses) {
        // Last 7 days
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Map<String, Float> last7 = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis() - (long) i * 86400000L);
            last7.put(sdf.format(cal.getTime()), 0f);
        }

        for (Expense e : expenses) {
            if (last7.containsKey(e.getDate())) {
                last7.put(e.getDate(), last7.get(e.getDate()) + (float) e.getAmount());
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        int i = 0;
        for (float val : last7.values()) {
            entries.add(new BarEntry(i++, val));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Spend (Last 7 Days)");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }
}
