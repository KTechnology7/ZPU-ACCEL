package com.Bllod100.zpu.core;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import java.util.Map;

public class VirtualCoolingEngine {
    private float lastHeadroom = 0.0f;
    private float thermalSlope = 0.0f;
    private float flowRate = 0.0f;
    private final PowerManager pm;

    private static final java.util.concurrent.atomic.AtomicBoolean thermalCaringActive = new java.util.concurrent.atomic.AtomicBoolean(true);

    public VirtualCoolingEngine(Context context) {
        this.pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    public static boolean isThermalCaringActive() {
        return thermalCaringActive.get();
    }

    public static void setThermalCaringActive(boolean active) {
        thermalCaringActive.set(active);
    }

    public void updateCooling() {
        if (Build.VERSION.SDK_INT >= 30) {
            float current = pm.getThermalHeadroom(0);
            float forecast = pm.getThermalHeadroom(30);
            thermalSlope = (forecast - current) / 30.0f;
            lastHeadroom = current;
            
            if (thermalSlope > 0) {
                flowRate = 2.0f + (thermalSlope * 100);
            } else {
                flowRate = 0.5f;
            }
        }
    }

    public float getSlope() {
        return thermalSlope;
    }

    public float getFlowRate() {
        return flowRate;
    }

    public boolean isSaturationRisk() {
        if (!thermalCaringActive.get()) return false;
        return thermalSlope > 0.02f || lastHeadroom > 0.90f;
    }
}
