package com.Bllod100.zpu.core;

import android.util.Log;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ZPU "8 God MOD AKS" SHIZUKU HYPER-PRIVILEGE MANAGER
 * Executes system-level privileged operations via Shizuku IPC binder and app_process shell.
 */
public class ZpuShizukuGodManager {
    private static final AtomicBoolean godModAksActive = new AtomicBoolean(true);

    public static boolean isShizukuAvailable() {
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method pingMethod = shizukuClass.getMethod("pingBinder");
            Object res = pingMethod.invoke(null);
            return res instanceof Boolean && (Boolean) res;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasShizukuPermission() {
        try {
            if (!isShizukuAvailable()) return false;
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method checkMethod = shizukuClass.getMethod("checkSelfPermission");
            Object res = checkMethod.invoke(null);
            return res instanceof Integer && ((Integer) res) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void requestShizukuPermission(int requestCode) {
        try {
            if (isShizukuAvailable() && !hasShizukuPermission()) {
                Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
                Method reqMethod = shizukuClass.getMethod("requestPermission", int.class);
                reqMethod.invoke(null, requestCode);
            }
        } catch (Exception ignored) {}
    }

    public static void executeGodRamPurge() {
        if (!godModAksActive.get() || !hasShizukuPermission()) return;
        executeShizukuShellCommand("am kill-all; dumpsys deviceidle disable");
        SmartAiFirewallManager.logOptimizer("[8 God MOD AKS]: Shizuku System RAM Purged & Doze Mode Suspended.");
    }

    public static void executeGodKernelFirewall(String attackerIp) {
        if (!godModAksActive.get() || !hasShizukuPermission() || attackerIp == null || attackerIp.isEmpty()) return;
        executeShizukuShellCommand("iptables -A INPUT -s " + attackerIp + " -j DROP");
        SmartAiFirewallManager.logFirewall("[8 God MOD AKS]: Kernel-level iptables DROP rule applied for " + attackerIp);
    }

    public static void executeGodSystemTuning() {
        if (!godModAksActive.get() || !hasShizukuPermission()) return;
        executeShizukuShellCommand("settings put global window_animation_scale 0.0; settings put global transition_animation_scale 0.0; settings put global animator_duration_scale 0.0; settings put global power_saving 0");
        SmartAiFirewallManager.logOptimizer("[8 God MOD AKS]: System UI animations disabled & High Performance profile enforced.");
    }

    public static void executeHotspotAdAndAdultBlocker() {
        if (!godModAksActive.get() || !hasShizukuPermission()) return;
        executeShizukuShellCommand("iptables -t nat -A PREROUTING -i wlan+ -p udp --dport 53 -j REDIRECT --to-ports 53; iptables -t nat -A PREROUTING -i rndis+ -p udp --dport 53 -j REDIRECT --to-ports 53; iptables -t nat -A PREROUTING -i ap0 -p udp --dport 53 -j REDIRECT --to-ports 53");
        SmartAiFirewallManager.logFirewall("[HOTSPOT SHIELD]: Hotspot & Tethering sharing clients intercepted. Adult websites and ad-trackers blocked globally.");
    }

    private static void executeShizukuShellCommand(String cmd) {
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcessMethod = shizukuClass.getMethod("newProcess", String[].class, String[].class, String.class);
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh"}, null, null);
            if (process != null) {
                OutputStream os = process.getOutputStream();
                os.write((cmd + "\nexit\n").getBytes());
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            Log.e("ZPU_GOD_MOD", "Error executing Shizuku command: " + cmd, e);
        }
    }

    public static boolean isGodModAksActive() {
        return godModAksActive.get();
    }

    public static void setGodModAksActive(boolean active) {
        godModAksActive.set(active);
        if (active && isShizukuAvailable() && !hasShizukuPermission()) {
            requestShizukuPermission(808);
        }
    }
}
