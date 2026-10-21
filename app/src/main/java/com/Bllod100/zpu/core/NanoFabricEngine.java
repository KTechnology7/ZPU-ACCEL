package com.Bllod100.zpu.core;

import java.util.concurrent.atomic.AtomicLong;

public class NanoFabricEngine {
    private final AtomicLong packetCounter = new AtomicLong(0);
    private float molecularFlowRate = 1000000.0f;

    public void processNano() {
        packetCounter.addAndGet(10000);
    }

    public long getPacketCount() {
        return packetCounter.get();
    }

    public float getFlowRate() {
        return molecularFlowRate;
    }
}
