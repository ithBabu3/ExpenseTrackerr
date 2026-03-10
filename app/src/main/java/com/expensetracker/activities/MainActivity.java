package com.expensetracker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.expensetracker.R;
import com.expensetracker.database.DataManager;
import com.expensetracker.fragments.DashboardFragment;
import com.expensetracker.fragments.ExpensesFragment;
import com.expensetracker.fragments.AnalyticsFragment;
import com.expensetracker.fragments.SettingsFragment;
import com.expensetracker.network.SyncServer;
import com.expensetracker.utils.NetworkUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private DataManager dataManager;
    public static SyncServer syncServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestStoragePermission();

        dataManager = DataManager.getInstance(this);

        // Start sync server (this device can host)
        startSyncServer();

        // Bottom navigation
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) fragment = new DashboardFragment();
            else if (id == R.id.nav_expenses) fragment = new ExpensesFragment();
            else if (id == R.id.nav_analytics) fragment = new AnalyticsFragment();
            else if (id == R.id.nav_settings) fragment = new SettingsFragment();

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
                return true;
            }
            return false;
        });

        // Default fragment
        nav.setSelectedItemId(R.id.nav_dashboard);

        // FAB - Add expense
        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
            startActivity(new Intent(this, AddEditExpenseActivity.class)));
    }

    private void startSyncServer() {
        String myIp = NetworkUtils.getLocalIpAddress(this);
        syncServer = new SyncServer(
            dataManager.getAppData(),
            new SyncServer.SyncServerCallback() {
                @Override
                public void onClientConnected(String clientIp) {
                    runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                            "Device connected: " + clientIp, Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onDataReceived(com.expensetracker.models.AppData data, String fromIp) {
                    dataManager.mergeData(data);
                    syncServer.updateLocalData(dataManager.getAppData());
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                            "Synced with " + fromIp, Toast.LENGTH_SHORT).show();
                        // Refresh current fragment
                        Fragment current = getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container);
                        if (current != null) {
                            getSupportFragmentManager().beginTransaction()
                                .detach(current).attach(current).commit();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e("SyncServer", "Error: " + error);
                }
            });
        syncServer.start();
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(
                    android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                 Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (syncServer != null) syncServer.stop();
    }
}
