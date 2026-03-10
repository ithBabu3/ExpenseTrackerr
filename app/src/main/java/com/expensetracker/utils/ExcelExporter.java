package com.expensetracker.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.expensetracker.models.Expense;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Lightweight CSV exporter — no external libraries needed.
 * Opens in Excel, Google Sheets, or any spreadsheet app.
 */
public class ExcelExporter {
    private static final String TAG = "ExcelExporter";

    public static String export(Context context, List<Expense> expenses, String month) {
        try {
            File folder = new File(Environment.getExternalStorageDirectory(), "ExpenseTracker");
            if (!folder.exists()) folder.mkdirs();

            String fileName = "ExpenseTracker_" + month + ".csv";
            File file = new File(folder, fileName);

            FileWriter fw = new FileWriter(file);

            // Header
            fw.write("#,Title,Category,Amount,Date,Added By,Note\n");

            // Data rows
            double total = 0;
            for (int i = 0; i < expenses.size(); i++) {
                Expense e = expenses.get(i);
                fw.write(String.format("%d,%s,%s,%.2f,%s,%s,%s\n",
                    i + 1,
                    escapeCsv(e.getTitle()),
                    escapeCsv(e.getCategory()),
                    e.getAmount(),
                    e.getDate() != null ? e.getDate() : "",
                    escapeCsv(e.getAddedBy() != null ? e.getAddedBy() : ""),
                    escapeCsv(e.getNote() != null ? e.getNote() : "")
                ));
                total += e.getAmount();
            }

            // Total
            fw.write(String.format(",,TOTAL,%.2f,,,\n", total));
            fw.close();

            return file.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Export failed", e);
            return null;
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
