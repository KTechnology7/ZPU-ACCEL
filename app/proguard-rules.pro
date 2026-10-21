-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-overloadaggressively
-repackageclasses 'z'
-allowaccessmodification

-keep public class com.Bllod100.zpu.MainActivity
-keep public class com.Bllod100.zpu.ResourceManagerService
-keep public class com.Bllod100.zpu.ZpuAdminReceiver

-keep class android.security.keystore.** { *; }
-keep interface android.security.keystore.** { *; }
-keepclassmembers class android.security.keystore.KeyInfo {
    public boolean isInsideSecureHardware();
    public int getSecurityLevel();
}

-keep,allowobfuscation class com.Bllod100.zpu.HardCryptEngine {
    private <fields>;
    private <methods>;
}

-keep class com.Bllod100.zpu.SavageIntelligence { *; }
-keep class com.Bllod100.zpu.core.** { *; }

-renamesourcefileattribute ZPU-RAR
-keepattributes SourceFile,LineNumberTable
