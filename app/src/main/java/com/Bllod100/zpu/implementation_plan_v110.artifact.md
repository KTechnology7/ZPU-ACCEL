# Zpu Resource Accelerator (v110.0 - Cross-Over & Guardian-Sync) Implementation Plan

The "Total Hardware Defense" milestone. This version unifies the **AI Crossover Engine** with a new **Guardian-Sync** protocol designed to protect your PC and Phone from compromised or cracked game binaries.

## User Review Required

> [!IMPORTANT]
> **AI Crossover Engine (Logic-Smart):**
> - **Cross-Profiling:** If the AI detects that a game (e.g., Resident Evil) runs better with the "Football Physics" logic, it will automatically hot-swap the internal weights for better performance.
> - **Performance Crossover:** The co-processor tests different logic combinations during the first 100 seconds of gameplay to find the "Winning Profile."
>
> **Guardian-Sync (Anti-Malware Isolation):**
> - **Paranoid Mode:** When the Master Bridge detects a "Cracked" or "Unsigned" game binary, it automatically engages **Paranoid Mode**. This puts the co-processor link into a strict one-way jail.
> - **Firewall Lockdown:** The PC script now creates a temporary Windows Firewall rule to block ALL internet IPs from touching the co-processor ports (59900-60155), ensuring only the phone can communicate.
> - **Registry Fortress:** Zpu's PC identity keys are locked with a "System-Only" attribute while the game is running, preventing malware from modifying your driver spoof.
>
> **0-Comment Enforcement:**
> - Absolute code purity for maximum execution speed and logic masking.

## Proposed Changes

### 1. Guardian-Sync & Crossover Logic (Java)

#### [MODIFY] [SavageAutoOrchestrator.java](file:///C:/android/zpu/SavageAutoOrchestrator.java)
- **Neural Crossover:** Implements the memory-based "Winning Profile" selector.
- **Threat-Level Scaling:** Dynamically increases encryption rounds if the PC signals a "Paranoid Mode" event.

#### [MODIFY] [MainActivity.java](file:///C:/android/zpu/MainActivity.java)
- **Guardian HUD:**
    - **GUARD-ST:** Status of the anti-malware isolation (SECURE).
    - **FW-LOCK:** Status of the port-firewall rule.
    - **AI-TEST:** Name of the current profile being tested (e.g., FN-CROSS).

### 2. PC Bridge Hardening

#### [MODIFY] [ZPU_MASTER_BRIDGE.ps1](file:///C:/Users/User\AndroidStudioProjects\Zpu\ZPU_MASTER_BRIDGE.ps1)
- **Signature Verifier:** Checks if the active game process is digitally signed.
- **Firewall Injector:** Automatically adds/removes Windows Firewall rules for the 256-lane bus.
- **Malware Sentry:** Scans for known malicious process names during gameplay.

## Verification Plan
- **Malware Test:** Launch an unsigned executable and verify the phone HUD switches to **PARANOID_MODE** with 2x encryption.
- **Firewall Test:** Attempt to ping the co-processor port from a local device and verify the connection is blocked.
- **Crossover Test:** Run Resident Evil and watch the **AI-TEST** gauge switch between profiles until a "Winner" is locked.
