package com.Bllod100.zpu.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SavageNeuralFirewall {
    private final AtomicInteger blockedPackets = new AtomicInteger(0);
    private final AtomicInteger exploitsBlocked = new AtomicInteger(0);
    private final AtomicLong totalInspected = new AtomicLong(0);
    
    private final AtomicLong tetheredBytes = new AtomicLong(0);
    private final AtomicInteger tetheredRequests = new AtomicInteger(0);
    private final AtomicInteger blockedTetheredRequests = new AtomicInteger(0);
    private final Set<String> customBlockedHosts = new CopyOnWriteArraySet<>();
    
    private volatile boolean shieldActive = true;
    private final AtomicInteger adsBlockedCount = new AtomicInteger(0);
    private volatile boolean adBlockEnabled = true;

    private static final String[] S = {
        "34.44.34.34.100.57.37.43", "58.37.40.44.30.47.56.100.57.37.43", 
        "34.44.34.34", "58.37.40.44", "40.31.26.62.63.58.31", 
        "58.37.40.44.40.63.58", "6.53.44.26.41.8",
        "coinhive.com", "miningpoolhub.com", "xmrpool.eu", "stratum+tcp",
        "phishing-secure-login.net", "update-microsoft-security.com"
    };
    
    public boolean inspect(byte[] data) {
        if (!shieldActive) return true;
        if (data == null || data.length == 0) return true;
        totalInspected.incrementAndGet();

        byte[] sample = data;
        if (data.length > 2048) {
            sample = new byte[2048];
            for (int i = 0; i < 2048; i++) {
                sample[i] = data[i];
            }
        }

        if (getEntropy(sample) > 7.95 || checkFortiGuardSignatures(sample)) {
            exploitsBlocked.incrementAndGet();
            return false;
        }

        return true;
    }

    public boolean isHostAllowed(String host) {
        if (!shieldActive || host == null || host.isEmpty()) return true;
        tetheredRequests.incrementAndGet();
        
        String lowerHost = host.toLowerCase().trim();

        if (AdultContentShield.isAdultContent(lowerHost)) {
            blockedTetheredRequests.incrementAndGet();
            adsBlockedCount.incrementAndGet();
            return false;
        }

        if (adBlockEnabled && AdBlockShield.isAdOrTracker(lowerHost)) {
            blockedTetheredRequests.incrementAndGet();
            adsBlockedCount.incrementAndGet();
            return false;
        }
        
        for (String blocked : customBlockedHosts) {
            if (!blocked.isEmpty() && lowerHost.contains(blocked.toLowerCase())) {
                blockedTetheredRequests.incrementAndGet();
                return false;
            }
        }

        for (int i = 0; i < S.length; i++) {
            String sig = S[i];
            String decoded = sig.contains(".") ? sig : d(sig);
            if (decoded.length() >= 4 && lowerHost.contains(decoded)) {
                blockedTetheredRequests.incrementAndGet();
                return false;
            }
        }

        return true;
    }

    public int getAdsBlockedCount() {
        return adsBlockedCount.get();
    }

    public void setAdBlockEnabled(boolean enabled) {
        this.adBlockEnabled = enabled;
    }

    public boolean isAdBlockEnabled() {
        return adBlockEnabled;
    }

    public void addBlockedDomain(String domain) {
        if (domain != null && !domain.trim().isEmpty()) {
            customBlockedHosts.add(domain.trim());
        }
    }

    public void removeBlockedDomain(String domain) {
        if (domain != null) {
            customBlockedHosts.remove(domain.trim());
        }
    }

    private long lastSpeedCheckTime = System.currentTimeMillis();
    private long lastSpeedBytes = 0;
    private double currentSpeedMbps = 0.0;

    public synchronized double getCurrentSpeedMbps() {
        long now = System.currentTimeMillis();
        long timeDelta = now - lastSpeedCheckTime;
        if (timeDelta >= 300) {
            long totalBytes = tetheredBytes.get();
            long bytesDelta = totalBytes - lastSpeedBytes;
            lastSpeedBytes = totalBytes;
            lastSpeedCheckTime = now;
            currentSpeedMbps = (bytesDelta * 8.0) / (timeDelta / 1000.0) / 1000000.0;
            if (currentSpeedMbps < 0) currentSpeedMbps = 0;
        }
        return currentSpeedMbps;
    }

    public double getCurrentSpeedMBps() {
        return getCurrentSpeedMbps() / 8.0;
    }

    public void addTetheredBytes(long bytes) {
        if (bytes > 0) {
            tetheredBytes.addAndGet(bytes);
        }
    }

    public long getTetheredBytes() {
        return tetheredBytes.get();
    }

    public int getTetheredRequestsCount() {
        return tetheredRequests.get();
    }

    public int getBlockedTetheredRequestsCount() {
        return blockedTetheredRequests.get();
    }

    public void setShieldActive(boolean active) {
        this.shieldActive = active;
    }

    private double getEntropy(byte[] data) {
        int[] f = new int[256];
        for (int i = 0; i < data.length; i++) {
            f[data[i] & 0xFF]++;
        }
        double e = 0;
        for (int i = 0; i < 256; i++) {
            int c = f[i];
            if (c > 0) {
                double p = (double) c / data.length;
                e -= p * (Math.log(p) / Math.log(2));
            }
        }
        return e;
    }

    private boolean checkFortiGuardSignatures(byte[] data) {
        String p = new String(data).toLowerCase();
        for (int i = 0; i < S.length; i++) {
            String sig = S[i];
            String decoded = sig.contains(".") ? sig : d(sig);
            if (p.contains(decoded)) return true;
        }
        return false;
    }

    public String getForbiddenList() {
        StringBuilder sb = new StringBuilder();
        String adultList = AdultContentShield.getAdultDomainsList();
        if (!adultList.isEmpty()) {
            sb.append(adultList);
        }
        for (int i = 0; i < S.length; i++) {
            String sig = S[i];
            String decoded = sig.contains(".") ? sig : d(sig);
            if (decoded.contains(".")) {
                if (sb.length() > 0) sb.append(",");
                sb.append(decoded);
            }
        }
        for (String custom : customBlockedHosts) {
            if (sb.length() > 0) sb.append(",");
            sb.append(custom);
        }
        return sb.toString();
    }

    private String d(String input) {
        StringBuilder sb = new StringBuilder();
        String[] parts = input.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            try {
                sb.append((char)(Integer.parseInt(parts[i]) ^ 0x5A));
            } catch (Exception ignored) {}
        }
        return sb.toString();
    }

    public int getBlockedCount() { return blockedPackets.get(); }
    public int getExploitsBlocked() { return exploitsBlocked.get(); }
    public long getInspectedCount() { return totalInspected.get(); }
    public boolean isShieldActive() { return shieldActive; }
}
