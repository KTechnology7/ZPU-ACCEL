package com.Bllod100.zpu.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrackedGameGuard {
    private final Set<String> isolatedPids = new CopyOnWriteArraySet<>();
    private final Set<String> suspiciousBehaviors = new CopyOnWriteArraySet<>();
    private final AtomicBoolean shieldActive = new AtomicBoolean(true);
    private volatile long lastHeuristicCheck = System.currentTimeMillis();

    public boolean isUnsignedBinary(String procName) {
        if (procName == null || procName.isEmpty()) return false;
        String lower = procName.toLowerCase();
        return lower.contains("_crk") || lower.contains("reloaded") || 
               lower.contains("emu") || lower.contains("codex") ||
               lower.contains("skidrow") || lower.contains("fitgirl") ||
               lower.contains("cheat") || lower.contains("injector") ||
               lower.contains("hook") || lower.contains("tamper");
    }

    public boolean monitorBehavior(String processName, long memoryUsage, int threadCount, double cpuPercent) {
        if (!shieldActive.get()) return true;
        if (processName == null) return true;
        if (cpuPercent > 92.0 || threadCount > 250 || memoryUsage > 4L * 1024 * 1024 * 1024) {
            suspiciousBehaviors.add(processName);
            isolateComponent(processName);
            return false;
        }
        return true;
    }

    public void isolateComponent(String pid) {
        if (pid == null || pid.isEmpty()) return;
        isolatedPids.add(pid);
        long now = System.currentTimeMillis();
        if (now - lastHeuristicCheck > 3000) {
            lastHeuristicCheck = now;
        }
    }

    public int getIsolatedCount() {
        return isolatedPids.size() + suspiciousBehaviors.size();
    }

    public boolean isShieldActive() {
        return shieldActive.get();
    }

    public void setShieldActive(boolean active) {
        this.shieldActive.set(active);
    }
}
