package com.Bllod100.zpu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SelfIgnitionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("zpu_settings", Context.MODE_PRIVATE);
        boolean startOnBoot = prefs.getBoolean("start_on_boot", true);

        if (startOnBoot) {
            String action = intent.getAction();
            if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
                "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
                
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }
}
