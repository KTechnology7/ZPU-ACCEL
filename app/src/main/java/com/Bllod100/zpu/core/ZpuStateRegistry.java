package com.Bllod100.zpu.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ZPU CENTRALIZED STATE & TELEMETRY REGISTRY
 * Unified source of truth for inter-file data sharing and high-integrity module synchronization.
 */
public class ZpuStateRegistry {
    private static final AtomicInteger cpuLoad = new AtomicInteger(0);
    private static final AtomicInteger gpuLoad = new AtomicInteger(0);
    private static final AtomicInteger ramUsage = new AtomicInteger(0);
    private static final AtomicLong sharedMem = new AtomicLong(0);
    private static final AtomicInteger laneCount = new AtomicInteger(1000);
    private static final AtomicBoolean firewallActive = new AtomicBoolean(true);
    private static final AtomicInteger vacuumCount = new AtomicInteger(0);
    private static final AtomicBoolean pcLowPowerMode = new AtomicBoolean(false);

    public static void update(int cpu, int gpu, int ram, long shared, int lanes, boolean firewall, int vacuum) {
        cpuLoad.set(cpu);
        gpuLoad.set(gpu);
        ramUsage.set(ram);
        sharedMem.set(shared);
        laneCount.set(lanes);
        firewallActive.set(firewall);
        vacuumCount.set(vacuum);
    }

    public static int getCpuLoad() { return cpuLoad.get(); }
    public static int getGpuLoad() { return gpuLoad.get(); }
    public static int getRamUsage() { return ramUsage.get(); }
    public static long getSharedMem() { return sharedMem.get(); }
    public static int getLaneCount() { return laneCount.get(); }
    public static boolean isFirewallActive() { return firewallActive.get(); }
    public static int getVacuumCount() { return vacuumCount.get(); }
    public static boolean isPcLowPowerMode() { return pcLowPowerMode.get(); }
    public static void setPcLowPowerMode(boolean active) { pcLowPowerMode.set(active); }
}
