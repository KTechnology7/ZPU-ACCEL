package com.Bllod100.zpu.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ZpuFiber {
    private static final ExecutorService pool = Executors.newFixedThreadPool(
        HardwareProbe.getCoreCount(),
        new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("ZPU-RT-KERNEL");
                t.setPriority(10);
                t.setDaemon(true);
                return t;
            }
        }
    );
    
    private static final AtomicInteger activeFibers = new AtomicInteger(0);

    public static void launch(Runnable block) {
        if (!SavageWatchdog.isHealthy()) return;
        activeFibers.incrementAndGet();
        pool.execute(() -> {
            try {
                block.run();
            } catch (Throwable t) {
                // SAVAGE-SHIELD: Prevent background failures from killing the app
                t.printStackTrace();
            } finally {
                activeFibers.decrementAndGet();
            }
        });
    }

    public static int getActiveCount() {
        return activeFibers.get();
    }
}
