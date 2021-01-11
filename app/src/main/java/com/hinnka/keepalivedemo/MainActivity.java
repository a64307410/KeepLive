package com.hinnka.keepalivedemo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.hinnka.keepalive.KeepAliveManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Bitmap> tests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle bundle = new Bundle();
        bundle.putString("test", "test");
//        startInstrumentation(new ComponentName(this, KeepAliveInstrumentation.class), getFileStreamPath("aaa").getAbsolutePath(), bundle);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("1", "1", NotificationManager.IMPORTANCE_HIGH));
        }
        Notification notification = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("test")
                .setContentText("test")
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setOngoing(true)
                .setAutoCancel(false)
                .build();
        notificationManager.notify(1, notification);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeepAliveManager.hideLauncherIcon(MainActivity.this);
                createShortCut();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeepAliveManager.showLauncherIcon(MainActivity.this);
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 999; i++) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_wall_preview_long);
                    tests.add(bitmap);
                }
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
    }

    //创建桌面快捷方式
    private void createShortCut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String shortcutId = "com.hinnka.shortcut";
            boolean isExit = false;
            ShortcutManager shortcutManager = (ShortcutManager) getSystemService(Context.SHORTCUT_SERVICE);
            for (ShortcutInfo info : shortcutManager.getPinnedShortcuts()) {
                if (shortcutId.equals(info.getId())) {
                    //判断快捷方式是否已存在
                    isExit = true;
                }
            }
            if (!isExit && shortcutManager.isRequestPinShortcutSupported()) {
                Intent shortcutInfoIntent = new Intent(Intent.ACTION_VIEW);
                shortcutInfoIntent.setClass(this, MainActivity.class);
                shortcutInfoIntent.setAction(Intent.ACTION_VIEW);
                ShortcutInfo info = new ShortcutInfo.Builder(this, shortcutId)
                        .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher_round))
                        .setShortLabel(getString(R.string.app_name))
                        .setIntent(shortcutInfoIntent)
                        .build();
                shortcutManager.requestPinShortcut(info, null);
            }
        } else {
            Intent intentAddShortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            intentAddShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
            intentAddShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this,
                            R.mipmap.ic_launcher_round));//设置Launcher的Uri数据
            intentAddShortcut.putExtra("duplicate", false);
            Intent intentLauncher = new Intent(this, MainActivity.class);
            intentAddShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intentLauncher);
            sendBroadcast(intentAddShortcut);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        KeepAliveManager.setLiveWallpaper(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("KeepAlive", "wallpaper set? " + KeepAliveManager.isWallpaperSet(this));
    }
}