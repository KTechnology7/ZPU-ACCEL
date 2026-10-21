package com.Bllod100.zpu.core;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;

/**
 * ZPU Permission Sentry - Passive Mode
 * Ensures basic system channels are ready without forcing settings interruptions.
 */
public class PermissionManager {
    public static void audit(Activity activity) {
        NotificationManager m = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        String cid = "perm_sentry";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && m != null) {
            if (m.getNotificationChannel(cid) == null) {
                m.createNotificationChannel(new NotificationChannel(cid, "Sentry", NotificationManager.IMPORTANCE_LOW));
            }
        }
        
        // PASSIVE AUDIT: No longer calls startActivity or requestPermissions to avoid settings popups.
    }

    public static boolean hasStorage() {
        if (Build.VERSION.SDK_INT >= 30) return Environment.isExternalStorageManager();
        return true;
    }
}
