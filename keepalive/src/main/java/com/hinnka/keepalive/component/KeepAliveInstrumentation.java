package com.hinnka.keepalive.component;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.hinnka.keepalive.ConfigInternal;
import com.hinnka.keepalive.KeepAliveConfig;
import com.hinnka.keepalive.KeepAliveManager;

public class KeepAliveInstrumentation extends Instrumentation {
    @Override
    public void callApplicationOnCreate(Application app) {
        super.callApplicationOnCreate(app);
    }

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        ConfigInternal.startFromKeepAlive = true;
        Intent intent = new Intent("com.hinnka.keepalive.intent.action.SERVICE_START_NOTIFY");
        intent.putExtra("startFromKeepAlive", true);
        intent.setPackage(getTargetContext().getPackageName());
        getTargetContext().sendOrderedBroadcast(intent, AutoBootReceiver.getPermissionName(getTargetContext()));
        KeepAliveService.start(getTargetContext());
    }
}
