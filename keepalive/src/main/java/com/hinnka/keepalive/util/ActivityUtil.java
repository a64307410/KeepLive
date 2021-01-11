package com.hinnka.keepalive.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

public class ActivityUtil {

    public static boolean startActivity(Context context, Intent intent) {
        boolean started = true;
        if (Build.VERSION.SDK_INT >= 23) {
            startByNotification(context, intent);
            startByAlarm(context, intent);
        } else {
            started = false;
        }
        if (!started) {
            context.startActivity(intent);
        }
        return started;
    }

    public static boolean startByAlarm(Context context, Intent intent) {
        intent.putExtra("start_way", "AlarmManager");
        KLog.d("NotificationStartHelper", "使用AlarmManager方式");
        PendingIntent activity = PendingIntent.getActivity(context, 10102, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 200, activity);
        } else {
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 200, activity);
        }
        return true;
    }

    private static void startByNotification(Context context, Intent intent) {
        KLog.d("NotificationStartHelper", "使用Notification方式");
        intent.putExtra("start_way", "notification");
        PendingIntent activity = PendingIntent.getActivity(context, 10102, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        startNotification(context, activity, context.getApplicationInfo().icon);
        sendPendingIntent(context, activity, intent);
    }

    private static void startNotification(Context context, PendingIntent pendingIntent, int i) {
        Notification.Builder builder;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(notificationManager);
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                builder = Notification.Builder.class.getDeclaredConstructor(new Class[]{Context.class, String.class}).newInstance(context, "sm_lkr_ntf_hl_pr_chn_id_7355608_wtf");
            } catch (Exception unused) {
                builder = null;
            }
            if (builder == null) {
                builder = new Notification.Builder(context);
            }
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(i);
        builder.setFullScreenIntent(pendingIntent, true);
        builder.setAutoCancel(true);
        notificationManager.cancel("AA_TAG1", 10101);
        notificationManager.notify("AA_TAG1", 10101, builder.getNotification());
        cancelNotification(context);
    }

    private static void sendPendingIntent(Context context, PendingIntent pendingIntent, Intent intent) {
        try {
            pendingIntent.send();
        } catch (Throwable unused) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(intent);
            } catch (Exception unused2) {
            }
        }
    }

    private static void createChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= 26 && notificationManager.getNotificationChannel("sm_lkr_ntf_hl_pr_chn_id_7355608_wtf") == null) {
            NotificationChannel notificationChannel = new NotificationChannel("sm_lkr_ntf_hl_pr_chn_id_7355608_wtf", "天气不好", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("天气预报");
            notificationChannel.setLockscreenVisibility(-1);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            notificationChannel.setSound((Uri) null, (AudioAttributes) null);
            notificationChannel.setBypassDnd(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private static void cancelNotification(final Context context) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                if (context != null) {
                    try {
                        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel("AA_TAG1", 10101);
                    } catch (Throwable unused) {
                    }
                }
            }
        }, 1000);
    }
}
