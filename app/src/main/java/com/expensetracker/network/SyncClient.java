package com.expensetracker.network;

import android.util.Log;

import com.expensetracker.models.AppData;
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncClient {
    private static final String TAG = "SyncClient";
    private Gson gson = new Gson();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface SyncCallback {
        void onSuccess(AppData mergedData);
        void onError(String error);
    }

    /**
     * Ping a device to check if it's hosting
     */
    public void ping(String hostIp, PingCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL("http://" + hostIp + ":" + SyncServer.PORT + "/ping");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                int code = conn.getResponseCode();
                callback.onResult(code == 200, hostIp);
            } catch (Exception e) {
                callback.onResult(false, hostIp);
            }
        });
    }

    /**
     * Pull data from host
     */
    public void pullData(String hostIp, SyncCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL("http://" + hostIp + ":" + SyncServer.PORT + "/data");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                AppData data = gson.fromJson(sb.toString(), AppData.class);
                callback.onSuccess(data);
            } catch (Exception e) {
                Log.e(TAG, "Pull error", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Push local data to host and receive merged result
     */
    public void syncWithHost(String hostIp, AppData localData, SyncCallback callback) {
        executor.execute(() -> {
            try {
                String jsonBody = gson.toJson(localData);
                byte[] bodyBytes = jsonBody.getBytes("UTF-8");

                URL url = new URL("http://" + hostIp + ":" + SyncServer.PORT + "/sync");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);

                conn.getOutputStream().write(bodyBytes);

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                AppData merged = gson.fromJson(sb.toString(), AppData.class);
                callback.onSuccess(merged);
            } catch (Exception e) {
                Log.e(TAG, "Sync error", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Auto-discover devices on LAN by scanning common IPs
     */
    public void discoverDevices(String subnet, DiscoveryCallback callback) {
        // e.g. subnet = "192.168.1"
        ExecutorService scanner = Executors.newFixedThreadPool(20);
        final int[] found = {0};
        final int total = 254;
        final int[] checked = {0};

        for (int i = 1; i <= total; i++) {
            final String ip = subnet + "." + i;
            scanner.execute(() -> {
                try {
                    URL url = new URL("http://" + ip + ":" + SyncServer.PORT + "/ping");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(500);
                    conn.setReadTimeout(500);
                    if (conn.getResponseCode() == 200) {
                        callback.onDeviceFound(ip);
                        found[0]++;
                    }
                } catch (Exception ignored) {}
                synchronized (checked) {
                    checked[0]++;
                    if (checked[0] == total) {
                        callback.onDiscoveryComplete(found[0]);
                        scanner.shutdown();
                    }
                }
            });
        }
    }

    public interface PingCallback {
        void onResult(boolean reachable, String ip);
    }

    public interface DiscoveryCallback {
        void onDeviceFound(String ip);
        void onDiscoveryComplete(int totalFound);
    }
}
