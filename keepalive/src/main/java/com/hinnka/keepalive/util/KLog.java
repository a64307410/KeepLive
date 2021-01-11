package com.hinnka.keepalive.util;

import android.util.Log;

import com.hinnka.keepalive.KeepAliveConfig;

import java.util.Arrays;

public class KLog {
    public static final String TAG = "KeepAliveSDK";

    public static void d(String... msg) {
        if (KeepAliveConfig.getInstance().isLogEnable()) {
            Log.d(TAG, Arrays.deepToString(msg));
        }
    }
}
