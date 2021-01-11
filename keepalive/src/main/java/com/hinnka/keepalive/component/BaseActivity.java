/*
 * Original Copyright 2015 Mars Kwok
 * Modified work Copyright (c) 2020, weishu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hinnka.keepalive.component;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.hinnka.keepalive.KeepAliveScreenMonitor;
import com.hinnka.keepalive.TimeStatistics;
import com.hinnka.keepalive.util.ActivityUtil;
import com.hinnka.keepalive.util.KLog;

import java.lang.ref.WeakReference;
import java.util.Random;

public class BaseActivity extends Activity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    public static WeakReference<BaseActivity> weakInstance = new WeakReference<>(null);
    private ChangeReceiver mReceiver;

    private boolean isScreenOn = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window mWindow = getWindow();
        mWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams attrParams = mWindow.getAttributes();
        attrParams.x = 0;
        attrParams.y = 0;
        attrParams.height = 1;
        attrParams.width = 1;
        mWindow.setAttributes(attrParams);
        weakInstance = new WeakReference<>(this);
        KLog.d("KeepAlive", "KeepAliveActivity onCreate " + getPackageName());

        isScreenOn = isScreenOn();
        if (isScreenOn) {
            KLog.d("KeepAlive", "Screen On");
            TimeStatistics.get().update();
            acquireWake();
            Intent screenIntent = new Intent("KEEP_ALIVE_SCREEN_ON");
            screenIntent.setPackage(getPackageName());
            sendBroadcast(screenIntent);
        }

        mReceiver = new ChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        this.registerReceiver(mReceiver, filter);
    }

    public boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isScreenOn() || (Build.VERSION.SDK_INT >= 20 && powerManager.isInteractive());
        }
        return false;
    }

    public void acquireWake() {
        if (!isScreenOn()) {
            return;
        }
        try {
            synchronized (PowerManager.WakeLock.class) {
                if (KeepAliveScreenMonitor.sActiveWakeLocks != null) {
                    KeepAliveScreenMonitor.sActiveWakeLocks.release();
                }
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "com.hinnka.keepalive:wake");
                wl.setReferenceCounted(false);
                wl.acquire();
                KeepAliveScreenMonitor.sActiveWakeLocks = wl;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TimeStatistics.get().update();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
                KLog.d("KeepAlive", "Screen On");
                if (!isFinishing()) {
                    finish();
                }
                acquireWake();
                Intent screenIntent = new Intent("KEEP_ALIVE_SCREEN_ON");
                screenIntent.setPackage(context.getPackageName());
                sendBroadcast(screenIntent);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
                KLog.d("KeepAlive", "Screen Off");
                Intent screenIntent = new Intent("KEEP_ALIVE_SCREEN_OFF");
                screenIntent.setPackage(context.getPackageName());
                sendBroadcast(screenIntent);
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) { // 解锁
                KLog.d("KeepAlive", "User Present");
                if (!isFinishing()) {
                    finish();
                }
                Intent screenIntent = new Intent("KEEP_ALIVE_USER_PRESENT");
                screenIntent.setPackage(context.getPackageName());
                sendBroadcast(screenIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isScreenOn()) {
            if (!isScreenOn) {
                KLog.d("KeepAlive", "Screen On");
                TimeStatistics.get().update();
                isScreenOn = true;
                acquireWake();
                Intent screenIntent = new Intent("KEEP_ALIVE_SCREEN_ON");
                screenIntent.setPackage(getPackageName());
                sendBroadcast(screenIntent);
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        KeepAliveService.start(this);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public static void start(Context context) {
        Class<?>[] activities = new Class[]{AZActivity.class, BYActivity.class, CXActivity.class, DWActivity.class, EVActivity.class};
        Random random = new Random();
        Class<?> activityClz = activities[random.nextInt(5)];
        Intent intent = new Intent(context, activityClz);
        ActivityUtil.startActivity(context, intent);
    }
}
