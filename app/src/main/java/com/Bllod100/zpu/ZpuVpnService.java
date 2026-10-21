package com.Bllod100.zpu;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.Bllod100.zpu.core.DnsPacketHandler;
import com.Bllod100.zpu.core.SavageNeuralFirewall;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.Socket;

public class ZpuVpnService extends VpnService {
    public static volatile ZpuVpnService instance;
    public static volatile String currentUpstreamDns = "1.1.1.1";
    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private java.util.concurrent.ExecutorService dnsThreadPool;
    private volatile boolean isRunning = false;
    private String primaryDns = "1.1.1.1";
    private String secondaryDns = "8.8.8.8";
    private boolean noDnsMode = false;

    private static final String[] VPN_ROUTES = {
        "1.1.1.1", "8.8.8.8", "8.8.4.4", "9.9.9.9", "94.140.14.14", "94.140.14.15",
        "10.0.2.3"
    };

    public static void protectSocket(DatagramSocket socket) {
        if (instance != null && socket != null) {
            try { instance.protect(socket); } catch (Exception ignored) {}
        }
    }

    public static void protectSocket(Socket socket) {
        if (instance != null && socket != null) {
            try { instance.protect(socket); } catch (Exception ignored) {}
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        if (intent != null) {
            if ("STOP".equals(intent.getAction())) {
                stopVpn();
                return START_NOT_STICKY;
            }
            if (intent.hasExtra("NO_DNS")) {
                noDnsMode = intent.getBooleanExtra("NO_DNS", false);
            }
            if (intent.hasExtra("PRIMARY_DNS")) {
                primaryDns = intent.getStringExtra("PRIMARY_DNS");
                currentUpstreamDns = primaryDns;
            }
            if (intent.hasExtra("SECONDARY_DNS")) {
                secondaryDns = intent.getStringExtra("SECONDARY_DNS");
            }
            com.Bllod100.zpu.core.FastDnsResolver.clearCache();
        }
        if (!isRunning) {
            startVpn();
        }
        return START_STICKY;
    }

    private synchronized void startVpn() {
        if (isRunning) return;
        try {
            Builder builder = new Builder();
            builder.addAddress("10.8.0.2", 24)
                   .addDisallowedApplication(getPackageName());

            if (android.os.Build.VERSION.SDK_INT >= 29) {
                try { builder.allowBypass(); } catch (Exception ignored) {}
            }

            if (!noDnsMode && !"SYSTEM".equals(currentUpstreamDns)) {
                for (int i = 0; i < VPN_ROUTES.length; i++) {
                    builder.addRoute(VPN_ROUTES[i], 32);
                }
                builder.addDnsServer(primaryDns != null && !"SYSTEM".equals(primaryDns) ? primaryDns : "1.1.1.1")
                       .addDnsServer(secondaryDns != null && !"SYSTEM".equals(secondaryDns) ? secondaryDns : "8.8.8.8");
            }

            builder.setMtu(1400)
                   .setSession(noDnsMode ? "ZPU Pure Firewall TUN" : "ZPU DuckDuckGo Privacy Shield TUN")
                   .setBlocking(false);

            vpnInterface = builder.establish();
            if (vpnInterface == null) {
                Log.e("ZPU_VPN", "VPN establish returned null");
                return;
            }

            isRunning = true;
            if (dnsThreadPool == null || dnsThreadPool.isShutdown()) {
                dnsThreadPool = java.util.concurrent.Executors.newFixedThreadPool(8);
            }
            vpnThread = new Thread(this::runVpnLoop, "ZPU-VPN-Loop");
            vpnThread.start();
            com.Bllod100.zpu.core.ZpuShizukuGodManager.executeHotspotAdAndAdultBlocker();
            notifyVpnStateChanged(true);
        } catch (Exception e) {
            stopVpn();
        }
    }

    private void runVpnLoop() {
        while (isRunning) {
            ParcelFileDescriptor pfd = vpnInterface;
            if (pfd == null) {
                try { Thread.sleep(500); } catch (Exception ignored) {}
                continue;
            }
            try (FileInputStream in = new FileInputStream(pfd.getFileDescriptor());
                 FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
                byte[] buffer = new byte[65536];

                SavageNeuralFirewall firewall = null;
                if (ResourceManagerService.inst != null && ResourceManagerService.inst.getBrain() != null) {
                    firewall = ResourceManagerService.inst.getBrain().getFirewall();
                }

                while (isRunning && vpnInterface == pfd) {
                    try {
                        SavageNeuralFirewall fw = firewall;
                        if (fw == null && ResourceManagerService.inst != null && ResourceManagerService.inst.getBrain() != null) {
                            fw = ResourceManagerService.inst.getBrain().getFirewall();
                            firewall = fw;
                        }

                        int length = in.read(buffer);
                        if (length > 0) {
                            if (!noDnsMode) {
                                byte[] packetCopy = java.util.Arrays.copyOf(buffer, length);
                                final SavageNeuralFirewall currentFw = fw;
                                dnsThreadPool.execute(() -> {
                                    try {
                                        byte[] dnsResp = DnsPacketHandler.handleDnsPacket(packetCopy, packetCopy.length, currentFw, ZpuVpnService.this);
                                        if (dnsResp != null) {
                                            if (currentFw != null) {
                                                currentFw.addTetheredBytes(dnsResp.length);
                                            }
                                            synchronized (out) {
                                                try {
                                                    out.write(dnsResp, 0, dnsResp.length);
                                                    out.flush();
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                });
                            } else {
                                synchronized (out) {
                                    out.write(buffer, 0, length);
                                    out.flush();
                                }
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        if (!isRunning) break;
                        try { Thread.sleep(50); } catch (InterruptedException ie) { break; }
                    }
                }
            } catch (Exception e) {
                if (!isRunning) break;
                try { Thread.sleep(1000); } catch (InterruptedException ie) { break; }
            }
        }
    }

    public synchronized void stopVpn() {
        isRunning = false;
        if (vpnThread != null) {
            vpnThread.interrupt();
            vpnThread = null;
        }
        if (dnsThreadPool != null) {
            try { dnsThreadPool.shutdownNow(); } catch (Exception ignored) {}
            dnsThreadPool = null;
        }
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
        } catch (Exception ignored) {}
        instance = null;
        notifyVpnStateChanged(false);
    }

    public void notifyVpnStateChanged(boolean active) {
        try {
            getSharedPreferences("zpu_settings", MODE_PRIVATE).edit().putBoolean("system_vpn_enabled", active).apply();
            Intent intent = new Intent("com.Bllod100.zpu.VPN_STATE_CHANGED");
            intent.putExtra("active", active);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        } catch (Exception ignored) {}
    }

    public static boolean isVpnRunning() {
        return instance != null && instance.isRunning;
    }

    @Override
    public void onRevoke() {
        stopVpn();
        notifyVpnStateChanged(false);
        super.onRevoke();
    }

    @Override
    public void onDestroy() {
        stopVpn();
        notifyVpnStateChanged(false);
        super.onDestroy();
    }
}
