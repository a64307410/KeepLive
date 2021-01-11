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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.hinnka.keepalive.ConfigInternal;
import com.hinnka.keepalive.KeepAliveDaemon;
import com.hinnka.keepalive.R;
import com.hinnka.keepalive.util.KLog;
import com.hinnka.keepalive.util.ServiceAliveUtils;

public class KeepAliveService extends Service {

    private volatile boolean threadAlive = true;

    @Override
    public void onCreate() {
        super.onCreate();
//        keepForeground(this);
        KLog.d("KeepAlive", "KeepAliveService 启动");

        threadAlive = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (threadAlive) {
                    SystemClock.sleep(1000 * 60 * 30);
                    KLog.d("KeepAlive", "service running...");
                }
            }
        }).start();
    }


    public static void keepForeground(Service service) {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                RemoteViews remoteViews = new RemoteViews(service.getPackageName(), R.layout.layout_keep_alive_noti);
                final NotificationManager manager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
                manager.createNotificationChannel(new NotificationChannel("com.hinnka.keepalive", "channel", NotificationManager.IMPORTANCE_HIGH));
                Notification notification = new NotificationCompat.Builder(service, "com.hinnka.keepalive")
                        .setSmallIcon(service.getApplicationInfo().icon)
//                            .setContentTitle("KeepAlive Title")
//                            .setContentText("KeepAlive Content")
                        .setCustomContentView(remoteViews)
                        .build();
                service.startForeground(1, notification);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.deleteNotificationChannel("com.hinnka.keepalive");
                    }
                }, 1000);
            } else {
                service.startForeground(1, new Notification());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            boolean startFromKeepAlive = intent.getBooleanExtra("startFromKeepAlive", false);
            if (startFromKeepAlive) {
                ConfigInternal.startFromKeepAlive = true;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        threadAlive = false;
        super.onDestroy();
    }


    public static void start(Context context) {
        if (!ServiceAliveUtils.isServiceAlice(context)) {
            KeepAliveDaemon.startService(context, KeepAliveService.class);
        }
    }
}
