package com.Bllod100.zpu.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ZPU PRE-CONFIGURED PRIVATE AD-BLOCK & TRACKER SHIELD
 * Pre-configured privacy blocklist for instant ad blocking, telemetry suppression, and speed acceleration.
 */
public class AdBlockShield {
    private static final Set<String> PRECONFIGURED_AD_DOMAINS = new CopyOnWriteArraySet<>();

    static {
        // Major Ad Networks
        PRECONFIGURED_AD_DOMAINS.add("doubleclick.net");
        PRECONFIGURED_AD_DOMAINS.add("googlesyndication.com");
        PRECONFIGURED_AD_DOMAINS.add("googleadservices.com");
        PRECONFIGURED_AD_DOMAINS.add("adservice.google.com");
        PRECONFIGURED_AD_DOMAINS.add("pagead2.googlesyndication.com");
        PRECONFIGURED_AD_DOMAINS.add("amazon-adsystem.com");
        PRECONFIGURED_AD_DOMAINS.add("adnxs.com");
        PRECONFIGURED_AD_DOMAINS.add("criteo.com");
        PRECONFIGURED_AD_DOMAINS.add("taboola.com");
        PRECONFIGURED_AD_DOMAINS.add("outbrain.com");
        PRECONFIGURED_AD_DOMAINS.add("rubiconproject.com");
        PRECONFIGURED_AD_DOMAINS.add("pubmatic.com");
        PRECONFIGURED_AD_DOMAINS.add("casalemedia.com");
        PRECONFIGURED_AD_DOMAINS.add("openx.net");
        PRECONFIGURED_AD_DOMAINS.add("popads.net");
        PRECONFIGURED_AD_DOMAINS.add("popcash.net");

        // Telemetry & Trackers
        PRECONFIGURED_AD_DOMAINS.add("telemetry.microsoft.com");
        PRECONFIGURED_AD_DOMAINS.add("v10.events.data.microsoft.com");
        PRECONFIGURED_AD_DOMAINS.add("graph.facebook.com");
        PRECONFIGURED_AD_DOMAINS.add("analytics.google.com");
        PRECONFIGURED_AD_DOMAINS.add("google-analytics.com");
        PRECONFIGURED_AD_DOMAINS.add("app-measurement.com");
        PRECONFIGURED_AD_DOMAINS.add("mixpanel.com");
        PRECONFIGURED_AD_DOMAINS.add("segment.io");
        PRECONFIGURED_AD_DOMAINS.add("adjust.com");
        PRECONFIGURED_AD_DOMAINS.add("appsflyer.com");
        PRECONFIGURED_AD_DOMAINS.add("scorecardresearch.com");
        PRECONFIGURED_AD_DOMAINS.add("quantserve.com");
    }

    public static boolean isAdOrTracker(String host) {
        if (host == null || host.length() == 0) return false;
        String lower = host.toLowerCase().trim();
        for (String adDomain : PRECONFIGURED_AD_DOMAINS) {
            if (lower.contains(adDomain)) {
                return true;
            }
        }
        return false;
    }

    public static int getPreconfiguredCount() {
        return PRECONFIGURED_AD_DOMAINS.size();
    }
}
