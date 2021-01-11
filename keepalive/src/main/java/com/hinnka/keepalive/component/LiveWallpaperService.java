package com.hinnka.keepalive.component;

import android.graphics.Canvas;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.hinnka.keepalive.WallpaperEngine;

public class LiveWallpaperService extends WallpaperService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public class LiveEngine extends Engine {
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            if (!isPreview()) {
                WallpaperEngine.getInstance().checkAppState(LiveWallpaperService.this);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                SurfaceHolder surfaceHolder = getSurfaceHolder();
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (isPreview()) {
                        canvas.drawBitmap(WallpaperEngine.getInstance().getPreviewWallpaper(), 0, 0, null);
                    } else {
                        canvas.drawBitmap(WallpaperEngine.getInstance().getDefaultWallpaper(), 0, 0, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new LiveEngine();
    }
}
