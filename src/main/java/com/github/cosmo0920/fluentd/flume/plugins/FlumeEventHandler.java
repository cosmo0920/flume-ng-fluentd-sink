package com.github.cosmo0920.fluentd.flume.plugins;
import org.apache.flume.Event;

import java.util.Map;

public class FlumeEventHandler {
    private final Event event;

    FlumeEventHandler(Event event) {
        this.event = event;
    }

    public boolean containsHeader(String headerName) {
        return event.getHeaders().containsKey(headerName);
    }

    public long getHeader(String headerName) {
        Map<String, String> headerMap = event.getHeaders();
        return Long.parseLong(headerMap.get(headerName));
    }
}
