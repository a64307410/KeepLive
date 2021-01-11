package com.hinnka.keepalivedemo2;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.hinnka.keepalive.KeepAliveListener;
import com.hinnka.keepalive.KeepAliveManager;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KeepAliveManager.init(this, new KeepAliveListener() {
            @Override
            public void onAliveReport() {
                Log.d("KeepAlive", "onAliveReport");
            }

//            @Override
//            public void onAppRevive() {
//                Log.d("KeepAlive", "onAppRevive");
//            }
        });
//        Log.d("KeepAlive", "Application启动:" + getPackageName());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }
}
