# 💰 Expense Tracker — Native Android App

A full-featured expense tracker built with **Java + Material Design 3** for Android.
Data is stored on the **SD card** so it **survives app uninstall** and clear storage.

---

## ✅ Features

| Feature | Details |
|---|---|
| Add/Edit/Delete Expenses | Title, amount, category, date, note |
| 12 Categories | Food, Transport, Shopping, Health, etc. |
| Monthly Budget Limits | Per-category budgets with visual progress |
| Charts & Analytics | Pie chart (by category) + Bar chart (7-day) |
| Search & Filter | Live search + category filter |
| WiFi Sync | Multi-device sync on same local WiFi — no internet needed |
| Export to Excel | Full .xlsx export saved to SD card |
| SD Card Storage | Data at `/sdcard/ExpenseTracker/data.json` — survives uninstall ✅ |
| Multi-user | Each user sets their name; expenses show who added them |

---

## 📁 Project Structure

```
app/src/main/java/com/expensetracker/
├── activities/
│   ├── SplashActivity.java
│   ├── MainActivity.java          ← Bottom nav + FAB + sync server host
│   ├── AddEditExpenseActivity.java
│   ├── BudgetActivity.java
│   └── SyncActivity.java          ← WiFi sync UI
├── adapters/
│   ├── ExpenseAdapter.java
│   └── BudgetAdapter.java
├── database/
│   └── DataManager.java           ← All read/write to SD card JSON
├── fragments/
│   ├── DashboardFragment.java
│   ├── ExpensesFragment.java
│   ├── AnalyticsFragment.java
│   └── SettingsFragment.java
├── models/
│   ├── Expense.java
│   ├── Budget.java
│   ├── Group.java
│   └── AppData.java               ← Single object holding all data
├── network/
│   ├── SyncServer.java            ← HTTP server running on this device
│   └── SyncClient.java            ← Connects to other devices
└── utils/
    ├── Categories.java
    ├── ExcelExporter.java
    └── NetworkUtils.java
```

---

## 🔄 WiFi Sync — How It Works

Every device running the app **automatically starts a sync server on port 8765**.

To sync between devices:
1. Both devices must be on the **same WiFi network**
2. Open **Settings → WiFi Sync** (or Dashboard → Sync)
3. Tap **Discover Devices** — it scans the local network
4. Select a device from the list (or type IP manually)
5. Tap **Sync Now**

Data is **merged intelligently** — newer entries win, no duplicates.

---

## 💾 Data Storage

Data is saved at:
```
/sdcard/ExpenseTracker/data.json
/sdcard/ExpenseTracker/data_backup.json  (auto backup)
/sdcard/ExpenseTracker/ExpenseTracker_YYYY-MM.xlsx  (Excel exports)
```

This folder is **outside the app's private storage**, so:
- ✅ Survives app uninstall
- ✅ Survives "Clear App Data"  
- ✅ Accessible from file manager
- ✅ Can be copied/backed up manually

---

## 🛠️ How to Build

### Requirements
- Android Studio Hedgehog or newer
- Android SDK 26+
- JDK 8+

### Steps
1. Open Android Studio
2. **File → Open** → Select the `ExpenseTracker` folder
3. Wait for Gradle sync to complete
4. Connect Android device or start emulator
5. Click **▶ Run**

### First Run
The app will request **storage permission** on Android 10 and below.
On **Android 11+**, it will open the system settings to grant
"All Files Access" permission — this is required for SD card storage.

---

## 📦 Dependencies

```gradle
com.github.PhilJay:MPAndroidChart:v3.1.0   // Charts
org.apache.poi:poi:5.2.3                    // Excel export
com.google.code.gson:gson:2.10.1            // JSON storage
com.google.android.material:material:1.11.0 // Material Design 3
```

---

## 🧑‍🤝‍🧑 Multi-User Usage

1. Each person installs the app and sets their name in **Settings**
2. All devices must be on the **same WiFi** (home router, hotspot, etc.)
3. One person adds expenses → goes to **Sync** → discovers others → syncs
4. Changes from all users are merged — everyone sees the same data

**No cloud, no accounts, no internet required.** Pure local network sync.

---

## 📱 Minimum Requirements

- Android 8.0 (API 26) or higher
- WiFi for multi-device sync
- ~10MB storage

---

*Built with ❤️ — Java + Material Design 3 + Local-first architecture*
