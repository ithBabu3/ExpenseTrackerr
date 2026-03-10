package com.expensetracker.database;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.expensetracker.models.AppData;
import com.expensetracker.models.Budget;
import com.expensetracker.models.Expense;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String TAG = "DataManager";
    private static final String FOLDER_NAME = "ExpenseTracker";
    private static final String DATA_FILE = "data.json";
    private static final String BACKUP_FILE = "data_backup.json";

    private static DataManager instance;
    private AppData appData;
    private Gson gson;
    private File dataFile;

    private DataManager(Context context) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        initStorage();
        loadData();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
        }
        return instance;
    }

    private void initStorage() {
        // Use external SD card / public storage so data persists after uninstall
        File sdCard = Environment.getExternalStorageDirectory();
        File folder = new File(sdCard, FOLDER_NAME);
        if (!folder.exists()) folder.mkdirs();
        dataFile = new File(folder, DATA_FILE);
        Log.d(TAG, "Storage path: " + dataFile.getAbsolutePath());
    }

    public void loadData() {
        if (!dataFile.exists()) {
            appData = new AppData();
            saveData();
            return;
        }
        try (FileReader reader = new FileReader(dataFile)) {
            appData = gson.fromJson(reader, AppData.class);
            if (appData == null) appData = new AppData();
            if (appData.getExpenses() == null) appData.setExpenses(new ArrayList<>());
            if (appData.getBudgets() == null) appData.setBudgets(new ArrayList<>());
        } catch (Exception e) {
            Log.e(TAG, "Failed to load data", e);
            appData = new AppData();
        }
    }

    public boolean saveData() {
        try {
            // Write to backup first
            File backupFile = new File(dataFile.getParent(), BACKUP_FILE);
            if (dataFile.exists()) {
                dataFile.renameTo(backupFile);
            }
            appData.touch();
            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(appData, writer);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to save data", e);
            return false;
        }
    }

    // ---- Expense CRUD ----

    public List<Expense> getAllExpenses() {
        return appData.getExpenses();
    }

    public void addExpense(Expense expense) {
        appData.getExpenses().add(expense);
        saveData();
    }

    public void updateExpense(Expense updated) {
        List<Expense> list = appData.getExpenses();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updated.getId())) {
                list.set(i, updated);
                break;
            }
        }
        saveData();
    }

    public void deleteExpense(String expenseId) {
        appData.getExpenses().removeIf(e -> e.getId().equals(expenseId));
        saveData();
    }

    // ---- Budget CRUD ----

    public List<Budget> getAllBudgets() {
        return appData.getBudgets();
    }

    public void setBudget(Budget budget) {
        List<Budget> list = appData.getBudgets();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getCategory().equals(budget.getCategory())
                    && list.get(i).getMonth().equals(budget.getMonth())) {
                list.set(i, budget);
                saveData();
                return;
            }
        }
        list.add(budget);
        saveData();
    }

    public Budget getBudgetForCategory(String category, String month) {
        for (Budget b : appData.getBudgets()) {
            if (b.getCategory().equals(category) && b.getMonth().equals(month)) {
                return b;
            }
        }
        return null;
    }

    public void deleteBudget(String category, String month) {
        appData.getBudgets().removeIf(b ->
            b.getCategory().equals(category) && b.getMonth().equals(month));
        saveData();
    }

    // ---- Group ----

    public com.expensetracker.models.Group getGroup() {
        return appData.getGroup();
    }

    public void saveGroup(com.expensetracker.models.Group group) {
        appData.setGroup(group);
        saveData();
    }

    // ---- Sync support ----

    public AppData getAppData() { return appData; }

    public void mergeData(AppData incoming) {
        // Merge incoming expenses (add only new ones by ID)
        List<Expense> current = appData.getExpenses();
        List<String> currentIds = new ArrayList<>();
        for (Expense e : current) currentIds.add(e.getId());

        for (Expense e : incoming.getExpenses()) {
            if (!currentIds.contains(e.getId())) {
                current.add(e);
            } else {
                // update if incoming is newer
                for (int i = 0; i < current.size(); i++) {
                    if (current.get(i).getId().equals(e.getId())
                            && e.getTimestamp() > current.get(i).getTimestamp()) {
                        current.set(i, e);
                    }
                }
            }
        }

        // Merge budgets - incoming wins if newer
        for (Budget ib : incoming.getBudgets()) {
            boolean found = false;
            List<Budget> budgets = appData.getBudgets();
            for (int i = 0; i < budgets.size(); i++) {
                if (budgets.get(i).getCategory().equals(ib.getCategory())
                        && budgets.get(i).getMonth().equals(ib.getMonth())) {
                    budgets.set(i, ib);
                    found = true;
                    break;
                }
            }
            if (!found) budgets.add(ib);
        }

        saveData();
    }

    // ---- Export path ----
    public String getDataFilePath() {
        return dataFile.getAbsolutePath();
    }

    public File getExportFolder() {
        return dataFile.getParentFile();
    }
}
