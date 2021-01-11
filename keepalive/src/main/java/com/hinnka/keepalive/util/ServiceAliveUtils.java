package com.hinnka.keepalive.util;

import android.app.ActivityManager;
import android.content.Context;

import com.hinnka.keepalive.component.KeepAliveService;

public class ServiceAliveUtils {

    public static boolean isServiceAlice(Context context) {
        boolean isServiceRunning = false;
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return true;
        }
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (KeepAliveService.class.getName().equals(service.service.getClassName())) {
                isServiceRunning = true;
            }
        }
        return isServiceRunning;
    }
}