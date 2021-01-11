package com.hinnka.keepalivedemo;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.hinnka.keepalive.KeepAliveListener;
import com.hinnka.keepalive.KeepAliveManager;

import org.json.JSONObject;

import java.util.Map;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d("KeepAlive", "Application启动:" + getPackageName());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        KeepAliveManager.init(this, new KeepAliveListener() {
            @Override
            public void trackEvent(String eventCode, String eventName, Map<String, Object> object) {
                Log.d("trackEvent", eventCode + " " + eventName + " baohuo_sdk_" + Build.BRAND);
            }
        });
    }
}
