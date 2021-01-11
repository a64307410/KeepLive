package com.hinnka.keepalive;

public class KeepAliveConfig {

    private static class SingletonHolder {
        private static KeepAliveConfig instance = new KeepAliveConfig();
    }

    public static KeepAliveConfig getInstance() {
        return SingletonHolder.instance;
    }

    public enum WakeupStrategy {
        Activity, Normal, All
    }

    private boolean wakeUpApps = true;
    private WakeupStrategy wakeupStrategy = WakeupStrategy.Normal;
    private boolean logEnable = true;
    private boolean listenScreenState = true;

    public boolean isListenScreenState() {
        return listenScreenState;
    }

    public void setListenScreenState(boolean listenScreenState) {
        this.listenScreenState = listenScreenState;
    }

    public boolean isWakeUpApps() {
        return wakeUpApps;
    }

    public WakeupStrategy getWakeupStrategy() {
        return wakeupStrategy;
    }

    public boolean isLogEnable() {
        return logEnable;
    }

    public void setWakeUpApps(boolean wakeUpApps) {
        this.wakeUpApps = wakeUpApps;
    }

    public void setWakeupStrategy(WakeupStrategy wakeupStrategy) {
        this.wakeupStrategy = wakeupStrategy;
    }

    public void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable;
    }
}
