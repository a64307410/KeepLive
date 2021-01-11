package com.hinnka.keepalive.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Handler;
import android.text.TextUtils;

import com.hinnka.keepalive.ConfigInternal;

public class AutoBootReceiver extends BroadcastReceiver {
    public static AutoBootReceiver instance;

    private static String permissionName;

    @Override
    public void onReceive(Context context, Intent intent) {
//        KLog.d("KeepAlive", intent.getAction() + " " + "AutoBootReceiver");
        if (intent != null) {
            boolean startFromKeepAlive = intent.getBooleanExtra("startFromKeepAlive", false);
            if (startFromKeepAlive) {
                ConfigInternal.startFromKeepAlive = true;
            }
        }
    }

    public static String getPermissionName(Context context) {
        try {
            if (TextUtils.isEmpty(permissionName)) {
                PermissionInfo[] permissionInfoArr = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).permissions;
                int length = permissionInfoArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    PermissionInfo permissionInfo = permissionInfoArr[i];
                    if (!TextUtils.isEmpty(permissionInfo.name) && permissionInfo.name.endsWith(".LIBCOREDAEMON_BROADCAST_PERMISSIONS")) {
                        permissionName = permissionInfo.name;
                        break;
                    }
                    i++;
                }
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        return permissionName;
    }

    public static void send(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("com.hinnka.keepalive.intent.action.SERVICE_START_NOTIFY");
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent, getPermissionName(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void register(Context context) {
        synchronized (AutoBootReceiver.class) {
            if (instance == null) {
                instance = new AutoBootReceiver();
                IntentFilter intentFilter = new IntentFilter("com.hinnka.keepalive.intent.action.SERVICE_START_NOTIFY");
                intentFilter.setPriority(1000);
                context.registerReceiver(instance, intentFilter, AutoBootReceiver.getPermissionName(context), (Handler) null);
            }
        }
    }
}
