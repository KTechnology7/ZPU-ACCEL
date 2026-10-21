package com.Bllod100.zpu.core;

import java.util.HashMap;
import java.util.Map;

public class SecurityLayerManager {
    private final Map<String, Boolean> layers = new HashMap<>();

    public void setLayerStatus(int level, boolean active) {
        layers.put("L" + level, active);
    }

    public boolean isLayerActive(int level) {
        Boolean active = layers.get("L" + level);
        return active != null && active;
    }

    public String getActiveChain() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= 4; i++) {
            if (isLayerActive(i)) sb.append(i);
        }
        return sb.length() == 0 ? "NONE" : sb.toString();
    }
}
