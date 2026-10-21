package com.Bllod100.zpu.core;

import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZPU HIGH-SPEED PARALLEL DNS RESOLVER & CACHE
 * Accelerates host resolution with in-memory caching and Windows NCSI instant DNS bypass.
 */
public class FastDnsResolver {
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long DEFAULT_TTL_MS = 5 * 60 * 1000; // 5 Minutes Cache TTL

    private static class CacheEntry {
        final InetAddress address;
        final long expiresAt;

        CacheEntry(InetAddress address, long expiresAt) {
            this.address = address;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    static {
        try {
            // Instant NCSI DNS probes mapping for Windows 10/11 Internet Detection
            InetAddress ncsiIp = InetAddress.getByAddress(new byte[]{(byte) 131, (byte) 107, (byte) 255, (byte) 255});
            cache.put("dns.msftncsi.com", new CacheEntry(ncsiIp, Long.MAX_VALUE));
            cache.put("www.msftncsi.com", new CacheEntry(ncsiIp, Long.MAX_VALUE));
            cache.put("msftconnecttest.com", new CacheEntry(ncsiIp, Long.MAX_VALUE));
            cache.put("www.msftconnecttest.com", new CacheEntry(ncsiIp, Long.MAX_VALUE));

            InetAddress localIp = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
            cache.put("localhost", new CacheEntry(localIp, Long.MAX_VALUE));
        } catch (Exception ignored) {}
    }

    public static InetAddress resolve(String host) throws UnknownHostException {
        if (host == null || host.isEmpty()) {
            throw new UnknownHostException("Host is null or empty");
        }

        String lowerHost = host.toLowerCase().trim();

        // 1. Check Fast LRU Memory Cache
        CacheEntry entry = cache.get(lowerHost);
        if (entry != null && !entry.isExpired()) {
            return entry.address;
        }

        // 2. Perform Network DNS Resolution
        try {
            InetAddress resolved = InetAddress.getByName(lowerHost);
            cache.put(lowerHost, new CacheEntry(resolved, System.currentTimeMillis() + DEFAULT_TTL_MS));
            return resolved;
        } catch (UnknownHostException e) {
            Log.w("ZPU_DNS", "DNS Lookup Failed for: " + lowerHost);
            throw e;
        }
    }

    public static void clearCache() {
        cache.clear();
    }
}
