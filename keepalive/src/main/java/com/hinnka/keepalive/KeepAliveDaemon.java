package com.hinnka.keepalive;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.hinnka.keepalive.component.Assist1ProcessService;
import com.hinnka.keepalive.component.AssistProcessService;
import com.hinnka.keepalive.component.AutoBootReceiver;
import com.hinnka.keepalive.component.DaemonProcessService;
import com.hinnka.keepalive.util.IOUtil;
import com.hinnka.keepalive.util.KLog;
import com.qihoo.libcoredaemon.DaemonEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class KeepAliveDaemon {

    public static void init(final Context context) {
        String processName = getProcessName();
        KLog.d("KeepAliveDaemon", processName + " start");
        registerBroadcast(context, processName);
        if (context.getPackageName().equals(processName)) {
            startService(context, DaemonProcessService.class);
            startService(context, AssistProcessService.class);
            startService(context, Assist1ProcessService.class);
        }
        if ("android.daemon".equals(processName)) {
            IOUtil.holdFileLock(ConfigInternal.tmpDirPath, new String[]{
                    "daemon_service_assist", "daemon_service_assist1", "daemon_native_assist", "daemon_native_assist1"
            });
            DaemonEntry.start(new String[]{
                    "assist_native_daemon", "assist1_native_daemon"
            }, context.getPackageName(), "daemon");
            DaemonEntry.start(new String[]{
                    "assist_service_daemon", "assist1_service_daemon"
            }, context.getPackageName());
        }
        if ("android.assist".equals(processName)) {
            IOUtil.holdFileLock(ConfigInternal.tmpDirPath, new String[]{
                    "assist_service_daemon", "assist_service_assist1", "assist_native_daemon", "assist_native_assist1"
            });
            DaemonEntry.start(new String[]{
                    "daemon_native_assist", "assist1_native_assist"
            }, context.getPackageName(), "assist");
            DaemonEntry.start(new String[]{
                    "daemon_service_assist", "assist1_service_assist"
            }, context.getPackageName());
        }
        if ("android.assist1".equals(processName)) {
            IOUtil.holdFileLock(ConfigInternal.tmpDirPath, new String[]{
                    "assist1_service_daemon", "assist1_service_assist", "assist1_native_daemon", "assist1_native_assist"
            });
            DaemonEntry.start(new String[]{
                    "daemon_native_assist1", "assist_native_assist1"
            }, context.getPackageName(), "assist1");
            DaemonEntry.start(new String[]{
                    "daemon_service_assist1", "assist_service_assist1"
            }, context.getPackageName());
        }
    }

    private static void registerBroadcast(Context context, String processName) {
        if (context.getPackageName().equals(processName)) {
            AutoBootReceiver.register(context);
        }
        MainProcessReceiver.send(context);
    }

    public static void startService(Context context, Class<? extends Service> serviceClass) {
        try {
            context.startService(new Intent(context, serviceClass));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            context.bindService(new Intent(context, serviceClass), new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            }, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProcessName() {
        BufferedReader mBufferedReader = null;
        try {
            File file = new File("/proc/self/cmdline");
            mBufferedReader = new BufferedReader(new FileReader(file));
            return mBufferedReader.readLine().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (mBufferedReader != null) {
                try {
                    mBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
