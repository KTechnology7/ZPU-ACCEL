# Zpu Resource Accelerator (v58.0 - Jack Core Namespace Refactor) Implementation Plan

Refactoring the entire project to the `com.jack.zpu` namespace, starting with the core library and then the main engine. This establishes **Jack Authority** over the 100% Java Silicon Node.

## User Review Required

> [!IMPORTANT]
> **Refactor Sequence (Core-First):**
> 1.  **Core Migration:** Move `com.android.zpu.core` to `com.jack.zpu.core`.
> 2.  **Engine Migration:** Move `com.android.zpu` main package to `com.jack.zpu`.
> 3.  **Dependency Alignment:** Update `build.gradle.kts` and `AndroidManifest.xml` to match the new personal identity.
>
> **Identity Change:**
> - **New Application ID:** `com.jack.zpu`
> - **HUD Change:** Dashboard will show **"JACK_AUTHORITY"** in the node status.

## Proposed Changes

### 1. The Personal Core Library (`com.jack.zpu.core`)

#### [NEW] [ZpuFiber.java](file:///C:/Users/User/AndroidStudioProjects/Zpu/app/src/main/java/com/jack/zpu/core/ZpuFiber.java)
#### [NEW] [HardwareProbe.java](file:///C:/Users/User/AndroidStudioProjects/Zpu/app/src/main/java/com/jack/zpu/core/HardwareProbe.java)
#### [NEW] [ZpuBinaryFabric.java](file:///C:/Users/User/AndroidStudioProjects/Zpu/app/src/main/java/com/jack/zpu/core/ZpuBinaryFabric.java)
#### [NEW] [SavageFileCrypt.java](file:///C:/Users/User/AndroidStudioProjects/Zpu/app/src/main/java/com/jack/zpu/core/SavageFileCrypt.java)
- Move and update package to `package com.jack.zpu.core`.

### 2. Main Engine Components (`com.jack.zpu`)

#### [NEW] [ResourceProtocol.java](file:///C:/android/zpu/ResourceProtocol.java)
#### [NEW] [SavageIntelligence.java](file:///C:/android/zpu/SavageIntelligence.java)
#### [NEW] [MainActivity.java](file:///C:/android/zpu/MainActivity.java)
- (And all other 20+ Java files)
- Move and update package to `package com.jack.zpu`.

### 3. Build & System Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/User/AndroidStudioProjects/Zpu/app/build.gradle.kts)
- Update `namespace` and `applicationId` to `com.jack.zpu`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/User/AndroidStudioProjects/Zpu/app/src/main/AndroidManifest.xml)
- Update all class references to `.JackAdminReceiver`, etc.

---

### PC Side & Master Bridge

#### [MODIFY] [ZPU_MASTER_BRIDGE.ps1](file:///C:/Users/User/AndroidStudioProjects/Zpu/ZPU_MASTER_BRIDGE.ps1)
- Update ADB shell launch commands to target `com.jack.zpu`.

## Verification Plan
- **Folder Integrity:** Verify `com/jack/zpu/core` contains all Personal Library files.
- **Project Sync:** Run Gradle sync and verify no "package not found" errors.
- **Execution Test:** Deploy the new `com.jack.zpu` APK and verify the PC script connects successfully.
