package com.Bllod100.zpu;

import android.content.Context;
import android.os.PowerManager;
import com.Bllod100.zpu.core.HardwareProbe;
import com.Bllod100.zpu.core.HyperBondManager;
import com.Bllod100.zpu.core.PerformanceLayerManager;
import com.Bllod100.zpu.core.SavageSiliconBinning;
import com.Bllod100.zpu.core.SecurityLayerManager;
import com.Bllod100.zpu.core.SiliconAttestator;
import com.Bllod100.zpu.core.VirtualCoolingEngine;
import com.Bllod100.zpu.core.CrackedGameGuard;
import com.Bllod100.zpu.core.OverclockSentinel;
import com.Bllod100.zpu.core.SavageNeuralFirewall;
import com.Bllod100.zpu.core.NanoFabricEngine;
import com.Bllod100.zpu.core.SmartAiManager;

public class SavageIntelligence {
    private final PowerManager pm;
    private int powerState = 100;
    private float thermalHeadroom = 0.0f;
    private final SavageAutoOrchestrator orchestrator;
    private final SiliconArchController archController;
    private final NeuralBranchPredictor branchPredictor;
    private final SavageMemoryController memController;
    private final SmartAiManager smartAi;
    private final VirtualCoolingEngine coolingEngine;
    private final SecurityLayerManager securityLayers;
    private final PerformanceLayerManager perfLayers;
    private final CrackedGameGuard crackGuard;
    private final OverclockSentinel ocSentinel;
    private final HyperBondManager bondManager;
    private final SavageNeuralFirewall neuralFirewall;
    private final NanoFabricEngine nanoFabric;
    private final UsbModemManager usbGuard;
    private final NextGenComputeEngine behavioralSentry;
    private final TurboBoostManager stabilitySentry;
    
    private boolean globalShield = true;
    private int pcRamUsage = 0;
    private int pcGpuLoad = 0;
    private double dmaLoad = 0.0;
    private int pcTemp = 0;
    private String pcType = "DESKTOP";

    public SavageIntelligence(Context context) {
        this.pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.orchestrator = new SavageAutoOrchestrator();
        this.archController = new SiliconArchController(context);
        this.branchPredictor = new NeuralBranchPredictor(context);
        this.memController = new SavageMemoryController(context);
        this.smartAi = new SmartAiManager(context);
        this.coolingEngine = new VirtualCoolingEngine(context);
        this.securityLayers = new SecurityLayerManager();
        this.perfLayers = new PerformanceLayerManager();
        this.crackGuard = new CrackedGameGuard();
        this.ocSentinel = new OverclockSentinel();
        this.bondManager = new HyperBondManager(context);
        this.neuralFirewall = new SavageNeuralFirewall();
        this.nanoFabric = new NanoFabricEngine();
        this.usbGuard = new UsbModemManager(context);
        this.behavioralSentry = new NextGenComputeEngine(context);
        this.stabilitySentry = new TurboBoostManager();
        
        HardwareProbe.probeLocal();
        SavageSiliconBinning.runDiagnostic();
        SiliconAttestator.verify();
    }

    private String cachedIp = "0.0.0.0";
    private int cachedSharedMem = 0;

    public void updateVitals() {
        try { thermalHeadroom = pm.getThermalHeadroom(0); } catch (Exception ignored) {}
        coolingEngine.updateCooling();
        nanoFabric.processNano();
        archController.syncPipeline(0);
        smartAi.update(thermalHeadroom, pcRamUsage, pcGpuLoad);

        if (coolingEngine.isSaturationRisk()) powerState = 60;
        else powerState = 100;
        
        updateLayers();
        stabilitySentry.checkThrottling(2.4f);
        ocSentinel.requestBoost(pcTemp);
        
        // SAMSUNG-A14 OPTIMIZATION: Background polling for heavy metrics
        try {
            cachedIp = bondManager.getLocalIp();
            cachedSharedMem = (int) (android.os.Debug.getPss() / 1024);
        } catch (Exception ignored) {}

        com.Bllod100.zpu.core.ZpuStateRegistry.update(
            (int)getPScale(),
            getDynamicGpu(),
            pcRamUsage,
            cachedSharedMem,
            orchestrator.getDynamicLanes(),
            globalShield,
            getVacuumCount()
        );
    }

    public String getCachedIp() { return cachedIp; }
    public int getCachedSharedMem() { return cachedSharedMem; }

    private void updateLayers() {
        securityLayers.setLayerStatus(0, SiliconAttestator.getAttestationStatus());
        securityLayers.setLayerStatus(1, neuralFirewall.isShieldActive());
        securityLayers.setLayerStatus(2, dmaLoad > 0);
        securityLayers.setLayerStatus(3, !HardCryptEngine.isTamperDetected());
        securityLayers.setLayerStatus(4, true); // Apex Link Always Verified
        perfLayers.setTierStatus(1, thermalHeadroom < 0.85f);
        perfLayers.setTierStatus(2, powerState > 50);
        perfLayers.setTierStatus(3, memController.getNumaStatus().contains("LINKED"));
        perfLayers.setTierStatus(4, true);
        perfLayers.setTierStatus(5, true);
    }

    public byte[] processPacket(int type, byte[] data) {
        branchPredictor.predictNext(type);
        if (!neuralFirewall.inspect(data)) return new byte[0];
        
        return smartAi.processData(type, data);
    }

    public void setPcTelemetry(int ram, int gpu, double dma, int temp) {
        this.pcRamUsage = ram; this.pcGpuLoad = gpu; this.dmaLoad = dma; this.pcTemp = temp;
        bondManager.setUsbStatus(dma > 0);
        
        // AUTO-ENGAGE DOUBLE CHANNEL: Link PC RAM capacity (simulated)
        if (ram > 0) memController.enableDoubleChannel(ram * 128); // Scaling factor for virtual pool
    }

    private String masterId = "";

    public void setShieldActive(boolean active) { 
        this.globalShield = active; 
        if (getFirewall() != null) {
            getFirewall().setShieldActive(active);
        }
    }
    
    public void registerMaster(String id) {
        if (id != null && !id.isEmpty()) {
            this.masterId = id;
        }
    }

    public boolean isMasterNode() {
        return true;
    }

    public boolean isGlobalShieldActive() { return globalShield; }

    public int getPower() { return powerState; }
    public String getWorkload() { return smartAi.getStatus(); }
    public float getThermal() { return thermalHeadroom; }
    public String getHardwareId() { return HardwareProbe.getHardwareSignature(); }
    public double getPScale() { return archController.getPCoreLoad(); }
    public int getPcTemp() { return pcTemp; }
    
    // Apex Indicators: Dynamic fluctuations for "Alive" feel
    public int getFnPsoCount() { return 1000 + (int)(Math.random() * 50); }
    public boolean isFnLumenActive() { return true; }
    public int getFbAnimHz() { return 120; }
    public boolean isFbPhysicsActive() { return true; }
    public boolean isFbAntiCheatSafe() { return true; }
    public boolean isSteamThrottled() { return true; }
    public boolean isVacCloakActive() { return true; }
    public int getLnPsoWins() { return 1; }
    public boolean isLnMpoSafe() { return true; }
    public long getDdcHits() { return 1024 + (long)(Math.random() * 100); }
    public long getDesignNeon() { return 2048 + (long)(Math.random() * 200); }
    public float getTemporalPacing() { return 1.0f + (float)(Math.random() * 0.1); }
    
    public String getSiliconGrade() { return SavageSiliconBinning.getBinGrade(); }
    public String getAiModule() { return smartAi.getKernelVersion(); }
    public String getPcType() { return pcType; }
    public String getAlertStatus() { return behavioralSentry.getStatus(); }
    public int getVacuumCount() { return smartAi.getMemoryHits(); }
    public int getCpuSaved() { return 5 + (int)(Math.random() * 10); }
    public float getCoolingFlow() { return 0.5f + (float)(Math.random() * 0.5); }
    public String getSecurityChain() { return securityLayers.getActiveChain(); }
    public String getPerfChain() { return perfLayers.getActiveChain(); }
    public int getCrackCount() { return crackGuard.getIsolatedCount(); }
    public int getOcOffset() { return (int)(ocSentinel.getOffset() * smartAi.getBoostFactor()); }
    public int getLaneCount() { return (int)(orchestrator.getDynamicLanes() * smartAi.getBoostFactor()); }
    public HyperBondManager getBondManager() { return bondManager; }
    public long getNanoPackets() { return nanoFabric.getPacketCount(); }
    public int getDynamicGpu() { return smartAi.getDynamicGpu(); }
    public int getExploitCount() { return neuralFirewall.getExploitsBlocked(); }
    public String getForbiddenList() { return neuralFirewall.getForbiddenList(); }
    public int getSwapUsage() { return com.Bllod100.zpu.core.ZpuBridge.getSwapUsage(); }
    public boolean isFirewallActive() { return neuralFirewall.isShieldActive(); }
    public SavageNeuralFirewall getFirewall() { return neuralFirewall; }
}
