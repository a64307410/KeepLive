package com.hinnka.libcoredaemon;

import android.content.Context;

public class DaemonNative {
    static {
        try {
            System.loadLibrary("core_daemon");
        } catch (Exception ignored) {
        }
    }

    public static native int nativeDaemon(Context context);

    public static native int nativeHoldFileLock(String str);

    public static native int nativeSetSid();

    public static native int nativeWaitOneFileLock(String str);
}
