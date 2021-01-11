package com.hinnka.libcoredaemon;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

import com.hinnka.keepalive.ConfigInternal;
import com.hinnka.keepalive.DaemonParams;
import com.hinnka.keepalive.util.IOUtil;
import com.hinnka.keepalive.KeepAliveDaemon;
import com.hinnka.keepalive.component.AutoBootReceiver;
import com.hinnka.keepalive.component.KeepAliveInstrumentation;
import com.hinnka.keepalive.component.KeepAliveService;
import com.hinnka.keepalive.util.KLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class DaemonEntry {

    int serviceCode = getTransactionCode("TRANSACTION_startService", "START_SERVICE_TRANSACTION");
    int instrumentationCode = getTransactionCode("TRANSACTION_startInstrumentation", "START_INSTRUMENTATION_TRANSACTION");
    int broadcastCode = getTransactionCode("TRANSACTION_broadcastIntent", "BROADCAST_INTENT_TRANSACTION");
    private Parcel mServiceData;
    private Parcel mInstrumentationData;
    private Parcel mBroadcastData;
    private IBinder mRemote;

    public static void main(String[] args) {
        String paramStr = args[0];
        final DaemonParams params = DaemonParams.parse(paramStr);
        final DaemonEntry entry = new DaemonEntry();
        entry.initAmsBinder();
        entry.initServiceParcel(params.packageName, params.processName);
        for (int i = 1; i < params.fileList.length; i++) {
            final int finalI = i;
            new Thread() {
                @Override
                public void run() {
                    setPriority(10);
                    IOUtil.waitFileLock(params.tmpDirPath, params.fileList[finalI]);
                    KLog.d("KeepAliveDaemon", "unlocked");
                    entry.startServiceByAmsBinder();
                }
            }.start();
        }
        IOUtil.waitFileLock(params.tmpDirPath, params.fileList[0]);
        KLog.d("KeepAliveDaemon", "unlocked");
        entry.startServiceByAmsBinder();
        KLog.d("KeepAliveDaemon", "startServiceByAmsBinder");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private boolean startServiceByAmsBinder() {
        if (mRemote == null) {
            return false;
        }
        if (mServiceData == null) {
            KLog.d("KeepAliveDaemon", "REMOTE IS NULL or PARCEL IS NULL !!!");
            return false;
        }
        for (int i = 0; i < 1; i++) {
            try {
                mRemote.transact(instrumentationCode, mInstrumentationData, null, 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                mRemote.transact(serviceCode, mServiceData, null, 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                mRemote.transact(broadcastCode, mBroadcastData, null, 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }


        }
        return true;
    }

    private void initAmsBinder() {
        Class<?> activityManagerNative;
        try {
            activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Object amn = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
            Field mRemoteField = amn.getClass().getDeclaredField("mRemote");
            mRemoteField.setAccessible(true);
            mRemote = (IBinder) mRemoteField.get(amn);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void initServiceParcel(String packageName, String processName) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName(packageName, KeepAliveService.class.getCanonicalName());
        intent.setComponent(component);
        intent.putExtra("startFromKeepAlive", true);

        Parcel parcel = Parcel.obtain();
        intent.writeToParcel(parcel, 0);

        mServiceData = Parcel.obtain();
        mServiceData.writeInterfaceToken("android.app.IActivityManager");
        mServiceData.writeStrongBinder(null);
        if (Build.VERSION.SDK_INT >= 26) {
            mServiceData.writeInt(1);
        }
        intent.writeToParcel(mServiceData, 0);
        mServiceData.writeString(null);
        if (Build.VERSION.SDK_INT >= 26) {
            mServiceData.writeInt(0);
        }
        if (Build.VERSION.SDK_INT > 22) {
            mServiceData.writeString(packageName);
        }
        mServiceData.writeInt(0);

        Intent intent2 = new Intent();
        ComponentName component2 = new ComponentName(packageName, KeepAliveInstrumentation.class.getCanonicalName());
        intent2.setComponent(component2);
        mInstrumentationData = Parcel.obtain();
        mInstrumentationData.writeInterfaceToken("android.app.IActivityManager");
        if (Build.VERSION.SDK_INT >= 26) {
            this.mInstrumentationData.writeInt(1);
        }
        intent2.getComponent().writeToParcel(this.mInstrumentationData, 0);
        this.mInstrumentationData.writeString((String) null);
        this.mInstrumentationData.writeInt(0);
        this.mInstrumentationData.writeInt(0);
        this.mInstrumentationData.writeStrongBinder((IBinder) null);
        this.mInstrumentationData.writeStrongBinder((IBinder) null);
        this.mInstrumentationData.writeInt(0);
        this.mInstrumentationData.writeString((String) null);

        Parcel obtain3 = Parcel.obtain();
        this.mBroadcastData = obtain3;
        obtain3.writeInterfaceToken("android.app.IActivityManager");
        this.mBroadcastData.writeStrongBinder((IBinder) null);
        if (Build.VERSION.SDK_INT >= 26) {
            this.mBroadcastData.writeInt(1);
        }
        Intent intent3 = new Intent();
        ComponentName component3 = new ComponentName(packageName, AutoBootReceiver.class.getCanonicalName());
        intent3.setComponent(component3);
        intent3.putExtra("startFromKeepAlive", true);
        intent3.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent3.writeToParcel(this.mBroadcastData, 0);
        this.mBroadcastData.writeString((String) null);
        this.mBroadcastData.writeStrongBinder((IBinder) null);
        this.mBroadcastData.writeInt(-1);
        this.mBroadcastData.writeString((String) null);
        this.mBroadcastData.writeInt(0);
        this.mBroadcastData.writeStringArray((String[]) null);
        this.mBroadcastData.writeInt(-1);
        this.mBroadcastData.writeInt(0);
        this.mBroadcastData.writeInt(0);
        this.mBroadcastData.writeInt(0);
        this.mBroadcastData.writeInt(0);

        DaemonNative.nativeSetSid();
        try {
            Method setArgV0 = android.os.Process.class.getDeclaredMethod("setArgV0", String.class);
            setArgV0.setAccessible(true);
            setArgV0.invoke(null,processName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public final int getTransactionCode(String str, String str2) {
        try {
            Class<?> cls = Class.forName("android.app.IActivityManager$Stub");
            Field declaredField = cls.getDeclaredField(str);
            declaredField.setAccessible(true);
            return declaredField.getInt(cls);
        } catch (Exception unused) {
            try {
                Class<?> cls2 = Class.forName("android.app.IActivityManager");
                Field declaredField2 = cls2.getDeclaredField(str2);
                declaredField2.setAccessible(true);
                return declaredField2.getInt(cls2);
            } catch (Exception unused2) {
                return -1;
            }
        }
    }

    public static void start(final String[] fileList, final String packageName) {
        new Thread() {
            @Override
            public void run() {
                setPriority(10);
                String processName = KeepAliveDaemon.getProcessName();
                DaemonParams params = new DaemonParams();
                params.fileList = fileList;
                params.packageName = packageName;
                params.processName = processName;
                params.tmpDirPath = ConfigInternal.tmpDirPath;
                DaemonEntry.main(new String[]{params.toString()});
            }
        }.start();
    }

    public static void start(final String[] fileList, final String packageName, final String processName) {
        new Thread() {
            @Override
            public void run() {
                setPriority(10);
                DaemonParams params = new DaemonParams();
                params.fileList = fileList;
                params.packageName = packageName;
                params.processName = processName;
                params.tmpDirPath = ConfigInternal.tmpDirPath;
                String export1 = "export CLASSPATH=$CLASSPATH:" + ConfigInternal.publicSourceDir + "\n";
                String export2 = "export _LD_LIBRARY_PATH=/system/lib/:/vendor/lib/:" + ConfigInternal.nativeLibraryDir + "\n";
                String export3 = "export LD_LIBRARY_PATH=/system/lib/:/vendor/lib/:" + ConfigInternal.nativeLibraryDir + "\n";
                String format = String.format("%s / %s %s --application --nice-name=%s --daemon &", new File("/system/bin/app_process32").exists() ? "app_process32" : "app_process",
                        DaemonEntry.class.getName(),
                        params.toString(),
                        params.processName) + "\n";
                String path = System.getenv("PATH");
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                String[] paths = path.split(":");
                if (paths.length <= 0) {
                    return;
                }
                for (String p : paths) {
                    File shFile = new File(p, "sh");
                    if (shFile.exists()) {
                        try {
                            ProcessBuilder builder = new ProcessBuilder();
                            builder = builder.command(shFile.getPath()).redirectErrorStream(true);
                            builder.directory(new File("/"));
                            Map<String, String> env = builder.environment();
                            env.putAll(System.getenv());
                            Process process = builder.start();
                            OutputStream os = process.getOutputStream();
                            InputStream is = process.getInputStream();
                            InputStreamReader isReader = new InputStreamReader(is, "utf-8");
                            BufferedReader reader = new BufferedReader(isReader);
                            os.write(export1.getBytes());
                            os.flush();
                            os.write(export2.getBytes());
                            os.flush();
                            os.write(export3.getBytes());
                            os.flush();
                            os.write(format.getBytes());
                            os.flush();
                            os.write("exit 156\n".getBytes());
                            os.flush();
                            process.waitFor();
                            String result = readBuffer(reader);
                            KLog.d("KeepAlive", "app_process: result" + result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }.start();
    }

    public static String readBuffer(BufferedReader bufferedReader) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                break;
            }
            sb.append(readLine + "\n");
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }
}
