package com.Bllod100.zpu;

import android.content.Context;
import java.util.concurrent.atomic.AtomicBoolean;

public class UsbModemManager {
    private final AtomicBoolean busLocked = new AtomicBoolean(false);
    private int tamperAttempts = 0;

    public UsbModemManager(Context context) {}

    public boolean verifyDriver(String driverId) {
        if (driverId.contains("VULN") || driverId.contains("EXPLOIT")) {
            tamperAttempts++;
            busLocked.set(true);
            return false;
        }
        return true;
    }

    public void unlockBus() {
        busLocked.set(false);
    }

    public boolean isLocked() {
        return busLocked.get();
    }

    public int getTamperCount() {
        return tamperAttempts;
    }
}
