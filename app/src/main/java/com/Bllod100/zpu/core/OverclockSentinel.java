package com.Bllod100.zpu.core;
import java.util.concurrent.atomic.AtomicInteger;
public class OverclockSentinel {
    private final AtomicInteger voltageOffset = new AtomicInteger(0);
    private float safeCeiling = 85.0f;
    public boolean requestBoost(float currentTemp) {
        if (currentTemp < safeCeiling) {
            voltageOffset.addAndGet(5);
            return true;
        }
        voltageOffset.set(0);
        return false;
    }
    public int getOffset() {
        return voltageOffset.get();
    }
    public float getCeiling() {
        return safeCeiling;
    }
}
