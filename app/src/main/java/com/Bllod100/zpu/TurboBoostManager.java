package com.Bllod100.zpu;

import java.util.concurrent.atomic.AtomicInteger;

public class TurboBoostManager {
    private final AtomicInteger burstCount = new AtomicInteger(0);
    private float lastFrequency = 0.0f;

    public boolean checkThrottling(float currentFreq) {
        if (lastFrequency > 0 && currentFreq < lastFrequency * 0.8) {
            burstCount.incrementAndGet();
            lastFrequency = currentFreq;
            return true;
        }
        lastFrequency = currentFreq;
        return false;
    }

    public int getBurstCount() {
        return burstCount.get();
    }
}
