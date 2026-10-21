package com.Bllod100.zpu.core;

import java.util.concurrent.atomic.AtomicReference;

public class SmartBondAI {
    public enum Mode { USB, WIFI, HYBRID, AUTO }
    private final AtomicReference<Mode> currentMode = new AtomicReference<>(Mode.AUTO);
    private float usbStability = 1.0f;
    private float wifiStability = 1.0f;

    public void train(float latency, float jitter, boolean usbFound, boolean wifiFound) {
        if (usbFound && wifiFound) {
            if (latency < 10) currentMode.set(Mode.USB);
            else currentMode.set(Mode.HYBRID);
        } else if (usbFound) {
            currentMode.set(Mode.USB);
        } else if (wifiFound) {
            currentMode.set(Mode.WIFI);
        }
    }

    public Mode getActiveMode() {
        return currentMode.get();
    }

    public String getStatusString() {
        return "BOND:" + currentMode.get().name() + " [AI-ACTIVE]";
    }
}
