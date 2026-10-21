package com.Bllod100.zpu.core;

import com.Bllod100.zpu.ResourceManagerService;
import android.content.Intent;
import android.util.Log;

/**
 * ZPU SILENT RECOVERY SENTINEL
 * Captures fatal exceptions and attempts to restore the ZPU fabric state.
 */
public class SavageSentinel implements Thread.UncaughtExceptionHandler {
    private static final SavageSentinel instance = new SavageSentinel();

    public static void watch() {
        Thread.setDefaultUncaughtExceptionHandler(instance);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e("ZPU_SENTINEL", "FATAL_ERROR: " + e.getMessage());
        
        // APEX RECOVERY: Attempt to restart service silently
        ResourceManagerService s = ResourceManagerService.inst;
        if (s != null) {
            try {
                Intent i = new Intent(s, ResourceManagerService.class);
                s.startService(i);
            } catch (Exception ignored) {}
        }
        
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
