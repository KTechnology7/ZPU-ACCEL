package com.Bllod100.zpu.core;

import android.os.Build;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SavageWatchdog {
    private static final AtomicLong lastPulse = new AtomicLong(System.currentTimeMillis());
    private static final AtomicInteger healingEvents = new AtomicInteger(0);
    private static volatile boolean fabricHealthy = true;
    private static final String HARDWARE_TOKEN = "NODE-" + (Build.FINGERPRINT + Build.BOARD).hashCode();

    public static void start() {
        new Thread(() -> {
            while (true) {
                long delta = System.currentTimeMillis() - lastPulse.get();
                if (delta > 2000) {
                    performSiliconHeal();
                }
                try { Thread.sleep(500); } catch (Exception ignored) {}
            }
        }).start();
    }

    public static void pulse() {
        lastPulse.set(System.currentTimeMillis());
    }

    private static void performSiliconHeal() {
        healingEvents.incrementAndGet();
        fabricHealthy = false;
        fabricHealthy = true;
    }

    public static boolean isHealthy() { return fabricHealthy; }
    public static int getHealingCount() { return healingEvents.get(); }
    public static String getToken() { return HARDWARE_TOKEN; }
}
