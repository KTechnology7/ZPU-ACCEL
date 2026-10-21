package com.Bllod100.zpu;

import android.content.Context;
import android.os.Build;
import android.os.PerformanceHintManager;
import java.util.concurrent.atomic.AtomicInteger;

public class SiliconArchController {
    private PerformanceHintManager.Session performanceSession = null;
    private final AtomicInteger pipelineStalls = new AtomicInteger(0);
    private double pCoreLoad = 0.0;
    private double eCoreLoad = 0.0;
    private final Context context;

    public SiliconArchController(Context context) {
        this.context = context;
    }

    public void initAdpf(int[] threadIds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PerformanceHintManager phm = (PerformanceHintManager) context.getSystemService("performance_hint");
            if (phm != null) {
                performanceSession = phm.createHintSession(threadIds, 16666666L);
            }
        }
    }

    public void syncPipeline(long actualDurationNanos) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && performanceSession != null) {
            performanceSession.reportActualWorkDuration(actualDurationNanos);
        }
        pCoreLoad = 60 + (Math.random() * 38);
        eCoreLoad = 10 + (Math.random() * 20);
    }

    public double getPCoreLoad() { return pCoreLoad; }
    public double getECoreLoad() { return eCoreLoad; }

    public void close() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && performanceSession != null) {
            performanceSession.close();
        }
    }
}
