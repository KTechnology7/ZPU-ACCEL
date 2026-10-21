package com.Bllod100.zpu.core;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ZpuHistoryLogger {
    private static final String LOG_FILE_NAME = "ZpuAuditLog.txt";
    private static volatile boolean loggingActive = false;

    public static boolean isLoggingActive() {
        return loggingActive;
    }

    public static void setLoggingActive(boolean active) {
        loggingActive = active;
        if (active) {
            logEvent("LOG_SYSTEM", "[ACTIVE]: Connection established. Live log recording and streaming started.");
        } else {
            logEvent("LOG_SYSTEM", "[STANDBY]: Awaiting connection to start live logs.");
        }
    }

    public static synchronized void logEvent(String category, String details) {
        if (!loggingActive && !category.contains("CONNECT") && !category.contains("APP_STARTUP") && !category.contains("LOG_SYSTEM")) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
        String logEntry = String.format(Locale.US, "[%s] [%s] %s%n", timestamp, category, details);
        Log.d("ZPU_AUDIT", logEntry.trim());

        if (category.contains("FIREWALL") || category.contains("SECURITY") || category.contains("CONNECT") || category.contains("DISCONNECT") || category.contains("LOG_SYSTEM")) {
            SmartAiFirewallManager.logFirewall("[" + category + "] " + details);
        } else {
            SmartAiFirewallManager.logOptimizer("[" + category + "] " + details);
        }
    }

    public static synchronized void logToFile(Context context, String category, String details) {
        logEvent(category, details);
        try {
            File file = new File(context.getExternalFilesDir(null), LOG_FILE_NAME);
            try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
                out.printf(Locale.US, "[%s] [%s] %s%n", timestamp, category, details);
            }
        } catch (Exception e) {
            try {
                File file = new File(context.getFilesDir(), LOG_FILE_NAME);
                try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
                    out.printf(Locale.US, "[%s] [%s] %s%n", timestamp, category, details);
                }
            } catch (Exception ignored) {}
        }
    }

    public static File getLogFile(Context context) {
        File file = new File(context.getExternalFilesDir(null), LOG_FILE_NAME);
        if (!file.exists()) {
            file = new File(context.getFilesDir(), LOG_FILE_NAME);
        }
        return file;
    }

    public static void exportLogFile(Context context) {
        try {
            File src = getLogFile(context);
            if (src.exists()) {
                android.widget.Toast.makeText(context, "Audit Log saved to: " + src.getAbsolutePath(), android.widget.Toast.LENGTH_LONG).show();
            }
        } catch (Exception ignored) {}
    }
}
