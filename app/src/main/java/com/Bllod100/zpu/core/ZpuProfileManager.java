package com.Bllod100.zpu.core;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class ZpuProfileManager {
    public static class Profile {
        public String name;
        public int fpsCap;
        public int powerState;
        public double firewallEntropy;
        public int dmaLanes;
        public boolean adBlockEnabled;
        public boolean isDefault;

        public Profile(String name, int fpsCap, int powerState, double firewallEntropy, int dmaLanes, boolean adBlockEnabled, boolean isDefault) {
            this.name = name;
            this.fpsCap = fpsCap;
            this.powerState = powerState;
            this.firewallEntropy = firewallEntropy;
            this.dmaLanes = dmaLanes;
            this.adBlockEnabled = adBlockEnabled;
            this.isDefault = isDefault;
        }

        public JSONObject toJson() {
            try {
                JSONObject obj = new JSONObject();
                obj.put("name", name);
                obj.put("fpsCap", fpsCap);
                obj.put("powerState", powerState);
                obj.put("firewallEntropy", firewallEntropy);
                obj.put("dmaLanes", dmaLanes);
                obj.put("adBlockEnabled", adBlockEnabled);
                obj.put("isDefault", isDefault);
                return obj;
            } catch (Exception e) {
                return new JSONObject();
            }
        }

        public static Profile fromJson(JSONObject obj) {
            try {
                return new Profile(
                    obj.optString("name", "Custom"),
                    obj.optInt("fpsCap", 144),
                    obj.optInt("powerState", 100),
                    obj.optDouble("firewallEntropy", 7.9),
                    obj.optInt("dmaLanes", 1000),
                    obj.optBoolean("adBlockEnabled", true),
                    obj.optBoolean("isDefault", false)
                );
            } catch (Exception e) {
                return getDefaultProfile();
            }
        }
    }

    public static Profile getDefaultProfile() {
        return new Profile("DEFAULT APEX (READ-ONLY)", 144, 100, 7.9, 1000, true, true);
    }

    private static final String PREF_NAME = "zpu_profiles_pref";
    private static final String KEY_PROFILES = "profiles_json";

    public static List<Profile> getProfiles(Context context) {
        List<Profile> list = new ArrayList<>();
        list.add(getDefaultProfile()); // Uneditable backup default profile

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String jsonStr = prefs.getString(KEY_PROFILES, "[]");
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Profile p = Profile.fromJson(obj);
                if (!p.isDefault) {
                    list.add(p);
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    public static void saveProfile(Context context, Profile profile) {
        if (profile.isDefault) return; // Cannot edit default profile
        try {
            List<Profile> current = getProfiles(context);
            boolean found = false;
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).name.equals(profile.name) && !current.get(i).isDefault) {
                    current.set(i, profile);
                    found = true;
                    break;
                }
            }
            if (!found) {
                current.add(profile);
            }

            JSONArray arr = new JSONArray();
            for (Profile p : current) {
                if (!p.isDefault) {
                    arr.put(p.toJson());
                }
            }

            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_PROFILES, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static void deleteProfile(Context context, String name) {
        if ("DEFAULT APEX (READ-ONLY)".equalsIgnoreCase(name)) return;
        try {
            List<Profile> current = getProfiles(context);
            JSONArray arr = new JSONArray();
            for (Profile p : current) {
                if (!p.isDefault && !p.name.equals(name)) {
                    arr.put(p.toJson());
                }
            }
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_PROFILES, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static void applyProfile(Context context, Profile profile) {
        try {
            ZpuLazyRenderCore.setFpsCap(profile.fpsCap);
            SmartAiFirewallManager.logOptimizer("[PROFILE LOADED]: " + profile.name + " applied (FPS:" + profile.fpsCap + ", Power:" + profile.powerState + "%, Entropy:" + profile.firewallEntropy + ", Lanes:" + profile.dmaLanes + ")");
        } catch (Exception ignored) {}
    }
}
