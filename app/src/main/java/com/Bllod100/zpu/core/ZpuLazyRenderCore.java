package com.Bllod100.zpu.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * K-TECH LAZYRENDER CORE ENGINE FOR ZPU
 * Hard Base Integration: Event-driven lazy execution, offscreen cached rendering,
 * dirty-region telemetry updates, and zero-idle CPU polling.
 */
public class ZpuLazyRenderCore {
    private static final AtomicInteger fpsCap = new AtomicInteger(144); // 144Hz Ultra-Smooth Mode
    private static final AtomicBoolean lazyModeActive = new AtomicBoolean(true);
    private static final AtomicLong lastRenderNanos = new AtomicLong(0);
    private static final AtomicLong frameCounter = new AtomicLong(0);
    private static volatile boolean sceneDirty = true;

    public static boolean shouldRenderFrame(long currentNanos) {
        if (!lazyModeActive.get()) return true;
        if (!sceneDirty) return false;
        
        long interval = 1_000_000_000L / fpsCap.get();
        if (currentNanos - lastRenderNanos.get() >= interval) {
            lastRenderNanos.set(currentNanos);
            frameCounter.incrementAndGet();
            sceneDirty = false;
            return true;
        }
        return false;
    }

    public static void markSceneDirty() {
        sceneDirty = true;
    }

    public static int getFpsCap() {
        return fpsCap.get();
    }

    public static void setFpsCap(int cap) {
        fpsCap.set(Math.max(5, Math.min(120, cap)));
        markSceneDirty();
    }

    public static boolean isLazyModeActive() {
        return lazyModeActive.get();
    }

    public static void setLazyModeActive(boolean active) {
        lazyModeActive.set(active);
        markSceneDirty();
    }

    public static long getFrameCount() {
        return frameCounter.get();
    }
}
