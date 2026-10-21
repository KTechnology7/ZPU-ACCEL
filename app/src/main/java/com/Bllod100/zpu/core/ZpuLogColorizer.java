package com.Bllod100.zpu.core;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

public class ZpuLogColorizer {
    public static CharSequence colorizeLog(String rawLog) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (rawLog == null || rawLog.isEmpty()) return builder;

        String[] lines = rawLog.split("\n");
        for (String line : lines) {
            int color = Color.WHITE; // Default text color

            String upper = line.toUpperCase();
            if (upper.contains("CONNECTED") || upper.contains("ESTABLISHED") || upper.contains("SECURE")) {
                color = Color.parseColor("#00FF00"); // Neon Green
            } else if (upper.contains("DISCONNECTED") || upper.contains("FAILED") || upper.contains("QUARANTINED") || upper.contains("BLOCKED")) {
                color = Color.parseColor("#FF4444"); // Bright Red
            } else if (upper.contains("FIREWALL") || upper.contains("AI") || upper.contains("DPI") || upper.contains("SHIELD")) {
                color = Color.parseColor("#00FFFF"); // Cyan
            } else if (upper.contains("OPTIMIZER") || upper.contains("BOOSTER") || upper.contains("FPS") || upper.contains("POWER")) {
                color = Color.parseColor("#FFD700"); // Gold
            } else if (upper.contains("STARTUP") || upper.contains("SHUTDOWN")) {
                color = Color.parseColor("#FF00FF"); // Magenta
            } else if (upper.contains("FUNCTION") || upper.contains("EXECUTE")) {
                color = Color.parseColor("#FFA500"); // Orange
            }

            SpannableString spannable = new SpannableString(line + "\n");
            spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(spannable);
        }
        return builder;
    }
}
