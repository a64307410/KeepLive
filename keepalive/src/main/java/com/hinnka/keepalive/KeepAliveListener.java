package com.hinnka.keepalive;

import java.util.Map;

public interface KeepAliveListener {
    void trackEvent(String eventCode, String eventName, Map<String, Object> map);
}
