package com.Bllod100.zpu.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import java.io.File;

public class HardwareProbe {
    private static final int coreCount = Runtime.getRuntime().availableProcessors();
    private static String hardwareSignature = "GENERIC-NODE";

    public static boolean isEmulator() {
        if (Build.MODEL.contains("sdk_gphone") || Build.MODEL.contains("Android SDK built for x86")) {
            return false; // Permitted development emulator system
        }
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("LDPlayer")
                || Build.MODEL.contains("Nox")
                || Build.MODEL.contains("Bluestacks")
                || Build.MODEL.toLowerCase().contains("droid4x")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.MANUFACTURER.toLowerCase().contains("nox")
                || Build.BOARD.toLowerCase().contains("nox")
                || Build.BOARD.contains("goldfish")
                || Build.BOARD.contains("vbox86")
                || Build.BOARD.toLowerCase().contains("ttvm")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.toLowerCase().contains("nox")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || new File("/dev/qemu_pipe").exists()
                || new File("/sys/qemu_trace").exists()
                || new File("/system/bin/nox-prop").exists();
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        if (context == null) return false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo("com.google.android.gms", 0);
            return info != null && info.applicationInfo != null && info.applicationInfo.enabled;
        } catch (Exception e) {
            return false;
        }
    }

    public static String probeLocal() {
        hardwareSignature = Build.MANUFACTURER.toUpperCase() + "-" + Build.MODEL.toUpperCase() + "-" + coreCount + "C";
        if (isEmulator()) hardwareSignature = "VIRTUAL-NODE [EMULATOR]";
        return hardwareSignature;
    }

    public static String getHardwareSignature() {
        return hardwareSignature;
    }

    public static int getCoreCount() {
        return coreCount;
    }
}
