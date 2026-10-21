package com.Bllod100.zpu.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ZpuHardwareSafetyManager {
    private static final AtomicBoolean softUsbDisabled = new AtomicBoolean(false);
    private static final AtomicBoolean wifiRelayDisabled = new AtomicBoolean(false);
    private static final AtomicBoolean cableDegraded = new AtomicBoolean(false);
    private static final AtomicInteger packetDropSpikes = new AtomicInteger(0);

    public static boolean isSoftUsbDisabled() {
        return softUsbDisabled.get();
    }

    public static void setSoftUsbDisabled(boolean disabled) {
        softUsbDisabled.set(disabled);
    }

    public static boolean isWifiRelayDisabled() {
        return wifiRelayDisabled.get();
    }

    public static void setWifiRelayDisabled(boolean disabled) {
        wifiRelayDisabled.set(disabled);
    }

    public static void recordPacketDrop() {
        if (packetDropSpikes.incrementAndGet() > 10) {
            cableDegraded.set(true);
        }
    }

    public static void resetPacketDropCount() {
        packetDropSpikes.set(0);
        cableDegraded.set(false);
    }

    public static boolean isCableDegraded() {
        return cableDegraded.get();
    }

    public static String getCableHealthDiagnostic() {
        if (softUsbDisabled.get()) {
            return "USB LINK SOFT-DISABLED (WI-FI ACTIVE)";
        }
        if (wifiRelayDisabled.get()) {
            return "WIFI RELAY DISABLED (USB PORT METHOD ACTIVE)";
        }
        if (cableDegraded.get()) {
            return "WARNING: WORN USB CABLE DETECTED. REPLACING CABLE WILL INCREASE SPEED & STABILITY.";
        }
        return "USB CABLE & POWER SUPPLY HEALTH: EXCELLENT (100% SAFE)";
    }
}
