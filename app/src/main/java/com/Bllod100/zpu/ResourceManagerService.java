package com.Bllod100.zpu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import com.Bllod100.zpu.core.SavageWatchdog;
import com.Bllod100.zpu.core.ZpuBridge;
import com.Bllod100.zpu.core.ZpuFiber;

public class ResourceManagerService extends Service {
    public static ResourceManagerService inst = null;
    private volatile boolean r = false;
    private SavageIntelligence brain;
    private SavageProxyEngine proxy;
    public int curClockValue = 0;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                // Background sensing active
            } else if ("com.android.zpu.TELEMETRY_SYNC".equals(action)) {
                int ram = intent.getIntExtra("ram", 0);
                int gpu = intent.getIntExtra("gpu", 0);
                int temp = intent.getIntExtra("temp", 0);
                if (brain != null) brain.setPcTelemetry(ram, gpu, 100.0, temp);
            }
        }
    };

    private final BroadcastReceiver usbConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(action) || android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                com.Bllod100.zpu.core.ZpuAudioFeedback.playConnectSound();
                com.Bllod100.zpu.core.ZpuHistoryLogger.logToFile(context, "USB_CONNECTED", "Physical USB cable / driver connection detected. ZPU Proxy Gateway bound to USB port.");
            } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action) || android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                com.Bllod100.zpu.core.ZpuAudioFeedback.playDisconnectSound();
                com.Bllod100.zpu.core.ZpuHistoryLogger.logToFile(context, "USB_DISCONNECTED", "Physical USB cable disconnected.");
            }
        }
    };

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    @Override
    @SuppressWarnings("UnspecifiedRegisterReceiverFlag")
    public void onCreate() {
        super.onCreate();
        inst = this;
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ZPU:ScreenOffCpuLock");
                wakeLock.acquire();
            }
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "ZPU:HighPerfWifiLock");
                wifiLock.acquire();
            }
            com.Bllod100.zpu.core.SmartAiFirewallManager.logOptimizer("[SCREEN-OFF LOCK]: Partial CPU WakeLock & High-Perf Wi-Fi Lock active.");
        } catch (Exception ignored) {}

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        try {
            filter.addAction("com.android.zpu.TELEMETRY_SYNC");
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(batteryReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(batteryReceiver, filter);
            }
        } catch (Exception ignored) {}

        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        usbFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        usbFilter.addAction(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED);
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(usbConnectionReceiver, usbFilter, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(usbConnectionReceiver, usbFilter);
            }
        } catch (Exception ignored) {}

        new Thread(() -> {
            try {
                brain = new SavageIntelligence(this); Thread.sleep(300);
                
                // INITIALIZE HARDWARE PROXY & USB TETHERING GATEWAY ON DEFAULT PORT 8080
                proxy = new SavageProxyEngine(brain.getFirewall());
                proxy.start(8080);
                
                HardCryptEngine.init(this);
                SavageWatchdog.start();
                com.Bllod100.zpu.core.ZpuNativePortEngine.warmUpColdStartCache();
                
                r = true;
                ZpuFiber.launch(() -> {
                    while (r) {
                        curClockValue = 2400;
                        if (brain != null) {
                            try {
                                brain.updateVitals();
                                com.Bllod100.zpu.core.ZpuDeepFreezeManager.executeDeepFreeze(this);
                            } catch (Exception ignored) {}
                        }
                        SavageWatchdog.pulse();
                        try { Thread.sleep(500); } catch (Exception ignored) {}
                    }
                });
                
                // TELEMETRY BRIDGE ON PORT 60000
                ZpuBridge.ignite(this, brain, 60000);
            } catch (Exception ignored) {}
        }).start();

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1, n(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(1, n());
            }
        } catch (Exception e) {
            // Fallback for strict background start restrictions
        }
    }

    private Notification n() {
        String cid = "z";
        NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (m != null) m.createNotificationChannel(new NotificationChannel(cid, "Z", NotificationManager.IMPORTANCE_LOW));
        return new NotificationCompat.Builder(this, cid)
            .setContentTitle("ZPU v420.2 APEX")
            .setContentText("ULTIMATE ACCELERATOR ACTIVE")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int bridgePort = 60000;
        if (intent != null) {
            bridgePort = intent.getIntExtra("PROXY_PORT", 60000);
            if (intent.hasExtra("TETHER_PROXY_PORT")) {
                int tetherPort = intent.getIntExtra("TETHER_PROXY_PORT", 8080);
                if (proxy != null && proxy.getPort() != tetherPort) {
                    proxy.stop();
                    proxy.start(tetherPort);
                }
            }
        }
        final int fPort = bridgePort;
        new Thread(() -> {
            while (brain == null) { try { Thread.sleep(100); } catch (Exception e) {} }
            ZpuBridge.ignite(this, brain, fPort);
        }).start();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartIntent = new Intent(getApplicationContext(), this.getClass());
        restartIntent.setPackage(getPackageName());
        startService(restartIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        r = false;
        try {
            if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
            if (wifiLock != null && wifiLock.isHeld()) wifiLock.release();
        } catch (Exception ignored) {}
        ZpuBridge.stop();
        if (proxy != null) proxy.stop();
        try { unregisterReceiver(batteryReceiver); } catch (Exception ignored) {}
        try { unregisterReceiver(usbConnectionReceiver); } catch (Exception ignored) {}
        inst = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
    public SavageIntelligence getBrain() { return brain; }

    public void startProxyGateway(int port) {
        if (brain != null && brain.getFirewall() != null) {
            if (proxy == null) {
                proxy = new SavageProxyEngine(brain.getFirewall());
            }
            proxy.start(port);
        }
    }

    public void stopProxyGateway() {
        if (proxy != null) {
            proxy.stop();
        }
    }

    public SavageProxyEngine getProxyEngine() {
        return proxy;
    }

    public void setTurboChargeMode(boolean active) {
        if (brain != null) {
            try {
                if (active) {
                    Runtime.getRuntime().exec("su -c 'echo powersave > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor'");
                } else {
                    Runtime.getRuntime().exec("su -c 'echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor'");
                }
            } catch (Exception ignored) {}
        }
    }
}
