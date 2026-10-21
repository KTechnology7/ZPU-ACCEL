package com.Bllod100.zpu.core;

import java.util.HashMap;
import java.util.Map;

public class PerformanceLayerManager {
    private final Map<String, Boolean> tiers = new HashMap<>();

    public void setTierStatus(int level, boolean active) {
        tiers.put("P" + level, active);
    }

    public boolean isTierActive(int level) {
        Boolean active = tiers.get("P" + level);
        return active != null && active;
    }

    public String getActiveChain() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (isTierActive(i)) sb.append(i);
        }
        return sb.length() == 0 ? "NONE" : sb.toString();
    }
}
