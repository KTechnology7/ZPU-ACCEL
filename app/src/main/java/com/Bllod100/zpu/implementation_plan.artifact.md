# Zpu Resource Accelerator (v420.2 - FSG University Edition & Bridge Purge) Implementation Plan

The "Identity & Integrity" update. This version honors the **FSG University** name, fixes the UI switch "bounce-back" bug, and implements the **Scorched Earth** port purge to fix the ADB daemon failure.

## User Review Required

> [!IMPORTANT]
> **FSG Identity Update:**
> - All UI strings now reflect **"ZPU @ FSG UNIVERSITY"**. "FSG" is treated as the project's home institution, not a setting.
>
> **UI Logic Fix (Switch-Back Bug):**
> - The **"APPLY CONFIG"** switch now uses `SharedPreferences` to persist its state. It will no longer flip back to "Off" automatically.
> - Fixed a logic race condition where restarting the service for a port change would reset the UI toggles.
>
> **ADB Daemon Purge (v420.2):**
> - **Port 5037 Killer:** The bridge script now identifies the specific Process ID (PID) using port 5037 and terminates it. This is the only way to solve "Could not read ok from ADB Server" if another program is blocking it.
> - **Firewall Auto-Exception:** Automatically runs a PowerShell command to whitelist the co-processor fabric in Windows Defender.
>
> **0-Comment Enforcement:**
> - 100% naked functional Java logic.

## Proposed Changes

### 1. Persistent UI & FSG Branding (Java)

#### [MODIFY] [MainActivity.java](file:///C:/android/zpu/MainActivity.java)
- **State Persistence:** Toggles now save their state to the internal profile.
- **FSG Branding:** Updated titles to "ZPU v420.2 FSG UNIVERSITY CORE".

#### [MODIFY] [LayerProfileManager.java](file:///C:/Users/User\AndroidStudioProjects\Zpu\app\src\main\java\com\Bllod100\zpu\core\LayerProfileManager.java)
- Added storage for boolean UI states (e.g., `FSG_CONFIG_ACTIVE`).

### 2. PC Bridge Resilience (v420.2)

#### [MODIFY] [ZPU_MASTER_BRIDGE.ps1](file:///C:/Users/User\AndroidStudioProjects\Zpu\ZPU_MASTER_BRIDGE.ps1)
- **Port Hunter:** `Get-NetTCPConnection -LocalPort 5037 | Stop-Process -Force` logic.
- **Legacy Env Injections:** Disables `libusb` and `burst_mode` for maximum stability.

## Verification Plan
- **Switch Test:** Toggle "APPLY CONFIG", close the app, and reopen it. Verify the switch stays **ON**.
- **ADB Purge Test:** Start the script while a "zombie" ADB is running. Verify the script kills the zombie and starts the link.
- **Firewall Test:** Verify "ZPU_SENTRY" appears in the Windows Firewall rules list.
