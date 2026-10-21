package com.Bllod100.zpu;

import android.content.Context;
import java.util.concurrent.atomic.AtomicInteger;

public class NextGenComputeEngine {
    private final AtomicInteger alerts = new AtomicInteger(0);
    private String status = "SHIELDED";

    public NextGenComputeEngine(Context context) {}

    public void analyzeProgramIntent(String action) {
        if (action.contains("hollow") || action.contains("inject")) {
            alerts.incrementAndGet();
            status = "VACCINATING";
        }
    }

    public String getStatus() {
        return status;
    }

    public int getAlertCount() {
        return alerts.get();
    }
}
