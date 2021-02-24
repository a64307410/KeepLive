package com.hinnka.keepalive.component;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hinnka.keepalive.MainProcessReceiver;

public class DaemonProcessService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AutoBootReceiver.send(this);
        MainProcessReceiver.register(this);
    }
}
