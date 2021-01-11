package com.hinnka.keepalive;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import com.hinnka.keepalive.component.BaseActivity;
import com.hinnka.keepalive.util.DeviceUtil;
import com.hinnka.keepalive.util.KLog;

public class KeepAliveScreenMonitor {

    public static PowerManager.WakeLock sActiveWakeLocks;

    private ChangeReceiver mReceiver;
    private Context mContext;

    public static KeepAliveScreenMonitor get() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static final KeepAliveScreenMonitor instance = new KeepAliveScreenMonitor();
    }

    public void onCreate(Context context) {
        this.mContext = context;
        if (KeepAliveConfig.getInstance().isListenScreenState()) {
            if (DeviceUtil.isOppo()) {
                acquireWake();

                if (!isScreenOn()) {
                    try {
                        BaseActivity.start(mContext);
                        KLog.d("KeepAlive", "start one px activity");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            mReceiver = new ChangeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            mContext.registerReceiver(mReceiver, filter);
        }
    }

    public boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isScreenOn() || (Build.VERSION.SDK_INT >= 20 && powerManager.isInteractive());
        }
        return false;
    }

    public void acquireWake() {
        if (!isScreenOn()) {
            return;
        }
        KLog.d("KeepAlive", "acquireWake");
        try {
            synchronized (PowerManager.WakeLock.class) {
                if (sActiveWakeLocks != null) {
                    sActiveWakeLocks.release();
                }
                PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "com.hinnka.keepalive:wake");
                wl.setReferenceCounted(false);
                wl.acquire();
                sActiveWakeLocks = wl;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void completeWake() {
        try {
            synchronized (PowerManager.WakeLock.class) {
                if (sActiveWakeLocks != null) {
                    sActiveWakeLocks.release();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ChangeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            TimeStatistics.get().update();
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
                KLog.d("KeepAlive", "Screen On");
                Activity activity = BaseActivity.weakInstance.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
                if (DeviceUtil.isOppo()) {
                    acquireWake();
                }
                Intent screenIntent = new Intent("KEEP_ALIVE_SCREEN_ON");
                screenIntent.setPackage(context.getPackageName());
                mContext.sendBroadcast(screenIntent);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
                KLog.d("KeepAlive", "Screen Off");
                if (DeviceUtil.isOppo()) {
                    try {
                        BaseActivity.start(context);
                        KLog.d("KeepAlive", "start one px activity");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    completeWake();
                }
                Intent screenIntent = new Intent("KEEP_ALIVE_SCREEN_OFF");
                screenIntent.setPackage(context.getPackageName());
                mContext.sendBroadcast(screenIntent);
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
                KLog.d("KeepAlive", "User Present");
                Activity activity = BaseActivity.weakInstance.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
                Intent screenIntent = new Intent("KEEP_ALIVE_USER_PRESENT");
                screenIntent.setPackage(context.getPackageName());
                mContext.sendBroadcast(screenIntent);
            }
        }
    }

    public void onDestroy() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
    }

}
