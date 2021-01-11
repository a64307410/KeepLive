package com.hinnka.keepalive;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.hinnka.keepalive.component.HideLauncherActivity;
import com.hinnka.keepalive.component.KeepAliveJobService;
import com.hinnka.keepalive.component.KeepAliveService;
import com.hinnka.keepalive.util.AppUtil;
import com.hinnka.keepalive.util.DeviceUtil;

import java.util.List;

public class KeepAliveManager {

    public static Handler handler = new Handler(Looper.getMainLooper());

    public static void init(final Application app, KeepAliveListener listener) {
        ConfigInternal.init(app, listener);
        KeepAliveDaemon.init(app);
        WallpaperEngine.getInstance().init(app);
        if (Build.VERSION.SDK_INT >= 21) {
            KeepAliveJobService.start(app, 206);
            KeepAliveJobService.start(app, 203);
            KeepAliveJobService.start(app, 204);
            KeepAliveJobService.start(app, 205);
        }
        if (ConfigInternal.packageName.equals(ConfigInternal.processName)) {
            KeepAliveScreenMonitor.get().onCreate(app);
            TimeStatistics.get().init(app);
        }
        Foreground.init(app);
        Foreground.get().addListener(new Foreground.Listener() {
            @Override
            public void onActivityFirstCreate(Activity activity) {
                KeepAliveService.start(activity);
            }

            @Override
            public void onBecameForeground(Activity activity) {
                if (KeepAliveConfig.getInstance().isWakeUpApps()) {
                    new Thread() {
                        @Override
                        public void run() {
                            notifyThirdApps(app);
                        }
                    }.start();
                }
            }

            @Override
            public void onBecameBackground() {

            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Foreground.get().isBackground()
                        && ConfigInternal.packageName.equals(ConfigInternal.processName)) {
                    if (KeepAliveConfig.getInstance().isWakeUpApps()) {
                        new Thread() {
                            @Override
                            public void run() {
                                notifyThirdApps(app);
                            }
                        }.start();
                    }
                }
                if (ConfigInternal.listener != null && ConfigInternal.packageName.equals(ConfigInternal.processName)) {
                    if (Foreground.get().isForeground()) {
                        ConfigInternal.listener.trackEvent("user_manual_start", "用户手动打开", null);
                    } else if (ConfigInternal.startFromKeepAlive) {
                        ConfigInternal.listener.trackEvent("keep_alive_start", "被保活拉起", null);
                    } else {
                        ConfigInternal.listener.trackEvent("unknown_start", "未知原因拉起", null);
                    }
                }

                aliveReport();
            }
        }, 5 * 1000);
    }

    private static void aliveReport() {
        if (ConfigInternal.packageName.equals(ConfigInternal.processName)) {
            if (ConfigInternal.listener != null) {
                ConfigInternal.listener.trackEvent("baohuo_sdk_heart_beat", "保活sdk心跳上报", null);
            }
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                aliveReport();
            }
        }, 30 * 60 * 1000);
    }

    public static void notifyThirdApps(final Context context) {
        if (KeepAliveConfig.getInstance().getWakeupStrategy() == KeepAliveConfig.WakeupStrategy.All
                || KeepAliveConfig.getInstance().getWakeupStrategy() == KeepAliveConfig.WakeupStrategy.Normal) {
            try {
                PackageManager packageManager = context.getPackageManager();
                Intent intent = new Intent("com.hinnka.keepalive.notify");
                List<ResolveInfo> resolveInfoList = packageManager.queryIntentServices(intent, 0);
                for (ResolveInfo resolveInfo : resolveInfoList) {
                    final ServiceInfo info = resolveInfo.serviceInfo;
                    if (info != null && !context.getPackageName().equals(info.packageName)) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String contentUri = "content://" + info.packageName + ".keep.alive.provider" + "/start";
                                    final Uri uri = Uri.parse(contentUri);
                                    context.grantUriPermission(info.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    context.grantUriPermission(info.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    final ContentValues contentValues = new ContentValues();
                                    contentValues.put("time", System.currentTimeMillis());
                                    context.getContentResolver().insert(uri, contentValues);
                                } catch (Exception e) {
                                    Log.e("KeepAlive", e.getMessage());
                                    e.printStackTrace();
                                }

                                try {
                                    ComponentName componentName = new ComponentName(info.packageName, info.name);
                                    Intent i = new Intent();
                                    i.setComponent(componentName);
                                    context.startService(i);
                                } catch (Exception e) {
                                    Log.e("KeepAlive", e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("KeepAlive", e.getMessage());
                e.printStackTrace();
            }
        }
        if (KeepAliveConfig.getInstance().getWakeupStrategy() == KeepAliveConfig.WakeupStrategy.All
                || KeepAliveConfig.getInstance().getWakeupStrategy() == KeepAliveConfig.WakeupStrategy.Activity) {
            try {
                PackageManager packageManager = context.getPackageManager();
                Intent intent = new Intent("com.hinnka.keepalive.notify");
                List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
                for (ResolveInfo resolveInfo : resolveInfoList) {
                    final ActivityInfo info = resolveInfo.activityInfo;
                    if (info != null && !context.getPackageName().equals(info.packageName)) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ComponentName componentName = new ComponentName(info.packageName, info.name);
                                    Intent i = new Intent();
                                    i.setComponent(componentName);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(i);
                                } catch (Exception e) {
                                    Log.e("KeepAlive", e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("KeepAlive", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void setLiveWallpaper(Activity activity) {
        WallpaperEngine.getInstance().setLiveWallpaper(activity);
    }

    public static boolean isWallpaperSet(Context context) {
        return WallpaperEngine.getInstance().isWallpaperSet(context);
    }

    public static void hideLauncherIcon(Context context) {
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(context, HideLauncherActivity.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static void showLauncherIcon(Context context) {
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(new ComponentName(context, HideLauncherActivity.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static boolean isIgnoringBatteryOptimizations(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String packageName = activity.getPackageName();
                PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                return pm.isIgnoringBatteryOptimizations(packageName);
            } catch (Throwable e) {
                e.printStackTrace();
                return true;
            }
        } else {
            return true;
        }
    }

    @SuppressLint("BatteryLife")
    public static void openIgnoringBatteryOptimizations(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String packageName = activity.getPackageName();
                PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    activity.startActivityForResult(intent, 9999);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isWhiteListAvailable(Context context) {
        if (DeviceUtil.isHuawei()) {
            if (AppUtil.isComponentExist(context, "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")) {
                return true;
            }
            if (AppUtil.isComponentExist(context, "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.bootstart.BootStartActivity")) {
                return true;
            }
        }
        if (DeviceUtil.isMIUI()) {
            if (AppUtil.isComponentExist(context, "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity")) {
                return true;
            }
        }
        if (DeviceUtil.isOppo()) {
            if (AppUtil.isPackageInstalled(context, "com.coloros.phonemanager")) {
                return true;
            }
            if (AppUtil.isPackageInstalled(context, "com.oppo.safe")) {
                return true;
            }
            if (AppUtil.isPackageInstalled(context, "com.coloros.oppoguardelf")) {
                return true;
            }
            if (AppUtil.isPackageInstalled(context, "com.coloros.safecenter")) {
                return true;
            }
        }
        if (DeviceUtil.isVivo()) {
            if (AppUtil.isPackageInstalled(context, "com.iqoo.secure")) {
                return true;
            }
        }
        if (DeviceUtil.isMeizu()) {
            if (AppUtil.isPackageInstalled(context, "com.meizu.safe")) {
                return true;
            }
        }
        if (DeviceUtil.isSamsung()) {
            if (AppUtil.isPackageInstalled(context, "com.samsung.android.sm_cn")) {
                return true;
            }
            if (AppUtil.isPackageInstalled(context, "com.samsung.android.sm")) {
                return true;
            }
        }
        return false;
    }

    public static void openWhiteListSetting(Context context) {
        if (DeviceUtil.isHuawei()) {
            if (AppUtil.isComponentExist(context, "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")) {
                AppUtil.startComponent(context, "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
            }
            if (AppUtil.isComponentExist(context, "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.bootstart.BootStartActivity")) {
                AppUtil.startComponent(context, "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
            }
        }
        if (DeviceUtil.isMIUI()) {
            if (AppUtil.isComponentExist(context, "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity")) {
                AppUtil.startComponent(context, "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity");
            }
        }
        if (DeviceUtil.isOppo()) {
            if (AppUtil.isPackageInstalled(context, "com.coloros.phonemanager")) {
                AppUtil.startApp(context, "com.coloros.phonemanager");
            }
            if (AppUtil.isPackageInstalled(context, "com.oppo.safe")) {
                AppUtil.startApp(context, "com.oppo.safe");
            }
            if (AppUtil.isPackageInstalled(context, "com.coloros.oppoguardelf")) {
                AppUtil.startApp(context, "com.coloros.oppoguardelf");
            }
            if (AppUtil.isPackageInstalled(context, "com.coloros.safecenter")) {
                AppUtil.startApp(context, "com.coloros.safecenter");
            }
        }
        if (DeviceUtil.isVivo()) {
            if (AppUtil.isPackageInstalled(context, "com.iqoo.secure")) {
                AppUtil.startApp(context, "com.iqoo.secure");
            }
        }
        if (DeviceUtil.isMeizu()) {
            if (AppUtil.isPackageInstalled(context, "com.meizu.safe")) {
                AppUtil.startApp(context, "com.meizu.safe");
            }
        }
        if (DeviceUtil.isSamsung()) {
            if (AppUtil.isPackageInstalled(context, "com.samsung.android.sm_cn")) {
                AppUtil.startApp(context, "com.samsung.android.sm_cn");
            }
            if (AppUtil.isPackageInstalled(context, "com.samsung.android.sm")) {
                AppUtil.startApp(context, "com.samsung.android.sm");
            }
        }
    }
}
