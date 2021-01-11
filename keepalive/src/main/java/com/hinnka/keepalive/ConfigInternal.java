package com.hinnka.keepalive;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import me.weishu.reflection.Reflection;

public class ConfigInternal {
    public static String packageName;
    public static String processName;
    public static Class<?> startActivity;
    public static KeepAliveListener listener;

    public static String tmpDirPath;
    public static String nativeLibraryDir;
    public static String publicSourceDir;

    public static boolean startFromKeepAlive;

    public static void init(Context context, KeepAliveListener listener) {
        Reflection.unseal(context);
        ConfigInternal.listener = listener;
        ConfigInternal.packageName = context.getPackageName();
        ConfigInternal.processName = KeepAliveDaemon.getProcessName();
        try {
            ConfigInternal.startActivity = Class.forName(context.getString(R.string.start_activity));
        } catch (ClassNotFoundException ignored) {
        }
        PackageInfo packageInfo;
        if (TextUtils.isEmpty(tmpDirPath)) {
            tmpDirPath = context.getDir("TmpDir", 0).getAbsolutePath();
        }
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (Exception unused) {
            packageInfo = null;
        }
        if (TextUtils.isEmpty(nativeLibraryDir)) {
            if (packageInfo != null) {
                nativeLibraryDir = packageInfo.applicationInfo.nativeLibraryDir;
            } else {
                throw new IllegalArgumentException("so find path is not set");
            }
        }
        if (TextUtils.isEmpty(publicSourceDir)) {
            if (packageInfo != null) {
                publicSourceDir = packageInfo.applicationInfo.publicSourceDir;
            } else {
                throw new IllegalArgumentException("class find path is not set");
            }
        }
    }
}
