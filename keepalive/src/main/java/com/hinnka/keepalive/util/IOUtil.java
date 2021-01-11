package com.hinnka.keepalive.util;

import com.qihoo.libcoredaemon.DaemonNative;

import java.io.File;

public class IOUtil {
    public static boolean holdFileLock(String dir, String[] strArr) {
        try {
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            for (String file2 : strArr) {
                File file3 = new File(file, file2);
                if (!file3.exists()) {
                    file3.createNewFile();
                }
//                KLog.d("KeepAliveDaemon", "hold file: " + file3.getAbsolutePath());
                if (DaemonNative.nativeHoldFileLock(file3.getAbsolutePath()) != 1) {
                    return false;
                }
            }
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    public static boolean waitFileLock(String dir, String strArr) {
        try {
            File file = new File(dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            File file3 = new File(file, strArr);
            if (!file3.exists()) {
                file3.createNewFile();
            }
//            KLog.d("KeepAliveDaemon", "wait file: " + file3.getAbsolutePath());
            if (DaemonNative.nativeWaitOneFileLock(file3.getAbsolutePath()) != 1) {
                return false;
            }
            return true;
        } catch (Exception unused) {
            return false;
        }
    }
}
