package com.expensetracker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.expensetracker.R;
import com.expensetracker.database.DataManager;
import com.expensetracker.network.SyncClient;
import com.expensetracker.utils.NetworkUtils;

public class SyncActivity extends AppCompatActivity {

    private TextView tvMyIp, tvStatus;
    private EditText etTargetIp;
    private Button btnSync, btnDiscover;
    private ListView lvDevices;
    private ProgressBar progressBar;
    private DataManager dataManager;
    private SyncClient syncClient;
    private ArrayAdapter<String> devicesAdapter;
    private java.util.List<String> discoveredDevices = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        dataManager = DataManager.getInstance(this);
        syncClient = new SyncClient();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("WiFi Sync");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvMyIp = findViewById(R.id.tv_my_ip);
        tvStatus = findViewById(R.id.tv_status);
        etTargetIp = findViewById(R.id.et_target_ip);
        btnSync = findViewById(R.id.btn_sync);
        btnDiscover = findViewById(R.id.btn_discover);
        lvDevices = findViewById(R.id.lv_devices);
        progressBar = findViewById(R.id.progress_bar);

        String myIp = NetworkUtils.getLocalIpAddress(this);
        tvMyIp.setText("📱 My IP: " + myIp + " (Port: 8765)");
        tvStatus.setText("✅ Hosting sync server — other devices can connect to you");

        devicesAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, discoveredDevices);
        lvDevices.setAdapter(devicesAdapter);

        btnDiscover.setOnClickListener(v -> discoverDevices(myIp));

        btnSync.setOnClickListener(v -> {
            String ip = etTargetIp.getText().toString().trim();
            if (ip.isEmpty()) {
                Toast.makeText(this, "Enter target IP", Toast.LENGTH_SHORT).show();
                return;
            }
            syncWithDevice(ip);
        });

        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            String ip = discoveredDevices.get(position);
            etTargetIp.setText(ip);
        });
    }

    private void discoverDevices(String myIp) {
        String subnet = NetworkUtils.getSubnet(myIp);
        discoveredDevices.clear();
        devicesAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("🔍 Scanning " + subnet + ".0/24 ...");

        syncClient.discoverDevices(subnet, new SyncClient.DiscoveryCallback() {
            @Override
            public void onDeviceFound(String ip) {
                if (!ip.equals(myIp)) {
                    runOnUiThread(() -> {
                        discoveredDevices.add(ip);
                        devicesAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onDiscoveryComplete(int total) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (discoveredDevices.isEmpty()) {
                        tvStatus.setText("❌ No devices found. Make sure others are on same WiFi.");
                    } else {
                        tvStatus.setText("✅ Found " + discoveredDevices.size() + " device(s). Tap to select.");
                    }
                });
            }
        });
    }

    private void syncWithDevice(String ip) {
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("🔄 Syncing with " + ip + "...");
        btnSync.setEnabled(false);

        syncClient.syncWithHost(ip, dataManager.getAppData(), new SyncClient.SyncCallback() {
            @Override
            public void onSuccess(com.expensetracker.models.AppData mergedData) {
                dataManager.mergeData(mergedData);
                if (MainActivity.syncServer != null)
                    MainActivity.syncServer.updateLocalData(dataManager.getAppData());

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSync.setEnabled(true);
                    tvStatus.setText("✅ Sync complete! " +
                        mergedData.getExpenses().size() + " expenses synced.");
                    Toast.makeText(SyncActivity.this, "Sync successful!", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSync.setEnabled(true);
                    tvStatus.setText("❌ Sync failed: " + error);
                    Toast.makeText(SyncActivity.this,
                        "Sync failed. Is the device on same WiFi?", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
