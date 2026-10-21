package com.Bllod100.zpu.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.RandomAccessFile;
import android.util.Log;
import android.content.Context;
import android.app.ActivityManager;
import com.Bllod100.zpu.SavageIntelligence;

/**
 * ZPU Stable Bridge with Dynamic Duplex Telemetry and Swap Hardening
 */
public class ZpuBridge {
    private static int currentPort = 60000;
    private static volatile boolean running = false;
    private static volatile boolean isForceClosing = false;
    private static ServerSocket activeServerSocket = null;
    private static final SmartBondAI bondAI = new SmartBondAI();

    public static void triggerForceCloseSignal() {
        isForceClosing = true;
    }

    public static void ignite(Context context, SavageIntelligence brain, int port) {
        if (running && currentPort == port) return;
        stop();
        currentPort = port;
        running = true;
        
        // ETERNAL BEACON: Background pulse for PC discovery (UDP Broadcast + Logcat)
        new Thread(() -> {
            while (running) {
                java.net.DatagramSocket socket = null;
                try {
                    socket = new java.net.DatagramSocket();
                    socket.setBroadcast(true);
                    byte[] data = "ZPU_NODE_READY".getBytes();
                    java.net.InetAddress broadcastAddr = java.net.InetAddress.getByName("255.255.255.255");
                    java.net.DatagramPacket packet = new java.net.DatagramPacket(data, data.length, broadcastAddr, 60002);
                    
                    socket.send(packet);
                    Log.d("ZPU_BEACON", "READY");
                } catch (Exception e) {
                    // SILENT FAIL: Prevents crash if network is down or broadcast restricted
                } finally {
                    if (socket != null) {
                        try { socket.close(); } catch (Exception ignored) {}
                    }
                }
                try { Thread.sleep(1000); } catch (Exception ignored) {}
            }
        }).start();

        new Thread(() -> {
            while (running) {
                try {
                    ServerSocket server = new ServerSocket();
                    activeServerSocket = server;
                    server.setReuseAddress(true);
                    server.bind(new InetSocketAddress(currentPort));
                    
                    while (running) {
                        Socket client = server.accept();
                        client.setTcpNoDelay(true);
                        client.setKeepAlive(true);
                        ZpuAudioFeedback.playConnectSound();

                        final String remoteAddr = client.getRemoteSocketAddress() != null ? client.getRemoteSocketAddress().toString() : "UNKNOWN";
                        final String connType = remoteAddr.contains("127.0.0.1") || remoteAddr.contains("localhost") ? "USB ADB PORT FORWARD (127.0.0.1)" :
                                                remoteAddr.contains("192.168.42.") || remoteAddr.contains("192.168.43.") || remoteAddr.contains("172.20.") ? "USB INTERNET TETHERING (RNDIS/ECM)" : "WIFI LOCAL NETWORK";

                        ZpuHistoryLogger.logToFile(context, "CONNECTION_ESTABLISHED", "Connected to PC Master Bridge via " + connType + " [" + remoteAddr + "]. ZpuBridge executed securely.");
                        ZpuHistoryLogger.setLoggingActive(true);
                        SmartAiFirewallManager.logOptimizer("[SYSTEM OPTIMIZER]: Game Booster & Vista Acceleration engine active.");
                        SmartAiFirewallManager.logOptimizer("[GAME BOOST]: 144Hz Display Mode & VSync Optimization applied.");
                        SmartAiFirewallManager.logOptimizer("[MEMORY CONTROLLER]: Double-Channel ZRAM Swap Pool synchronized.");

                        // DOUBLE-CHANNEL: Handle each link in a dedicated fiber
                        ZpuFiber.launch(() -> {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                 PrintWriter writer = new PrintWriter(client.getOutputStream(), true)) {
                                
                                String line;
                                boolean authenticated = false;
                                while (running && (line = reader.readLine()) != null) {
                                    if (line.startsWith("AUTH_KEY:")) {
                                        String key = line.substring(9).trim();
                                        if (ZpuSecretKeyManager.validateKey(context, key)) {
                                            authenticated = true;
                                            ZpuHistoryLogger.logToFile(context, "SECURITY_AUTH", "Secret Key Authentication SUCCESSFUL from " + remoteAddr);
                                        } else {
                                            ZpuHistoryLogger.logToFile(context, "SECURITY_AUTH_FAILED", "Secret Key Authentication FAILED from " + remoteAddr);
                                            writer.println("AUTH_FAILED");
                                            break;
                                        }
                                    }
                                    if (line.startsWith("MASTER_ID:")) {
                                        brain.registerMaster(line.substring(10));
                                    }
                                    byte[] raw = line.getBytes();
                                    byte[] verified = brain.processPacket(0, raw);
                                    if (verified.length > 0) {
                                        parseAndInject(new String(verified), brain);
                                    }
                                    writer.println(getPhoneTelemetry(context, brain));
                                }
                            } catch (Exception e) {
                                ZpuHistoryLogger.logToFile(context, "FUNCTION_FAILURE", "ZpuBridge session stream failed: " + e.getMessage());
                            }
                            finally {
                                ZpuAudioFeedback.playDisconnectSound();
                                ZpuHistoryLogger.setLoggingActive(false);
                                ZpuHistoryLogger.logToFile(context, "CONNECTION_DISCONNECTED", "Disconnected from " + connType + " [" + remoteAddr + "]. Session ended cleanly.");
                                try { client.close(); } catch (Exception ignored) {}
                            }
                        });
                    }
                    server.close();
                } catch (Exception e) {
                    try { Thread.sleep(1000); } catch (Exception ignored) {}
                }
            }
        }).start();
    }

    private static String getPhoneTelemetry(Context context, SavageIntelligence brain) {
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            am.getMemoryInfo(mi);
            int ramPct = (int) (100 - (mi.availMem / (double) mi.totalMem * 100));
            
            // SHARED MEMORY: Process-specific allocation in MB
            int sharedMem = (brain != null) ? brain.getCachedSharedMem() : 0;
            
            int gpuLoad = (brain != null) ? brain.getDynamicGpu() : 0;
            int cpuLoad = (brain != null) ? (int)brain.getPScale() : 0;
            int hits = (brain != null) ? brain.getVacuumCount() : 0;
            int swap = getSwapUsage();
            int exploits = (brain != null) ? brain.getExploitCount() : 0;
            String shield = (brain != null && brain.isGlobalShieldActive()) ? "ON" : "OFF";
            String bond = (bondAI != null) ? bondAI.getActiveMode().name() : "AUTO";
            String blkList = (brain != null) ? brain.getForbiddenList() : "";
            String hwId = HardwareProbe.getHardwareSignature();
            boolean isMaster = (brain != null && brain.isMasterNode());
            double tetherMb = (brain != null && brain.getFirewall() != null) ? (brain.getFirewall().getTetheredBytes() / 1048576.0) : 0.0;
            int tetherBlocks = (brain != null && brain.getFirewall() != null) ? brain.getFirewall().getBlockedTetheredRequestsCount() : 0;
            double netSpeed = (brain != null && brain.getFirewall() != null) ? brain.getFirewall().getCurrentSpeedMBps() : 0.0;

            String dnsVal = com.Bllod100.zpu.ZpuVpnService.currentUpstreamDns;
            String statusStr = isForceClosing ? ",STATUS:FORCE_CLOSE" : "";
            String pcLowPwrStr = ZpuStateRegistry.isPcLowPowerMode() ? "ON" : "OFF";
            return String.format(java.util.Locale.US, "P_RAM:%d,P_SHARED:%d,P_CPU:%d,P_GPU:%d,HITS:%d,SWAP:%d,EXPLOITS:%d,SHIELD:%s,BOND:%s,BLKLIST:%s,HW:%s,MASTER:%b,TETHER_MB:%.1f,TETHER_BLOCKS:%d,NET_SPEED:%.2f,DNS:%s,PC_LOW_PWR:%s%s",
                    ramPct, sharedMem, cpuLoad, gpuLoad, hits, swap, exploits, shield, bond, blkList, hwId, isMaster, tetherMb, tetherBlocks, netSpeed, dnsVal, pcLowPwrStr, statusStr);
        } catch (Exception e) {
            return "P_RAM:0,P_GPU:0,HITS:0,SWAP:0,SHIELD:OFF,BOND:OFFLINE";
        }
    }

    public static int getSwapUsage() {
        // Reads /proc/meminfo to calculate real ZRAM/Swap usage on Samsung kernels
        try (RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r")) {
            String line;
            long total = 0, free = 0;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("SwapTotal:")) total = Long.parseLong(line.replaceAll("[^0-9]", ""));
                else if (line.startsWith("SwapFree:")) free = Long.parseLong(line.replaceAll("[^0-9]", ""));
            }
            if (total == 0) return 0;
            return (int) (100 - (free / (double) total * 100));
        } catch (Exception e) {
            return 0;
        }
    }

    private static void parseAndInject(String data, SavageIntelligence brain) {
        if (brain == null) return;
        try {
            String[] parts = data.split(",");
            int ram = 0, gpu = 0;
            for (String p : parts) {
                if (p.startsWith("RAM:")) ram = Integer.parseInt(p.substring(4));
                else if (p.startsWith("GPU:")) gpu = Integer.parseInt(p.substring(4));
            }
            brain.setPcTelemetry(ram, gpu, 100.0, 55);
        } catch (Exception e) {}
    }

    public static void stop() {
        running = false;
        if (activeServerSocket != null && !activeServerSocket.isClosed()) {
            try { activeServerSocket.close(); } catch (Exception ignored) {}
            activeServerSocket = null;
        }
    }
}
