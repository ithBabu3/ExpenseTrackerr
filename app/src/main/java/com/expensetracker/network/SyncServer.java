package com.expensetracker.network;

import android.util.Log;

import com.expensetracker.models.AppData;
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple HTTP server that runs on the device's local WiFi IP.
 * Other devices on the same network can connect to sync data.
 * Port: 8765
 */
public class SyncServer {
    private static final String TAG = "SyncServer";
    public static final int PORT = 8765;

    private ServerSocket serverSocket;
    private ExecutorService executor;
    private boolean running = false;
    private Gson gson = new Gson();
    private SyncServerCallback callback;
    private AppData localData;

    public interface SyncServerCallback {
        void onClientConnected(String clientIp);
        void onDataReceived(AppData data, String fromIp);
        void onError(String error);
    }

    public SyncServer(AppData localData, SyncServerCallback callback) {
        this.localData = localData;
        this.callback = callback;
        this.executor = Executors.newCachedThreadPool();
    }

    public void updateLocalData(AppData data) {
        this.localData = data;
    }

    public void start() {
        executor.execute(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                Log.d(TAG, "Server started on port " + PORT);

                while (running) {
                    Socket client = serverSocket.accept();
                    executor.execute(() -> handleClient(client));
                }
            } catch (IOException e) {
                if (running) {
                    Log.e(TAG, "Server error", e);
                    if (callback != null) callback.onError(e.getMessage());
                }
            }
        });
    }

    private void handleClient(Socket client) {
        String clientIp = client.getInetAddress().getHostAddress();
        if (callback != null) callback.onClientConnected(clientIp);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream(), true);

            // Read HTTP request
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            int contentLength = 0;

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\n");
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            String request = requestBuilder.toString();

            if (request.startsWith("GET /data")) {
                // Client is pulling data from us
                String json = gson.toJson(localData);
                sendHttpResponse(writer, "200 OK", "application/json", json);

            } else if (request.startsWith("POST /sync")) {
                // Client is pushing data to us
                char[] body = new char[contentLength];
                reader.read(body, 0, contentLength);
                String bodyStr = new String(body);

                AppData incoming = gson.fromJson(bodyStr, AppData.class);
                if (callback != null) callback.onDataReceived(incoming, clientIp);

                // Send back merged data
                String json = gson.toJson(localData);
                sendHttpResponse(writer, "200 OK", "application/json", json);

            } else if (request.startsWith("GET /ping")) {
                sendHttpResponse(writer, "200 OK", "text/plain", "pong");
            }

            client.close();
        } catch (IOException e) {
            Log.e(TAG, "Client handler error", e);
        }
    }

    private void sendHttpResponse(PrintWriter writer, String status, String contentType, String body) {
        writer.println("HTTP/1.1 " + status);
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + body.getBytes().length);
        writer.println("Access-Control-Allow-Origin: *");
        writer.println();
        writer.print(body);
        writer.flush();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error stopping server", e);
        }
        executor.shutdownNow();
    }

    public boolean isRunning() {
        return running;
    }
}
