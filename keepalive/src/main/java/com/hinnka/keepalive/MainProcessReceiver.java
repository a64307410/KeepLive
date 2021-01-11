package com.hinnka.keepalive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.hinnka.keepalive.component.AutoBootReceiver;

public class MainProcessReceiver extends BroadcastReceiver {
    public static MainProcessReceiver instance;

    public static void send(Context context) {
        Intent intent = new Intent();
        intent.setAction("com.hinnka.keepalive.intent.action.MAIN_PROCESS_START_NOTIFY");
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent, AutoBootReceiver.getPermissionName(context));
    }

    public static synchronized void register(Context context) {
        synchronized (MainProcessReceiver.class) {
            if (instance == null) {
                instance = new MainProcessReceiver();
                try {
                    IntentFilter intentFilter = new IntentFilter("com.hinnka.keepalive.intent.action.MAIN_PROCESS_START_NOTIFY");
                    intentFilter.setPriority(1000);
                    context.registerReceiver(instance, intentFilter, AutoBootReceiver.getPermissionName(context), (Handler) null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AutoBootReceiver.send(context);
    }
}
