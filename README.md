保活以及互相唤醒SDK
===


## 初始化

```
KeepAliveManager.init(Application app, KeepAliveListener listener);
```

targetSdkVersion最高只支持26


## 锁屏监听

OPPO手机使用监听不到亮屏灭屏等ACTION，初始化保活SDK后可以通过`"KEEP_ALIVE_SCREEN_ON"`, `"KEEP_ALIVE_SCREEN_OFF"`, `"KEEP_ALIVE_USER_PRESENT"`三个ACTION监听到亮屏灭屏解锁状态


## 壁纸保活

系统不会清理有已设置动态壁纸的应用，此方法通过将用户原有壁纸设置为动态壁纸，从而在不改变用户原有壁纸的情况下实现保活

### 注意事项

* 壁纸保活没有targetSdk版本号限制，理论上所有版本都可以使用
* 需要在获得存储权限后使用，否则可能会取不到用户当前正在使用的壁纸
* 部分设备在设置完壁纸后会回到桌面，SDK已实现设置完后主动唤起App
* 用户在应用详情页主动点击强行停止会导致动态壁纸失效，需要重新设置才能生效
* 用户主动更换壁纸也会让此保活手段失效，需要重新设置才能生效



设置壁纸：

```java
KeepAliveManager.setLiveWallpaper(Activity activity);
```



判断是否已设置壁纸：

```java
boolean KeepAliveManager.isWallpaperSet(Context context);
```


## 隐藏桌面图标

默认不开启隐藏桌面图标功能，想要开启请按以下步骤操作：

1. 移除默认启动页的`<category android:name="android.intent.category.LAUNCHER" />`
2. 在app的build.gradle中开启隐藏图标功能，并配置启动页类名

```groovy
resValue "bool", "hide_launcher", "true"
resValue "string", "start_activity", "com.hinnka.keepalivedemo.MainActivity" 
```

3. 在退出App后调用`KeepAliveManager.hideLauncherIcon(Context context);`**(隐藏图标可能会导致当前正在运行的页面被关闭，建议退出后隐藏)**
4. 需要时可恢复桌面图标`KeepAliveManager.showLauncherIcon(Context context);`


## 其他（可选）

### 互相唤醒策略配置
```
public enum WakeupStrategy {
    Activity, Normal, All
}

KeepAliveConfig.getInstance().setWakeupStrategy(WakeupStrategy wakeupStrategy);
```

`Activity`: 使用1像素Activity互相唤醒，如果App内有热启动开屏Activity，需要屏蔽KeepAliveActivity，避免呼唤时带出开屏页，部分设备禁止后台弹出界面，可能导致唤醒失败

`Normal`: 使用Service和ContentProvider进行互相唤醒，部分设备不允许通过Service方式链式唤醒，部分设备进程被杀时无法调用其ContentProvider


`All`: 两种策略同时启用

默认策略为**Normal**


### 忽略电池优化
```
KeepAliveManager.openIgnoringBatteryOptimizations(Activity activity);
```

### 加入自启动白名单
```
KeepAliveManager.openWhiteListSetting(Context context);
```

### 是否支持加入自启动白名单
```
KeepAliveManager.isWhiteListAvailable(Context context);
```


