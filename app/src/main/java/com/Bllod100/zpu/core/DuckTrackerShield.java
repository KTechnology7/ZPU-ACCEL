package com.Bllod100.zpu.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ZPU DUCKDUCKGO-GRADE APP TRACKER & PRIVACY PROTECTION SHIELD
 * DuckDuckGo App Tracking Protection technology for Android & PC.
 * Blocks embedded app trackers, telemetry SDKs, ad networks, and fingerprinting probes.
 */
public class DuckTrackerShield {
    private static final Set<String> DUCK_TRACKER_DOMAINS = new CopyOnWriteArraySet<>();

    static {
        // 1. DuckDuckGo App Trackers & Telemetry SDKs
        DUCK_TRACKER_DOMAINS.add("graph.facebook.com");
        DUCK_TRACKER_DOMAINS.add("an.facebook.com");
        DUCK_TRACKER_DOMAINS.add("analytics.google.com");
        DUCK_TRACKER_DOMAINS.add("google-analytics.com");
        DUCK_TRACKER_DOMAINS.add("app-measurement.com");
        DUCK_TRACKER_DOMAINS.add("appsflyer.com");
        DUCK_TRACKER_DOMAINS.add("adjust.com");
        DUCK_TRACKER_DOMAINS.add("kochava.com");
        DUCK_TRACKER_DOMAINS.add("flurry.com");
        DUCK_TRACKER_DOMAINS.add("branch.io");
        DUCK_TRACKER_DOMAINS.add("mixpanel.com");
        DUCK_TRACKER_DOMAINS.add("segment.io");
        DUCK_TRACKER_DOMAINS.add("singular.net");
        DUCK_TRACKER_DOMAINS.add("amplitude.com");
        DUCK_TRACKER_DOMAINS.add("applovin.com");
        DUCK_TRACKER_DOMAINS.add("vungle.com");
        DUCK_TRACKER_DOMAINS.add("mopub.com");
        DUCK_TRACKER_DOMAINS.add("chartboost.com");
        DUCK_TRACKER_DOMAINS.add("unityads.unity3d.com");
        DUCK_TRACKER_DOMAINS.add("adcolony.com");
        DUCK_TRACKER_DOMAINS.add("byteoversea.com");
        DUCK_TRACKER_DOMAINS.add("pangle.io");

        // 2. Ad Networks & Popups
        DUCK_TRACKER_DOMAINS.add("doubleclick.net");
        DUCK_TRACKER_DOMAINS.add("googlesyndication.com");
        DUCK_TRACKER_DOMAINS.add("googleadservices.com");
        DUCK_TRACKER_DOMAINS.add("adservice.google.com");
        DUCK_TRACKER_DOMAINS.add("pagead2.googlesyndication.com");
        DUCK_TRACKER_DOMAINS.add("amazon-adsystem.com");
        DUCK_TRACKER_DOMAINS.add("adnxs.com");
        DUCK_TRACKER_DOMAINS.add("criteo.com");
        DUCK_TRACKER_DOMAINS.add("taboola.com");
        DUCK_TRACKER_DOMAINS.add("outbrain.com");
        DUCK_TRACKER_DOMAINS.add("rubiconproject.com");
        DUCK_TRACKER_DOMAINS.add("pubmatic.com");
        DUCK_TRACKER_DOMAINS.add("casalemedia.com");
        DUCK_TRACKER_DOMAINS.add("openx.net");
        DUCK_TRACKER_DOMAINS.add("popads.net");
        DUCK_TRACKER_DOMAINS.add("popcash.net");

        // 3. OS & Hardware Telemetry
        DUCK_TRACKER_DOMAINS.add("telemetry.microsoft.com");
        DUCK_TRACKER_DOMAINS.add("v10.events.data.microsoft.com");
        DUCK_TRACKER_DOMAINS.add("diag.google.com");
        DUCK_TRACKER_DOMAINS.add("device-provisioning.googleapis.com");
        DUCK_TRACKER_DOMAINS.add("scorecardresearch.com");
        DUCK_TRACKER_DOMAINS.add("quantserve.com");
    }

    public static boolean isTrackerOrAd(String host) {
        if (host == null || host.length() == 0) return false;
        String lower = host.toLowerCase().trim();
        for (String domain : DUCK_TRACKER_DOMAINS) {
            if (lower.contains(domain)) {
                return true;
            }
        }
        return false;
    }

    public static int getTrackerDomainCount() {
        return DUCK_TRACKER_DOMAINS.size();
    }
}
