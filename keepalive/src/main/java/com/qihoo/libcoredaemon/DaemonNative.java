package com.qihoo.libcoredaemon;

public class DaemonNative {
    static {
        try {
            System.loadLibrary("core_daemon");
        } catch (Exception unused) {
        }
    }

    public static final native int nativeDaemon(int i, int i2);

    public static final native int nativeHoldFileLock(String str);

    public static final native int nativeSetSid();

    public static final native int nativeWaitFileListLock(String[] strArr);

    public static final native int nativeWaitOneFileLock(String str);
}
