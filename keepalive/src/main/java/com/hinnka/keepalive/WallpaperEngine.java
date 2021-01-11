package com.hinnka.keepalive;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.view.WindowManager;

import com.hinnka.keepalive.component.LiveWallpaperService;
import com.hinnka.keepalive.util.DeviceUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;

public class WallpaperEngine {

    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private Bitmap previewWallpaper;
    private Bitmap defaultWallpaper;
    private boolean settingWallpaper;

    private WallpaperEngine() {
    }

    public static WallpaperEngine getInstance() {
        return Singleton.instance;
    }

    public void init(Context context) {
        Point size = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        decodePreviewWallpaper(context);
        decodeDefaultWallpaper(context);
    }

    private void decodePreviewWallpaper(Context context) {
        previewWallpaper = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_wall_preview_long);
        previewWallpaper = ThumbnailUtils.extractThumbnail(previewWallpaper, screenWidth, screenHeight);
    }

    private void decodeDefaultWallpaper(Context context) {
        File wallpaperFile = context.getFileStreamPath("def_wallpaper.jpg");
        WallpaperManager wallpaperManager = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
        if (wallpaperManager != null) {
            try {
                Drawable drawable = wallpaperManager.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    defaultWallpaper = ThumbnailUtils.extractThumbnail(((BitmapDrawable) drawable).getBitmap(), screenWidth, screenHeight);
                }
            } catch (Exception ignored) {
            }
        }
        if (defaultWallpaper == null) {
            if (wallpaperFile.exists()) {
                defaultWallpaper = BitmapFactory.decodeFile(wallpaperFile.getAbsolutePath());
            }
        }
        if (defaultWallpaper == null) {
            defaultWallpaper = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_bg);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(wallpaperFile);
            defaultWallpaper.compress(Bitmap.CompressFormat.JPEG, 70, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Bitmap getDefaultWallpaper() {
        return defaultWallpaper;
    }

    public Bitmap getPreviewWallpaper() {
        return previewWallpaper;
    }

    public void setLiveWallpaper(Activity activity) {
        if (isWallpaperSet(activity)) {
            return;
        }
        if (previewWallpaper == null) {
            decodePreviewWallpaper(activity);
        }
        try {
            settingWallpaper = true;
            decodeDefaultWallpaper(activity);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(activity.getPackageName(), LiveWallpaperService.class.getName()));
            PackageManager packageManager = activity.getPackageManager();
            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, MATCH_DEFAULT_ONLY);
            if (DeviceUtil.isOppo() && resolveInfos.size() > 1) {
                ResolveInfo resolveInfo = resolveInfos.get(1);
                intent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            }
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isWallpaperSet(Context context) {
        WallpaperManager wallpaperManager = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
        if (wallpaperManager == null) {
            return false;
        }
        WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
        return wallpaperInfo != null && context.getPackageName().equals(wallpaperInfo.getPackageName());
    }

    public void checkAppState(Context context) {
        if (settingWallpaper) {
            settingWallpaper = false;
            moveToFront(context);
//            PackageManager packageManager = context.getPackageManager();
//            try {
//                Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
//                if (intent == null && ConfigInternal.startActivity != null) {
//                    intent = new Intent(context, ConfigInternal.startActivity);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                }
//                if (intent != null) {
//                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
////                context.startActivity(intent);
//                    PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    pi.send();
//                }
//            } catch (Exception e) {
//                Log.e("Hinnka", "start error", e);
//            }
        }
    }

    public static void moveToFront(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningTaskInfo next : activityManager.getRunningTasks(200)) {
            if (next.baseActivity.getPackageName().equals(context.getPackageName())) {
                activityManager.moveTaskToFront(next.id, 0);
                activityManager.moveTaskToFront(next.id, 0);
                activityManager.moveTaskToFront(next.id, 0);
                activityManager.moveTaskToFront(next.id, 0);
                return;
            }
        }
    }

    private static class Singleton {
        private static final WallpaperEngine instance = new WallpaperEngine();
    }
}
