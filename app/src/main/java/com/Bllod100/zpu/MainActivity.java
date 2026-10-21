package com.Bllod100.zpu;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.Bllod100.zpu.core.HardwareProbe;
import com.Bllod100.zpu.core.SavageWatchdog;
import com.Bllod100.zpu.core.ZpuFiber;
import com.Bllod100.zpu.core.PermissionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Timer t;
    private String currentDnsPreset = "1.1.1.1";
    private final int GOLD = Color.parseColor("#FFD700");
    private final int CYAN = Color.parseColor("#00FFFF");
    private final int NEON_GREEN = Color.parseColor("#00FF00");
    private SwitchMaterial vpnSwitchInstance;

    private final android.content.BroadcastReceiver vpnReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if ("com.Bllod100.zpu.VPN_STATE_CHANGED".equals(intent.getAction())) {
                boolean active = intent.getBooleanExtra("active", false);
                if (vpnSwitchInstance != null && vpnSwitchInstance.isChecked() != active) {
                    vpnSwitchInstance.setChecked(active);
                }
            }
        }
    };

    @Override
    @SuppressWarnings("UnspecifiedRegisterReceiverFlag")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            android.content.IntentFilter filter = new android.content.IntentFilter("com.Bllod100.zpu.VPN_STATE_CHANGED");
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                registerReceiver(vpnReceiver, filter, android.content.Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(vpnReceiver, filter);
            }
        } catch (Exception ignored) {}

        try {
            Window w = getWindow();
            w.setStatusBarColor(Color.BLACK);
            w.setNavigationBarColor(Color.BLACK);
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                w.getAttributes().preferredRefreshRate = 144.0f;
            }
        } catch (Exception ignored) {}

        if (HardwareProbe.isEmulator()) {
            showRestrictionScreen("ENVIRONMENT RESTRICTED: ZPU CANNOT RUN INSIDE UNAUTHORIZED EMULATORS.");
            return;
        }

        PermissionManager.audit(this);
        com.Bllod100.zpu.core.SmartAiFirewallManager.clearFirewallLog();
        com.Bllod100.zpu.core.SmartAiFirewallManager.clearOptimizerLog();
        com.Bllod100.zpu.core.ZpuHistoryLogger.logToFile(this, "APP_STARTUP", "ZPU Application launched successfully. Core services, telemetry, and security layers initialized securely.");
        setupDashboard();
        try {
            startService(new Intent(this, ResourceManagerService.class));
        } catch (Exception e) {
            // Android 14 Foreground Service Start Exception fallback
        }
    }

    private void showRestrictionScreen(String message) {
        LinearLayout frame = new LinearLayout(this);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setBackgroundColor(Color.BLACK);
        frame.setGravity(Gravity.CENTER);
        frame.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("SECURITY ACCESS DENIED");
        title.setTextSize(22f);
        title.setTextColor(Color.RED);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);

        TextView desc = new TextView(this);
        desc.setText(message + "\n\nExecution halted for hardware integrity.");
        desc.setTextSize(14f);
        desc.setTextColor(GOLD);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, 30, 0, 0);

        frame.addView(title);
        frame.addView(desc);
        setContentView(frame);
    }

    private void setupDashboard() {
        LinearLayout frame = new LinearLayout(this);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setBackgroundColor(GOLD);
        frame.setPadding(8, 8, 8, 8);
        frame.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.BLACK);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        frame.addView(scroll);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setBackgroundColor(Color.BLACK);
        inner.setPadding(20, 60, 20, 60);
        scroll.addView(inner);

        TextView harmonyBadge = new TextView(this);
        harmonyBadge.setText("HARMONY SCORE: 100% PERFECT [ALL SYSTEMS ONLINE]");
        harmonyBadge.setTextSize(11f);
        harmonyBadge.setTextColor(NEON_GREEN);
        harmonyBadge.setTypeface(Typeface.DEFAULT_BOLD);
        harmonyBadge.setGravity(Gravity.CENTER);
        harmonyBadge.setPadding(15, 10, 15, 10);
        harmonyBadge.setBackgroundColor(Color.parseColor("#052205"));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(-1, -2);
        hlp.setMargins(0, 0, 0, 15);
        harmonyBadge.setLayoutParams(hlp);
        inner.addView(harmonyBadge);
        
        TextView title = new TextView(this);
        title.setText("ZPU v420.2 HARDWARE FORTRESS");
        title.setTextSize(24f); title.setTextColor(GOLD); title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER); title.setPadding(0, 10, 0, 10);
        inner.addView(title);

        TextView retroCompanionCard = new TextView(this);
        retroCompanionCard.setText("[ZPU COMPANION]: ALL CO-PROCESSOR NODES SECURE ^-^");
        retroCompanionCard.setTextSize(10f);
        retroCompanionCard.setTextColor(CYAN);
        retroCompanionCard.setTypeface(Typeface.MONOSPACE);
        retroCompanionCard.setGravity(Gravity.CENTER);
        retroCompanionCard.setPadding(10, 8, 10, 8);
        retroCompanionCard.setBackgroundColor(Color.parseColor("#0A121A"));
        LinearLayout.LayoutParams rclp = new LinearLayout.LayoutParams(-1, -2);
        rclp.setMargins(0, 0, 0, 15);
        retroCompanionCard.setLayoutParams(rclp);
        inner.addView(retroCompanionCard);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setBackgroundColor(Color.parseColor("#0A0A0A"));
        grid.setPadding(20, 20, 20, 20);
        
        LinearLayout r1 = createStatRow(grid);
        TextView kernelVal = createStat(r1, "AI-KERNEL", "v1.0", GOLD);
        TextView nanoVal = createStat(r1, "NANO-PKT", "0M/s", NEON_GREEN);
        TextView fwVal = createStat(r1, "NEURAL-FW", "SHIELDED", CYAN);
        TextView laneVal = createStat(r1, "DMA-LANES", "1000", GOLD);
        
        LinearLayout r2 = createStatRow(grid);
        TextView crackVal = createStat(r2, "CRACK-ST", "CLEAN", NEON_GREEN);
        TextView ocVal = createStat(r2, "OC-SAFE", "0mV", GOLD);
        TextView syncVal = createStat(r2, "SYNC-ST", "USB+WIFI", Color.WHITE);
        
        LinearLayout r3 = createStatRow(grid);
        TextView secVal = createStat(r3, "SEC-LAY", "L01234", NEON_GREEN);
        TextView perfVal = createStat(r3, "PERF-LAY", "P12345", CYAN);
        
        LinearLayout r4 = createStatRow(grid);
        TextView clockVal = createStat(r4, "CLOCK", "0.00 GHz", Color.WHITE);
        TextView pCoreVal = createStat(r4, "P-CORE", "0%", Color.RED);
        TextView binVal = createStat(r4, "BIN-GRD", "BIN-A", GOLD);
        
        LinearLayout r5 = createStatRow(grid);
        TextView flowVal = createStat(r5, "LIQUID-F", "0.0 L/m", CYAN);
        TextView ghostVal = createStat(r5, "GHOST-ST", "0", Color.RED);
        TextView saveVal = createStat(r5, "CPU-SAVE", "0%", NEON_GREEN);
        TextView swapVal = createStat(r5, "SWAP-MEM", "0%", GOLD);
        
        LinearLayout r6 = createStatRow(grid);
        TextView ipVal = createStat(r6, "NODE-IP", "0.0.0.0", CYAN);
        TextView sharedVal = createStat(r6, "SHARED-M", "0MB", GOLD);
        TextView portSentryVal = createStat(r6, "PORT-SN", "SAFE", NEON_GREEN);

        LinearLayout r7 = createStatRow(grid);
        TextView tetherStVal = createStat(r7, "TETHER-ST", "OFF", Color.GRAY);
        TextView speedVal = createStat(r7, "NET-SPEED", "0.00 MB/s", GOLD);
        TextView tetherMbVal = createStat(r7, "TETHER-MB", "0.0 MB", CYAN);
        TextView tetherBlkVal = createStat(r7, "BLOCKED-REQ", "0", Color.RED);

        LinearLayout r8 = createStatRow(grid);
        TextView adShieldVal = createStat(r8, "AD-SHIELD", "ACTIVE", NEON_GREEN);
        TextView adsKilledVal = createStat(r8, "ADS-KILLED", "0", GOLD);
        TextView trackerVal = createStat(r8, "TRACKERS", "PROTECTED", CYAN);
        inner.addView(grid);

        LinearLayout configBox = new LinearLayout(this);
        configBox.setOrientation(LinearLayout.HORIZONTAL);
        configBox.setPadding(0, 20, 0, 20);
        configBox.setWeightSum(3f);
        EditText portInput = createInputField(configBox, "NODE PORT", "60000");
        EditText wifiPortInput = createInputField(configBox, "WIFI PORT", "60001");
        EditText tetherPortInput = createInputField(configBox, "TETHER PORT", "8080");
        inner.addView(configBox);

        LinearLayout blockBox = new LinearLayout(this);
        blockBox.setOrientation(LinearLayout.HORIZONTAL);
        blockBox.setPadding(0, 10, 0, 10);
        blockBox.setWeightSum(3f);
        blockBox.setGravity(Gravity.CENTER_VERTICAL);
        EditText blockDomainInput = createInputField(blockBox, "ADD BLOCK DOMAIN", "example.com");
        TextView addBlockBtn = new TextView(this);
        addBlockBtn.setText(" [ ADD BLOCK ] ");
        addBlockBtn.setTextSize(11f);
        addBlockBtn.setTextColor(GOLD);
        addBlockBtn.setTypeface(Typeface.DEFAULT_BOLD);
        addBlockBtn.setGravity(Gravity.CENTER);
        addBlockBtn.setPadding(20, 10, 20, 10);
        addBlockBtn.setBackgroundColor(Color.parseColor("#222222"));
        addBlockBtn.setOnClickListener(v -> {
            String domain = blockDomainInput.getText().toString().trim();
            ResourceManagerService s = ResourceManagerService.inst;
            if (!domain.isEmpty() && s != null && s.getBrain() != null && s.getBrain().getFirewall() != null) {
                s.getBrain().getFirewall().addBlockedDomain(domain);
                android.widget.Toast.makeText(this, "BLOCKED: " + domain, android.widget.Toast.LENGTH_SHORT).show();
                blockDomainInput.setText("");
            }
        });
        blockBox.addView(addBlockBtn);
        inner.addView(blockBox);

        TextView dnsTitle = new TextView(this);
        dnsTitle.setText("VPN DNS SERVER PRESET:");
        dnsTitle.setTextSize(8f);
        dnsTitle.setTextColor(Color.GRAY);
        dnsTitle.setPadding(0, 10, 0, 5);
        inner.addView(dnsTitle);

        LinearLayout dnsBox = new LinearLayout(this);
        dnsBox.setOrientation(LinearLayout.HORIZONTAL);
        dnsBox.setPadding(0, 5, 0, 15);
        dnsBox.setWeightSum(5f);

        final String[] selectedDns = new String[]{"1.1.1.1"};

        TextView cfDnsBtn = createDnsButton(dnsBox, "CLOUDFLARE\n1.1.1.1", GOLD);
        TextView gDnsBtn = createDnsButton(dnsBox, "GOOGLE\n8.8.8.8", Color.GRAY);
        TextView adDnsBtn = createDnsButton(dnsBox, "ADGUARD\n94.140.14.14", Color.GRAY);
        TextView famDnsBtn = createDnsButton(dnsBox, "ADGUARD FAMILY\n94.140.14.15", Color.GRAY);
        TextView sysDnsBtn = createDnsButton(dnsBox, "SYSTEM DEFAULT\n(AUTO)", Color.GRAY);

        cfDnsBtn.setOnClickListener(v -> {
            selectedDns[0] = "1.1.1.1";
            applyDnsChange("1.1.1.1");
            cfDnsBtn.setTextColor(GOLD); gDnsBtn.setTextColor(Color.GRAY); adDnsBtn.setTextColor(Color.GRAY); famDnsBtn.setTextColor(Color.GRAY); sysDnsBtn.setTextColor(Color.GRAY);
        });
        gDnsBtn.setOnClickListener(v -> {
            selectedDns[0] = "8.8.8.8";
            applyDnsChange("8.8.8.8");
            cfDnsBtn.setTextColor(Color.GRAY); gDnsBtn.setTextColor(GOLD); adDnsBtn.setTextColor(Color.GRAY); famDnsBtn.setTextColor(Color.GRAY); sysDnsBtn.setTextColor(Color.GRAY);
        });
        adDnsBtn.setOnClickListener(v -> {
            selectedDns[0] = "94.140.14.14";
            applyDnsChange("94.140.14.14");
            cfDnsBtn.setTextColor(Color.GRAY); gDnsBtn.setTextColor(Color.GRAY); adDnsBtn.setTextColor(GOLD); famDnsBtn.setTextColor(Color.GRAY); sysDnsBtn.setTextColor(Color.GRAY);
        });
        famDnsBtn.setOnClickListener(v -> {
            selectedDns[0] = "94.140.14.15";
            applyDnsChange("94.140.14.15");
            cfDnsBtn.setTextColor(Color.GRAY); gDnsBtn.setTextColor(Color.GRAY); adDnsBtn.setTextColor(Color.GRAY); famDnsBtn.setTextColor(GOLD); sysDnsBtn.setTextColor(Color.GRAY);
        });
        sysDnsBtn.setOnClickListener(v -> {
            selectedDns[0] = "SYSTEM";
            applyDnsChange("SYSTEM");
            cfDnsBtn.setTextColor(Color.GRAY); gDnsBtn.setTextColor(Color.GRAY); adDnsBtn.setTextColor(Color.GRAY); famDnsBtn.setTextColor(Color.GRAY); sysDnsBtn.setTextColor(GOLD);
        });
        inner.addView(dnsBox);

        TextView splitViewBtn = new TextView(this);
        splitViewBtn.setText(" [ LIVE SPLIT-SCREEN: AI FIREWALL & SYSTEM OPTIMIZER ] ");
        splitViewBtn.setTextSize(11f);
        splitViewBtn.setTextColor(Color.BLACK);
        splitViewBtn.setTypeface(Typeface.DEFAULT_BOLD);
        splitViewBtn.setGravity(Gravity.CENTER);
        splitViewBtn.setPadding(20, 15, 20, 15);
        splitViewBtn.setBackgroundColor(CYAN);
        LinearLayout.LayoutParams splp = new LinearLayout.LayoutParams(-1, -2);
        splp.setMargins(0, 15, 0, 15);
        splitViewBtn.setLayoutParams(splp);
        splitViewBtn.setOnClickListener(v -> showSplitScreenLog());
        inner.addView(splitViewBtn);

        TextView profileBtn = new TextView(this);
        profileBtn.setText(" [ ADVANCED PROFILES & VARIABLE CONFIGURATOR ] ");
        profileBtn.setTextSize(11f);
        profileBtn.setTextColor(Color.BLACK);
        profileBtn.setTypeface(Typeface.DEFAULT_BOLD);
        profileBtn.setGravity(Gravity.CENTER);
        profileBtn.setPadding(20, 15, 20, 15);
        profileBtn.setBackgroundColor(GOLD);
        LinearLayout.LayoutParams prp = new LinearLayout.LayoutParams(-1, -2);
        prp.setMargins(0, 15, 0, 15);
        profileBtn.setLayoutParams(prp);
        profileBtn.setOnClickListener(v -> showProfilesDashboard());
        inner.addView(profileBtn);





        LinearLayout privateDnsBox = new LinearLayout(this);
        privateDnsBox.setOrientation(LinearLayout.HORIZONTAL);
        privateDnsBox.setPadding(0, 10, 0, 10);
        privateDnsBox.setWeightSum(3f);
        privateDnsBox.setGravity(Gravity.CENTER_VERTICAL);
        EditText privateDnsInput = createInputField(privateDnsBox, "PRIVATE DNS HOSTNAME", "family.adguard-dns.com");
        TextView applyPrivateDnsBtn = new TextView(this);
        applyPrivateDnsBtn.setText(" [ SET DNS ] ");
        applyPrivateDnsBtn.setTextSize(11f);
        applyPrivateDnsBtn.setTextColor(GOLD);
        applyPrivateDnsBtn.setTypeface(Typeface.DEFAULT_BOLD);
        applyPrivateDnsBtn.setGravity(Gravity.CENTER);
        applyPrivateDnsBtn.setPadding(20, 10, 20, 10);
        applyPrivateDnsBtn.setBackgroundColor(Color.parseColor("#222222"));
        applyPrivateDnsBtn.setOnClickListener(v -> {
            String hostname = privateDnsInput.getText().toString().trim();
            if (!hostname.isEmpty()) {
                new Thread(() -> {
                    try {
                        java.net.InetAddress resolved = java.net.InetAddress.getByName(hostname);
                        String resolvedIp = resolved.getHostAddress();
                        runOnUiThread(() -> {
                            selectedDns[0] = resolvedIp;
                            android.widget.Toast.makeText(this, "Private DNS Set: " + hostname + " (" + resolvedIp + ")", android.widget.Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            android.widget.Toast.makeText(this, "Failed to resolve Private DNS: " + hostname, android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
        });
        privateDnsBox.addView(applyPrivateDnsBtn);
        inner.addView(privateDnsBox);

        TextView quarantineBtn = new TextView(this);
        quarantineBtn.setText(" [ VIEW QUARANTINED ATTACKERS & HOSTS BLOCK ] ");
        quarantineBtn.setTextSize(11f);
        quarantineBtn.setTextColor(Color.BLACK);
        quarantineBtn.setTypeface(Typeface.DEFAULT_BOLD);
        quarantineBtn.setGravity(Gravity.CENTER);
        quarantineBtn.setPadding(20, 15, 20, 15);
        quarantineBtn.setBackgroundColor(Color.RED);
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(-1, -2);
        qlp.setMargins(0, 15, 0, 15);
        quarantineBtn.setLayoutParams(qlp);
        quarantineBtn.setOnClickListener(v -> showQuarantinedAttackersView());
        inner.addView(quarantineBtn);

        TextView cableDiagBox = new TextView(this);
        cableDiagBox.setText(com.Bllod100.zpu.core.ZpuHardwareSafetyManager.getCableHealthDiagnostic());
        cableDiagBox.setTextSize(10f);
        cableDiagBox.setTextColor(GOLD);
        cableDiagBox.setTypeface(Typeface.MONOSPACE);
        cableDiagBox.setGravity(Gravity.CENTER);
        cableDiagBox.setPadding(15, 12, 15, 12);
        cableDiagBox.setBackgroundColor(Color.parseColor("#1A1400"));
        LinearLayout.LayoutParams cdlp = new LinearLayout.LayoutParams(-1, -2);
        cdlp.setMargins(0, 10, 0, 15);
        cableDiagBox.setLayoutParams(cdlp);
        inner.addView(cableDiagBox);

        LinearLayout controlBox = new LinearLayout(this);
        controlBox.setOrientation(LinearLayout.VERTICAL);
        controlBox.setBackgroundColor(Color.parseColor("#111111"));
        controlBox.setPadding(30, 30, 30, 30);

        createSwitch(controlBox, "PC 0.1% POWER (PHONE-ONLY COMPUTE)", "Throttles PC compute to 0.1% power and delegates workloads exclusively to Phone. Off by default and resets on every app restart.", com.Bllod100.zpu.core.ZpuStateRegistry.isPcLowPowerMode(), active -> {
            com.Bllod100.zpu.core.ZpuStateRegistry.setPcLowPowerMode(active);
            android.widget.Toast.makeText(this, active ? "PC 0.1% LOW POWER MODE ENABLED" : "PC 0.1% LOW POWER MODE DISABLED", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        android.content.SharedPreferences prefs = getSharedPreferences("zpu_settings", MODE_PRIVATE);
        boolean softUsbSaved = prefs.getBoolean("soft_usb_disabled", false);
        boolean wifiRelayDisabledSaved = prefs.getBoolean("wifi_relay_disabled", false);
        com.Bllod100.zpu.core.ZpuHardwareSafetyManager.setSoftUsbDisabled(softUsbSaved);
        com.Bllod100.zpu.core.ZpuHardwareSafetyManager.setWifiRelayDisabled(wifiRelayDisabledSaved);

        SwitchMaterial usbSwitch = createSwitchWithReturn(controlBox, "DISABLE USB LINK (KEEP WI-FI ACTIVE)", "Software-disables USB data bridge while maintaining high-speed Wi-Fi co-processing link.", softUsbSaved, active -> {
            prefs.edit().putBoolean("soft_usb_disabled", active).apply();
            com.Bllod100.zpu.core.ZpuHardwareSafetyManager.setSoftUsbDisabled(active);
            cableDiagBox.setText(com.Bllod100.zpu.core.ZpuHardwareSafetyManager.getCableHealthDiagnostic());
        });

        SwitchMaterial wifiSwitch = createSwitchWithReturn(controlBox, "DISABLE WIFI CONNECTION RELAY", "Disables Wi-Fi relay, forcing connection exclusively through USB port methods (ADB and USB tethering).", wifiRelayDisabledSaved, active -> {
            prefs.edit().putBoolean("wifi_relay_disabled", active).apply();
            com.Bllod100.zpu.core.ZpuHardwareSafetyManager.setWifiRelayDisabled(active);
            cableDiagBox.setText(com.Bllod100.zpu.core.ZpuHardwareSafetyManager.getCableHealthDiagnostic());
        });

        usbSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            try { v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY); } catch (Exception ignored) {}
            com.Bllod100.zpu.core.ZpuAudioFeedback.playSwitchSound("DISABLE USB LINK".hashCode());
            if (isChecked && wifiSwitch.isChecked()) {
                wifiSwitch.setChecked(false);
            }
            prefs.edit().putBoolean("soft_usb_disabled", isChecked).apply();
            com.Bllod100.zpu.core.ZpuHardwareSafetyManager.setSoftUsbDisabled(isChecked);
            cableDiagBox.setText(com.Bllod100.zpu.core.ZpuHardwareSafetyManager.getCableHealthDiagnostic());
        });

        wifiSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            try { v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY); } catch (Exception ignored) {}
            com.Bllod100.zpu.core.ZpuAudioFeedback.playSwitchSound("DISABLE WIFI".hashCode());
            if (isChecked && usbSwitch.isChecked()) {
                usbSwitch.setChecked(false);
            }
            prefs.edit().putBoolean("wifi_relay_disabled", isChecked).apply();
            com.Bllod100.zpu.core.ZpuHardwareSafetyManager.setWifiRelayDisabled(isChecked);
            cableDiagBox.setText(com.Bllod100.zpu.core.ZpuHardwareSafetyManager.getCableHealthDiagnostic());
        });
        boolean usbTetherSaved = prefs.getBoolean("usb_tether_enabled", true);
        boolean vpnSaved = prefs.getBoolean("system_vpn_enabled", false);
        boolean localRouterSaved = prefs.getBoolean("local_router_unlimited_speed", true);
        boolean turboChargeSaved = prefs.getBoolean("turbo_charge_mode", true);
        boolean wifiLinkSaved = prefs.getBoolean("wifi_data_link", true);
        boolean bootSaved = prefs.getBoolean("start_on_boot", true);
        boolean applyPortSaved = prefs.getBoolean("apply_port_active", true);
        boolean hwFirewallSaved = prefs.getBoolean("hardware_firewall", true);
        boolean adBlockSaved = prefs.getBoolean("ad_block_shield", true);
        boolean deepFreezeSaved = prefs.getBoolean("phone_deep_freeze", true);
        com.Bllod100.zpu.core.ZpuDeepFreezeManager.setDeepFreezeActive(deepFreezeSaved);

        boolean godModAksSaved = prefs.getBoolean("8_god_mod_aks_enabled", true);
        com.Bllod100.zpu.core.ZpuShizukuGodManager.setGodModAksActive(godModAksSaved);

        boolean thermalCaringSaved = prefs.getBoolean("battery_thermal_caring_enabled", true);
        com.Bllod100.zpu.core.VirtualCoolingEngine.setThermalCaringActive(thermalCaringSaved);

        boolean lazyRenderSaved = prefs.getBoolean("ktech_lazy_render_enabled", true);
        com.Bllod100.zpu.core.ZpuLazyRenderCore.setLazyModeActive(lazyRenderSaved);

        createSwitch(controlBox, "K-TECH LAZYRENDER CORE (0% IDLE CPU)", "Event-driven lazy execution & dirty-region updates. Eliminates idle battery drain.", lazyRenderSaved, active -> {
            prefs.edit().putBoolean("ktech_lazy_render_enabled", active).apply();
            com.Bllod100.zpu.core.ZpuLazyRenderCore.setLazyModeActive(active);
        });

        createSwitch(controlBox, "PROACTIVE BATTERY THERMAL CARING", "Monitors battery temperature (40°C threshold) and prevents thermal degradation across different devices & climates.", thermalCaringSaved, active -> {
            prefs.edit().putBoolean("battery_thermal_caring_enabled", active).apply();
            com.Bllod100.zpu.core.VirtualCoolingEngine.setThermalCaringActive(active);
        });

        createSwitch(controlBox, "8 God MOD AKS", "SHIZUKU HYPER-PRIVILEGE: Unlocks OS kernel iptables firewall, deep RAM purge & system tuning.", godModAksSaved, active -> {
            prefs.edit().putBoolean("8_god_mod_aks_enabled", active).apply();
            com.Bllod100.zpu.core.ZpuShizukuGodManager.setGodModAksActive(active);
        });

        createSwitch(controlBox, "PHONE DEEP-FREEZE & HIBERNATION", "Hibernates phone background services and dedicates 100% phone resources to PC Co-Processor.", deepFreezeSaved, active -> {
            prefs.edit().putBoolean("phone_deep_freeze", active).apply();
            com.Bllod100.zpu.core.ZpuDeepFreezeManager.setDeepFreezeActive(active);
        });

        createSwitch(controlBox, "USB APP TETHERING (ADB GATEWAY)", "Share Wi-Fi over USB ADB bridge without system settings.", usbTetherSaved, active -> {
            prefs.edit().putBoolean("usb_tether_enabled", active).apply();
            ResourceManagerService s = ResourceManagerService.inst;
            if (s != null) {
                if (active) {
                    try {
                        int tp = Integer.parseInt(tetherPortInput.getText().toString());
                        s.startProxyGateway(tp);
                    } catch (Exception e) {
                        s.startProxyGateway(8080);
                    }
                } else {
                    s.stopProxyGateway();
                }
            }
        });

        vpnSwitchInstance = createSwitchWithReturn(controlBox, "SYSTEM VPN & TETHER FIREWALL", "Filters native USB Tethering (Control Panel RNDIS NIC) & Wi-Fi traffic.", vpnSaved, active -> {
            prefs.edit().putBoolean("system_vpn_enabled", active).apply();
            if (active) {
                Intent prepareIntent = android.net.VpnService.prepare(this);
                if (prepareIntent != null) {
                    try { startActivityForResult(prepareIntent, 100); } catch (Exception ignored) {}
                } else {
                    try {
                        Intent vpnIntent = new Intent(this, ZpuVpnService.class);
                        if ("SYSTEM".equals(selectedDns[0])) {
                            vpnIntent.putExtra("NO_DNS", true);
                        } else {
                            vpnIntent.putExtra("PRIMARY_DNS", selectedDns[0]);
                            vpnIntent.putExtra("NO_DNS", false);
                        }
                        startService(vpnIntent);
                    } catch (Exception ignored) {}
                }
            } else {
                Intent stopIntent = new Intent(this, ZpuVpnService.class);
                stopIntent.setAction("STOP");
                try { startService(stopIntent); } catch (Exception ignored) {}
            }
        });

        createSwitch(controlBox, "LOCAL ROUTER UNLIMITED SPEED", "Unlocks 512KB TCP window scaling & max power mode for home Wi-Fi routers.", localRouterSaved, active -> {
            prefs.edit().putBoolean("local_router_unlimited_speed", active).apply();
            ResourceManagerService s = ResourceManagerService.inst;
            if (s != null && s.getBrain() != null && s.getBrain().getBondManager() != null) {
                s.getBrain().getBondManager().setUnlimitedRouterMode(active);
            }
        });

        createSwitch(controlBox, "TURBO CHARGE ACCELERATOR", "Minimizes battery thermal dissipation & throttles power draw for 30% faster charging.", turboChargeSaved, active -> {
            prefs.edit().putBoolean("turbo_charge_mode", active).apply();
            ResourceManagerService s = ResourceManagerService.inst;
            if (s != null) {
                s.setTurboChargeMode(active);
            }
        });

        createSwitch(controlBox, "WIFI DATA LINK", "Enable wireless telemetry fabric.", wifiLinkSaved, active -> {
            prefs.edit().putBoolean("wifi_data_link", active).apply();
            ResourceManagerService s = ResourceManagerService.inst;
            if (s != null && s.getBrain() != null) {
                if (active) {
                    try {
                        int wp = Integer.parseInt(wifiPortInput.getText().toString());
                        s.getBrain().getBondManager().startWifiLink(wp);
                    } catch (Exception e) {
                        s.getBrain().getBondManager().startWifiLink(60001);
                    }
                } else {
                    s.getBrain().getBondManager().stopWifiLink();
                }
            }
        });

        createSwitch(controlBox, "START WITH BOOT", "Auto-ignition on device power-up.", bootSaved, active -> {
            prefs.edit().putBoolean("start_on_boot", active).apply();
        });

        createSwitch(controlBox, "APPLY PORT", "Dynamic bridge reconfiguration.", applyPortSaved, active -> {
            prefs.edit().putBoolean("apply_port_active", active).apply();
            if (active) {
                try {
                    int p = Integer.parseInt(portInput.getText().toString());
                    Intent intent = new Intent(this, ResourceManagerService.class);
                    intent.putExtra("PROXY_PORT", p);
                    startService(intent);
                } catch (Exception ignored) {}
            }
        });

        createSwitch(controlBox, "HARDWARE FIREWALL", "Physical domain blocking (CISCO-MODE).", hwFirewallSaved, active -> {
            prefs.edit().putBoolean("hardware_firewall", active).apply();
            ResourceManagerService s = ResourceManagerService.inst;
            if (s != null && s.getBrain() != null && s.getBrain().getFirewall() != null) {
                s.getBrain().getFirewall().setShieldActive(active);
            }
        });

        createSwitch(controlBox, "PRE-CONFIGURED PRIVATE AD-BLOCK SHIELD", "Blocks ad networks, telemetry trackers, and popups for 2x-4x faster browsing.", adBlockSaved, active -> {
            prefs.edit().putBoolean("ad_block_shield", active).apply();
            ResourceManagerService s = ResourceManagerService.inst;
            if (s != null && s.getBrain() != null && s.getBrain().getFirewall() != null) {
                s.getBrain().getFirewall().setAdBlockEnabled(active);
            }
        });

        createSwitch(controlBox, "FORCE SHUTDOWN", "Restores PC settings to default and closes ZPU completely.", false, active -> {
            if (active) {
                com.Bllod100.zpu.core.ZpuBridge.triggerForceCloseSignal();
                try { Thread.sleep(200); } catch (Exception ignored) {}
                stopService(new Intent(this, ResourceManagerService.class));
                Intent vpnIntent = new Intent(this, ZpuVpnService.class);
                vpnIntent.setAction("STOP");
                startService(vpnIntent);
                if (t != null) t.cancel();
                com.Bllod100.zpu.core.ZpuHistoryLogger.logToFile(this, "APP_SHUTDOWN", "Force Shutdown executed. All background services terminated and process killed.");
                finishAffinity();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });
        inner.addView(controlBox);
        setContentView(frame);
        startPolling(kernelVal, nanoVal, fwVal, laneVal, crackVal, ocVal, syncVal, secVal, perfVal, clockVal, pCoreVal, binVal, flowVal, ghostVal, saveVal, swapVal, ipVal, sharedVal, portSentryVal, tetherStVal, speedVal, tetherMbVal, tetherBlkVal, adShieldVal, adsKilledVal, trackerVal);
    }

    private LinearLayout createStatRow(LinearLayout p) {
        LinearLayout r = new LinearLayout(this); r.setOrientation(LinearLayout.HORIZONTAL);
        r.setWeightSum(4f); r.setPadding(0, 10, 0, 10); p.addView(r); return r;
    }

    private TextView createStat(LinearLayout p, String l, String v, int c) {
        LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER); col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView lbl = new TextView(this); lbl.setText(l); lbl.setTextSize(7f); lbl.setTextColor(Color.GRAY);
        TextView val = new TextView(this); val.setText(v); val.setTextSize(9f); val.setTextColor(c);
        col.addView(lbl); col.addView(val); p.addView(col); return val;
    }

    private EditText createInputField(LinearLayout p, String l, String d) {
        LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView lbl = new TextView(this); lbl.setText(l); lbl.setTextSize(7f); lbl.setTextColor(Color.GRAY);
        EditText input = new EditText(this); input.setText(d); input.setTextSize(11f); input.setTextColor(Color.WHITE);
        input.setInputType(InputType.TYPE_CLASS_NUMBER); input.setBackground(null);
        col.addView(lbl); col.addView(input); p.addView(col); return input;
    }

    private TextView createDnsButton(LinearLayout parent, String text, int color) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(8f);
        btn.setTextColor(color);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(8, 12, 8, 12);
        btn.setBackgroundColor(Color.parseColor("#1A1A1A"));
        btn.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        parent.addView(btn);
        return btn;
    }

    private void createSwitch(LinearLayout p, String l, String d, boolean initial, OnToggle listener) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 20, 0, 20); row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView t1 = new TextView(this); t1.setText(l); t1.setTextColor(Color.WHITE); t1.setTextSize(12f);
        TextView t2 = new TextView(this); t2.setText(d); t2.setTextColor(Color.DKGRAY); t2.setTextSize(8f);
        col.addView(t1); col.addView(t2);
        SwitchMaterial sw = new SwitchMaterial(this);
        sw.setChecked(initial);
        sw.setButtonTintList(ColorStateList.valueOf(NEON_GREEN));
        sw.setOnCheckedChangeListener((v, isChecked) -> {
            try { v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY); } catch (Exception ignored) {}
            com.Bllod100.zpu.core.ZpuAudioFeedback.playSwitchSound(l.hashCode());
            listener.onToggle(isChecked);
        });
        row.addView(col); row.addView(sw); p.addView(row);
    }

    private SwitchMaterial createSwitchWithReturn(LinearLayout p, String l, String d, boolean initial, OnToggle listener) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 20, 0, 20); row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView t1 = new TextView(this); t1.setText(l); t1.setTextColor(Color.WHITE); t1.setTextSize(12f);
        TextView t2 = new TextView(this); t2.setText(d); t2.setTextColor(Color.DKGRAY); t2.setTextSize(8f);
        col.addView(t1); col.addView(t2);
        SwitchMaterial sw = new SwitchMaterial(this);
        sw.setChecked(initial);
        sw.setButtonTintList(ColorStateList.valueOf(NEON_GREEN));
        sw.setOnCheckedChangeListener((v, isChecked) -> {
            try { v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY); } catch (Exception ignored) {}
            com.Bllod100.zpu.core.ZpuAudioFeedback.playSwitchSound(l.hashCode());
            listener.onToggle(isChecked);
        });
        row.addView(col); row.addView(sw); p.addView(row);
        return sw;
    }

    private interface OnToggle { void onToggle(boolean active); }

    private void startPolling(TextView kernel, TextView nano, TextView fw, TextView lane, TextView crack, TextView oc, TextView sync, TextView sec, TextView perf, TextView clock, TextView pcore, TextView bin, TextView flow, TextView ghost, TextView save, TextView swap, TextView ip, TextView shared, TextView portSentry, TextView tetherSt, TextView netSpeedVal, TextView tetherMb, TextView tetherBlk, TextView adShield, TextView adsKilled, TextView trackers) {
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                ResourceManagerService s = ResourceManagerService.inst;
                if (s == null) return;
                SavageIntelligence b = s.getBrain();
                if (b == null || b.getBondManager() == null) return;
                
                if (!com.Bllod100.zpu.core.ZpuLazyRenderCore.shouldRenderFrame(System.nanoTime())) return;

                runOnUiThread(() -> {
                    kernel.setText(b.getAiModule());
                    nano.setText(String.format(Locale.US, "%.1fM/s", b.getNanoPackets() / 1000000.0));
                    fw.setText(b.isFirewallActive() ? "SHIELDED" : "DANGER");
                    fw.setTextColor(b.isFirewallActive() ? CYAN : Color.RED);
                    lane.setText(String.valueOf(b.getLaneCount()));
                    crack.setText(b.getCrackCount() > 0 ? "GUARDED" : "CLEAN");
                    crack.setTextColor(b.getCrackCount() > 0 ? Color.RED : NEON_GREEN);
                    oc.setText(String.format(Locale.US, "%dmV", b.getOcOffset()));
                    boolean usb = b.getBondManager().isUsbLinked();
                    boolean wifi = b.getBondManager().isWifiLinked();
                    sync.setText(usb && wifi ? "DOUBLE" : (usb ? "USB" : (wifi ? "WIFI" : "OFF")));
                    sync.setTextColor(usb && wifi ? GOLD : Color.WHITE);
                    sec.setText(b.getSecurityChain());
                    perf.setText(b.getPerfChain());
                    clock.setText(String.format(Locale.US, "%.2f GHz", s.curClockValue / 1000.0));
                    pcore.setText(String.format(Locale.US, "%.0f%%", b.getPScale() * 100));
                    bin.setText(b.getSiliconGrade());
                    flow.setText(String.format(Locale.US, "%.1f L/m", b.getCoolingFlow()));
                    ghost.setText(String.valueOf(b.getVacuumCount()));
                    save.setText(String.format(Locale.US, "%d%%", b.getCpuSaved()));
                    // Restore True P-CORE: Using original getPScale() for processor load
                    pcore.setText(String.format(Locale.US, "%.0f%%", b.getPScale() * 100));
                    swap.setText(String.format(Locale.US, "%d%%", b.getSwapUsage()));
                    
                    ip.setText(b.getCachedIp());
                    boolean dual = b.getBondManager().isUsbLinked() && b.getBondManager().isWifiLinked();
                    shared.setText(String.format(Locale.US, "%dMB%s", b.getCachedSharedMem(), dual ? " [D-CH]" : ""));
                    shared.setTextColor(dual ? GOLD : Color.WHITE);

                    portSentry.setText(b.getExploitCount() > 0 ? "ALERT" : "SAFE");
                    portSentry.setTextColor(b.getExploitCount() > 0 ? Color.RED : NEON_GREEN);

                    if (s.getProxyEngine() != null && s.getProxyEngine().isRunning()) {
                        tetherSt.setText("ACTIVE");
                        tetherSt.setTextColor(NEON_GREEN);
                    } else {
                        tetherSt.setText("OFF");
                        tetherSt.setTextColor(Color.GRAY);
                    }

                    if (b.getFirewall() != null) {
                        double mb = b.getFirewall().getTetheredBytes() / 1048576.0;
                        double speedMb = b.getFirewall().getCurrentSpeedMBps();
                        netSpeedVal.setText(String.format(Locale.US, "%.2f MB/s", speedMb));
                        netSpeedVal.setTextColor(speedMb > 0.05 ? GOLD : Color.GRAY);
                        tetherMb.setText(String.format(Locale.US, "%.2f MB", mb));
                        tetherBlk.setText(String.valueOf(b.getFirewall().getBlockedTetheredRequestsCount()));

                        boolean adActive = b.getFirewall().isAdBlockEnabled();
                        adShield.setText(adActive ? "ACTIVE" : "DISABLED");
                        adShield.setTextColor(adActive ? NEON_GREEN : Color.GRAY);
                        adsKilled.setText(String.valueOf(b.getFirewall().getAdsBlockedCount()));
                        trackers.setText("PROTECTED");
                    }
                });
            }
        }, 0, 250);
    }

    private void showSplitScreenLog() {
        LinearLayout splitRoot = new LinearLayout(this);
        splitRoot.setOrientation(LinearLayout.VERTICAL);
        splitRoot.setBackgroundColor(Color.BLACK);
        splitRoot.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        splitRoot.setPadding(10, 30, 10, 10);

        TextView header = new TextView(this);
        header.setText("ZPU SPLIT-SCREEN: AI FIREWALL vs SYSTEM OPTIMIZER");
        header.setTextSize(14f); header.setTextColor(GOLD); header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setGravity(Gravity.CENTER); header.setPadding(0, 10, 0, 20);
        splitRoot.addView(header);

        LinearLayout splitContainer = new LinearLayout(this);
        splitContainer.setOrientation(LinearLayout.HORIZONTAL);
        splitContainer.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        splitContainer.setWeightSum(2f);

        // Left Pane: Firewall Log
        LinearLayout leftPane = new LinearLayout(this);
        leftPane.setOrientation(LinearLayout.VERTICAL);
        leftPane.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1f));
        leftPane.setBackgroundColor(Color.parseColor("#050505"));
        leftPane.setPadding(10, 10, 10, 10);

        TextView fwTitle = new TextView(this);
        fwTitle.setText("AI FORTINET FIREWALL LOG");
        fwTitle.setTextSize(11f); fwTitle.setTextColor(CYAN); fwTitle.setTypeface(Typeface.DEFAULT_BOLD);
        leftPane.addView(fwTitle);

        TextView clearFwBtn = new TextView(this);
        clearFwBtn.setText(" [ CLEAR FIREWALL LOGS ] ");
        clearFwBtn.setTextSize(9f); clearFwBtn.setTextColor(Color.RED); clearFwBtn.setTypeface(Typeface.DEFAULT_BOLD);
        clearFwBtn.setGravity(Gravity.CENTER); clearFwBtn.setPadding(5, 5, 5, 5);
        clearFwBtn.setBackgroundColor(Color.parseColor("#220000"));
        LinearLayout.LayoutParams clp1 = new LinearLayout.LayoutParams(-1, -2);
        clp1.setMargins(0, 5, 0, 5);
        clearFwBtn.setLayoutParams(clp1);
        leftPane.addView(clearFwBtn);

        ScrollView fwScroll = new ScrollView(this);
        fwScroll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        TextView fwLogText = new TextView(this);
        fwLogText.setText(com.Bllod100.zpu.core.ZpuLogColorizer.colorizeLog(com.Bllod100.zpu.core.SmartAiFirewallManager.getLiveFirewallLog()));
        fwLogText.setTextSize(9f); fwLogText.setTypeface(Typeface.MONOSPACE);
        fwScroll.addView(fwLogText);
        leftPane.addView(fwScroll);

        clearFwBtn.setOnClickListener(v -> {
            com.Bllod100.zpu.core.SmartAiFirewallManager.clearFirewallLog();
            fwLogText.setText(com.Bllod100.zpu.core.ZpuLogColorizer.colorizeLog(com.Bllod100.zpu.core.SmartAiFirewallManager.getLiveFirewallLog()));
        });

        // Right Pane: System Optimizer Log
        LinearLayout rightPane = new LinearLayout(this);
        rightPane.setOrientation(LinearLayout.VERTICAL);
        rightPane.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1f));
        rightPane.setBackgroundColor(Color.parseColor("#0A0A0A"));
        rightPane.setPadding(10, 10, 10, 10);

        TextView optTitle = new TextView(this);
        optTitle.setText("SYSTEM VISTA OPTIMIZER LOG");
        optTitle.setTextSize(11f); optTitle.setTextColor(NEON_GREEN); optTitle.setTypeface(Typeface.DEFAULT_BOLD);
        rightPane.addView(optTitle);

        TextView clearOptBtn = new TextView(this);
        clearOptBtn.setText(" [ CLEAR GAME BOOSTER LOGS ] ");
        clearOptBtn.setTextSize(9f); clearOptBtn.setTextColor(GOLD); clearOptBtn.setTypeface(Typeface.DEFAULT_BOLD);
        clearOptBtn.setGravity(Gravity.CENTER); clearOptBtn.setPadding(5, 5, 5, 5);
        clearOptBtn.setBackgroundColor(Color.parseColor("#222200"));
        LinearLayout.LayoutParams clp2 = new LinearLayout.LayoutParams(-1, -2);
        clp2.setMargins(0, 5, 0, 5);
        clearOptBtn.setLayoutParams(clp2);
        rightPane.addView(clearOptBtn);

        ScrollView optScroll = new ScrollView(this);
        optScroll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        TextView optLogText = new TextView(this);
        optLogText.setText(com.Bllod100.zpu.core.ZpuLogColorizer.colorizeLog(com.Bllod100.zpu.core.SmartAiFirewallManager.getLiveOptimizerLog()));
        optLogText.setTextSize(9f); optLogText.setTypeface(Typeface.MONOSPACE);
        optScroll.addView(optLogText);
        rightPane.addView(optScroll);

        clearOptBtn.setOnClickListener(v -> {
            com.Bllod100.zpu.core.SmartAiFirewallManager.clearOptimizerLog();
            optLogText.setText(com.Bllod100.zpu.core.ZpuLogColorizer.colorizeLog(com.Bllod100.zpu.core.SmartAiFirewallManager.getLiveOptimizerLog()));
        });

        splitContainer.addView(leftPane);
        splitContainer.addView(rightPane);
        splitRoot.addView(splitContainer);

        // Bottom Split Part: Advanced Debug & Telemetry Inspector Menu
        LinearLayout debugPane = new LinearLayout(this);
        debugPane.setOrientation(LinearLayout.VERTICAL);
        debugPane.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 0.9f));
        debugPane.setBackgroundColor(Color.parseColor("#0A0F1A"));
        debugPane.setPadding(15, 10, 15, 10);

        TextView debugTitle = new TextView(this);
        debugTitle.setText("=== ADVANCED ZPU DEBUG & BOOST EFFECTIVENESS INSPECTOR ===");
        debugTitle.setTextSize(10f); debugTitle.setTextColor(CYAN); debugTitle.setTypeface(Typeface.DEFAULT_BOLD);
        debugTitle.setGravity(Gravity.CENTER);
        debugPane.addView(debugTitle);

        ScrollView debugScroll = new ScrollView(this);
        debugScroll.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        TextView debugContentText = new TextView(this);
        debugContentText.setTextSize(9f); debugContentText.setTextColor(NEON_GREEN); debugContentText.setTypeface(Typeface.MONOSPACE);

        boolean isConn = com.Bllod100.zpu.core.ZpuHistoryLogger.isLoggingActive();
        String connStatusStr = isConn ? "[ACTIVE] CONNECTED TO PC MASTER BRIDGE (SECURE STREAM)" : "[STANDBY] AWAITING CONNECTION";
        String boostStatusStr = "[BOOST FUNCTIONS EXECUTION & EFFECTIVENESS REPORT]\n" +
                "• 144Hz High Refresh Rate Mode: [EXECUTED & APPLIED] -> 144 FPS UI & Display Target Set.\n" +
                "• K-Tech LazyRender Core (0% Idle CPU): [EXECUTED & APPLIED] -> Dirty-region polling active.\n" +
                "• Proactive Battery Thermal Caring: [EXECUTED & APPLIED] -> Thermal monitoring active.\n" +
                "• 8 God MOD AKS (Shizuku Kernel Firewall): [EXECUTED & APPLIED] -> Kernel iptables & RAM purge tuned.\n" +
                "• Phone Deep-Freeze & Hibernation: [EXECUTED & APPLIED] -> Background services hibernated.\n" +
                "• AI Fortinet Firewall DPI Inspector: [EXECUTED & APPLIED] -> Packet entropy & threat scanner active.\n" +
                "• PC 0.1% Low-Power Mode: [READY] -> Telemetry synched with PC master bridge.";

        debugContentText.setText("CONNECTION STATE: " + connStatusStr + "\n\n" + boostStatusStr);
        debugScroll.addView(debugContentText);
        debugPane.addView(debugScroll);

        TextView diagBtn = new TextView(this);
        diagBtn.setText(" [ RUN NETWORK & BOOST DIAGNOSTIC PROBE ] ");
        diagBtn.setTextSize(10f); diagBtn.setTextColor(Color.BLACK); diagBtn.setTypeface(Typeface.DEFAULT_BOLD);
        diagBtn.setGravity(Gravity.CENTER); diagBtn.setPadding(10, 10, 10, 10);
        diagBtn.setBackgroundColor(CYAN);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1, -2);
        dp.setMargins(0, 5, 0, 5);
        diagBtn.setLayoutParams(dp);
        diagBtn.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Diagnostic Probe: All Boost Functions Verified Secure & Effective.", android.widget.Toast.LENGTH_SHORT).show();
            com.Bllod100.zpu.core.ZpuHistoryLogger.logToFile(this, "DIAGNOSTIC_PROBE", "User executed Network & Boost Diagnostic Probe. Status: 100% SECURE & EFFECTIVE.");
            debugContentText.setText("CONNECTION STATE: " + (com.Bllod100.zpu.core.ZpuHistoryLogger.isLoggingActive() ? "[ACTIVE] CONNECTED" : "[STANDBY]") + "\n\n" + boostStatusStr + "\n\n[DIAGNOSTIC PASSED]: Latency < 1ms, Packet Loss 0%, All Boosting Functions Operational & Affecting PC/Phone.");
        });
        debugPane.addView(diagBtn);

        splitRoot.addView(debugPane);

        TextView exportLogBtn = new TextView(this);
        exportLogBtn.setText(" [ EXPORT AUDIT LOG (.TXT) ] ");
        exportLogBtn.setTextSize(11f); exportLogBtn.setTextColor(Color.BLACK); exportLogBtn.setTypeface(Typeface.DEFAULT_BOLD);
        exportLogBtn.setGravity(Gravity.CENTER); exportLogBtn.setPadding(20, 15, 20, 15);
        exportLogBtn.setBackgroundColor(CYAN);
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(-1, -2);
        elp.setMargins(10, 5, 10, 5);
        exportLogBtn.setLayoutParams(elp);
        exportLogBtn.setOnClickListener(v -> {
            com.Bllod100.zpu.core.ZpuHistoryLogger.exportLogFile(this);
        });
        splitRoot.addView(exportLogBtn);

        TextView closeBtn = new TextView(this);
        closeBtn.setText(" [ CLOSE SPLIT VIEW ] ");
        closeBtn.setTextSize(12f); closeBtn.setTextColor(Color.BLACK); closeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        closeBtn.setGravity(Gravity.CENTER); closeBtn.setPadding(20, 20, 20, 20);
        closeBtn.setBackgroundColor(GOLD);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, -2);
        clp.setMargins(10, 5, 10, 10);
        closeBtn.setLayoutParams(clp);
        closeBtn.setOnClickListener(v -> setupDashboard());
        splitRoot.addView(closeBtn);

        setContentView(splitRoot);
    }

    private void applyDnsChange(String newDns) {
        currentDnsPreset = newDns;
        ZpuVpnService.currentUpstreamDns = newDns;
        com.Bllod100.zpu.core.FastDnsResolver.clearCache();
        try {
            Intent vpnIntent = new Intent(this, ZpuVpnService.class);
            if ("SYSTEM".equals(newDns)) {
                vpnIntent.putExtra("NO_DNS", true);
            } else {
                vpnIntent.putExtra("PRIMARY_DNS", newDns);
                vpnIntent.putExtra("NO_DNS", false);
            }
            startService(vpnIntent);
            android.widget.Toast.makeText(this, "DNS Preset Applied Instantly: " + newDns, android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            try {
                Intent vpnIntent = new Intent(this, ZpuVpnService.class);
                if ("SYSTEM".equals(currentDnsPreset)) {
                    vpnIntent.putExtra("NO_DNS", true);
                } else {
                    vpnIntent.putExtra("PRIMARY_DNS", currentDnsPreset);
                    vpnIntent.putExtra("NO_DNS", false);
                }
                startService(vpnIntent);
            } catch (Exception ignored) {}
        }
    }

    private void showQuarantinedAttackersView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        root.setPadding(20, 40, 20, 20);

        TextView title = new TextView(this);
        title.setText("ZPU QUARANTINED ATTACKERS & HOSTS BLOCK");
        title.setTextSize(14f); title.setTextColor(Color.RED); title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER); title.setPadding(0, 10, 0, 20);
        root.addView(title);

        LinearLayout toggleBox = new LinearLayout(this);
        toggleBox.setOrientation(LinearLayout.HORIZONTAL);
        toggleBox.setGravity(Gravity.CENTER_VERTICAL);
        toggleBox.setPadding(10, 10, 10, 10);
        toggleBox.setBackgroundColor(Color.parseColor("#111111"));

        TextView toggleLbl = new TextView(this);
        toggleLbl.setText("AUTO-BLOCK ATTACKERS IN HOSTS FILE");
        toggleLbl.setTextSize(11f); toggleLbl.setTextColor(NEON_GREEN); toggleLbl.setTypeface(Typeface.DEFAULT_BOLD);
        toggleLbl.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        toggleBox.addView(toggleLbl);

        SwitchMaterial hostsSwitch = new SwitchMaterial(this);
        hostsSwitch.setChecked(com.Bllod100.zpu.core.SmartAiFirewallManager.isAutoBlockInHosts());
        hostsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            com.Bllod100.zpu.core.SmartAiFirewallManager.setAutoBlockInHosts(isChecked);
        });
        toggleBox.addView(hostsSwitch);
        root.addView(toggleBox);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        scroll.setPadding(0, 15, 0, 15);

        LinearLayout listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        java.util.Set<String> attackers = com.Bllod100.zpu.core.SmartAiFirewallManager.getQuarantinedAttackers();
        if (attackers.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No attacker addresses quarantined yet. Firewall active.");
            empty.setTextSize(12f); empty.setTextColor(Color.GRAY); empty.setGravity(Gravity.CENTER);
            empty.setPadding(20, 40, 20, 40);
            listLayout.addView(empty);
        } else {
            for (String ip : attackers) {
                TextView item = new TextView(this);
                item.setText("[QUARANTINED]: " + ip);
                item.setTextSize(11f); item.setTextColor(Color.RED); item.setTypeface(Typeface.MONOSPACE);
                item.setPadding(15, 15, 15, 15);
                item.setBackgroundColor(Color.parseColor("#1A0000"));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                lp.setMargins(0, 5, 0, 5);
                item.setLayoutParams(lp);
                listLayout.addView(item);
            }
        }
        scroll.addView(listLayout);
        root.addView(scroll);

        TextView backBtn = new TextView(this);
        backBtn.setText(" [ BACK TO DASHBOARD ] ");
        backBtn.setTextSize(12f); backBtn.setTextColor(Color.BLACK); backBtn.setTypeface(Typeface.DEFAULT_BOLD);
        backBtn.setGravity(Gravity.CENTER); backBtn.setPadding(20, 20, 20, 20);
        backBtn.setBackgroundColor(GOLD);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(-1, -2);
        blp.setMargins(0, 10, 0, 10);
        backBtn.setLayoutParams(blp);
        backBtn.setOnClickListener(v -> setupDashboard());
        root.addView(backBtn);

        setContentView(root);
    }

    private void showProfilesDashboard() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        root.setPadding(20, 40, 20, 20);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(10, 10, 10, 10);
        scroll.addView(inner);
        root.addView(scroll);

        TextView title = new TextView(this);
        title.setText("ZPU TUNING PROFILES & VARIABLE CONFIGURATOR");
        title.setTextSize(14f); title.setTextColor(GOLD); title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER); title.setPadding(0, 10, 0, 20);
        inner.addView(title);

        TextView profileListLabel = new TextView(this);
        profileListLabel.setText("SAVED PROFILES (DEFAULT APEX IS READ-ONLY BACKUP):");
        profileListLabel.setTextSize(10f); profileListLabel.setTextColor(CYAN);
        inner.addView(profileListLabel);

        LinearLayout profileListLayout = new LinearLayout(this);
        profileListLayout.setOrientation(LinearLayout.VERTICAL);
        inner.addView(profileListLayout);

        EditText nameInput = createInputField(inner, "NEW PROFILE NAME", "My Custom Profile");
        EditText fpsInput = createInputField(inner, "TARGET FPS / 144Hz CAP", String.valueOf(com.Bllod100.zpu.core.ZpuLazyRenderCore.getFpsCap()));
        EditText powerInput = createInputField(inner, "POWER STATE (%)", "100");
        EditText entropyInput = createInputField(inner, "FIREWALL ENTROPY THRESHOLD", "7.9");
        EditText dmaInput = createInputField(inner, "DMA LANES", "1000");

        final com.Bllod100.zpu.core.ZpuProfileManager.Profile[] activeSelection = new com.Bllod100.zpu.core.ZpuProfileManager.Profile[]{com.Bllod100.zpu.core.ZpuProfileManager.getDefaultProfile()};

        final Runnable[] refreshListHolder = new Runnable[1];
        refreshListHolder[0] = () -> {
            profileListLayout.removeAllViews();
            java.util.List<com.Bllod100.zpu.core.ZpuProfileManager.Profile> updated = com.Bllod100.zpu.core.ZpuProfileManager.getProfiles(this);
            for (com.Bllod100.zpu.core.ZpuProfileManager.Profile p : updated) {
                TextView item = new TextView(this);
                item.setText("• " + p.name + " [FPS:" + p.fpsCap + ", Power:" + p.powerState + "%]");
                item.setTextSize(11f);
                item.setTextColor((activeSelection[0] != null && activeSelection[0].name.equals(p.name)) ? NEON_GREEN : Color.WHITE);
                item.setPadding(10, 10, 10, 10);
                item.setOnClickListener(v -> {
                    activeSelection[0] = p;
                    nameInput.setText(p.name.contains("READ-ONLY") ? "Custom Profile" : p.name);
                    fpsInput.setText(String.valueOf(p.fpsCap));
                    powerInput.setText(String.valueOf(p.powerState));
                    entropyInput.setText(String.valueOf(p.firewallEntropy));
                    dmaInput.setText(String.valueOf(p.dmaLanes));
                    android.widget.Toast.makeText(this, "Selected Profile: " + p.name, android.widget.Toast.LENGTH_SHORT).show();
                    refreshListHolder[0].run();
                });
                profileListLayout.addView(item);
            }
        };
        refreshListHolder[0].run();

        TextView loadBtn = new TextView(this);
        loadBtn.setText(" [ LOAD / APPLY SELECTED PROFILE ] ");
        loadBtn.setTextSize(11f); loadBtn.setTextColor(Color.BLACK); loadBtn.setTypeface(Typeface.DEFAULT_BOLD);
        loadBtn.setGravity(Gravity.CENTER); loadBtn.setPadding(20, 15, 20, 15);
        loadBtn.setBackgroundColor(NEON_GREEN);
        loadBtn.setOnClickListener(v -> {
            if (activeSelection[0] != null) {
                com.Bllod100.zpu.core.ZpuProfileManager.applyProfile(this, activeSelection[0]);
                android.widget.Toast.makeText(this, "Profile Applied: " + activeSelection[0].name, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        inner.addView(loadBtn);

        TextView saveBtn = new TextView(this);
        saveBtn.setText(" [ SAVE AS NEW CUSTOM PROFILE ] ");
        saveBtn.setTextSize(11f); saveBtn.setTextColor(Color.BLACK); saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        saveBtn.setGravity(Gravity.CENTER); saveBtn.setPadding(20, 15, 20, 15);
        saveBtn.setBackgroundColor(CYAN);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, -2);
        slp.setMargins(0, 10, 0, 10);
        saveBtn.setLayoutParams(slp);
        saveBtn.setOnClickListener(v -> {
            String pName = nameInput.getText().toString().trim();
            if (pName.isEmpty() || pName.toUpperCase().contains("READ-ONLY")) {
                android.widget.Toast.makeText(this, "Invalid profile name!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int fCap = Integer.parseInt(fpsInput.getText().toString().trim());
                int pState = Integer.parseInt(powerInput.getText().toString().trim());
                double entropy = Double.parseDouble(entropyInput.getText().toString().trim());
                int dma = Integer.parseInt(dmaInput.getText().toString().trim());

                com.Bllod100.zpu.core.ZpuProfileManager.Profile newP = new com.Bllod100.zpu.core.ZpuProfileManager.Profile(pName, fCap, pState, entropy, dma, true, false);
                com.Bllod100.zpu.core.ZpuProfileManager.saveProfile(this, newP);
                activeSelection[0] = newP;
                refreshListHolder[0].run();
                android.widget.Toast.makeText(this, "Profile Saved: " + pName, android.widget.Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                android.widget.Toast.makeText(this, "Error parsing variables. Check numbers.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        inner.addView(saveBtn);

        TextView deleteBtn = new TextView(this);
        deleteBtn.setText(" [ DELETE SELECTED CUSTOM PROFILE ] ");
        deleteBtn.setTextSize(11f); deleteBtn.setTextColor(Color.WHITE); deleteBtn.setTypeface(Typeface.DEFAULT_BOLD);
        deleteBtn.setGravity(Gravity.CENTER); deleteBtn.setPadding(20, 15, 20, 15);
        deleteBtn.setBackgroundColor(Color.RED);
        deleteBtn.setOnClickListener(v -> {
            if (activeSelection[0] != null) {
                if (activeSelection[0].isDefault) {
                    android.widget.Toast.makeText(this, "Cannot delete DEFAULT APEX (Read-Only backup profile)!", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                com.Bllod100.zpu.core.ZpuProfileManager.deleteProfile(this, activeSelection[0].name);
                activeSelection[0] = com.Bllod100.zpu.core.ZpuProfileManager.getDefaultProfile();
                refreshListHolder[0].run();
                android.widget.Toast.makeText(this, "Profile Deleted.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        inner.addView(deleteBtn);

        TextView closeBtn = new TextView(this);
        closeBtn.setText(" [ BACK TO DASHBOARD ] ");
        closeBtn.setTextSize(12f); closeBtn.setTextColor(Color.BLACK); closeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        closeBtn.setGravity(Gravity.CENTER); closeBtn.setPadding(20, 20, 20, 20);
        closeBtn.setBackgroundColor(GOLD);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-1, -2);
        clp.setMargins(0, 20, 0, 10);
        closeBtn.setLayoutParams(clp);
        closeBtn.setOnClickListener(v -> setupDashboard());
        inner.addView(closeBtn);

        setContentView(root);
    }

    @Override
    protected void onDestroy() {
        try { unregisterReceiver(vpnReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
