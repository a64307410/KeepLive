package com.hinnka.keepalive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.hinnka.keepalive.util.KLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TimeStatistics {

    private TimeStatistics() {}

    private static class SingletonHolder {
        private static final TimeStatistics instance = new TimeStatistics();
    }

    public static TimeStatistics get() {
        return SingletonHolder.instance;
    }

    private SharedPreferences sp;
    private long startTime = System.currentTimeMillis();

    public void init(Context context) {
        sp = context.getSharedPreferences("keep_alive", Context.MODE_PRIVATE);
        long lastTime = sp.getLong("lastTime", 0);
        long lastStartTime = sp.getLong("lastStartTime", 0);

        final long appRunTime = (lastTime - lastStartTime) / 60 / 1000;
        if (lastStartTime > 0 && appRunTime > 0) {
            final boolean restart = System.currentTimeMillis() - lastTime <= 60 * 1000;
            if (restart) {
                startTime = lastStartTime;
            }
            KLog.d("app run time:", String.valueOf(appRunTime));
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ConfigInternal.listener != null) {
                        Map<String, Object> object = new HashMap<>();
                        object.put("time", appRunTime);
                        object.put("restart", restart);
                        ConfigInternal.listener.trackEvent("keep_alive_time", "保活时长", object);
                    }
                }
            }, 2000);
        }
    }

    @SuppressLint("ApplySharedPref")
    public void update() {
        KLog.d("app run time update");
        sp.edit().putLong("lastTime", System.currentTimeMillis()).commit();
        sp.edit().putLong("lastStartTime", startTime).commit();
    }

}
