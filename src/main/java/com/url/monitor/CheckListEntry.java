package com.url.monitor;

import lombok.Data;

import java.net.URL;

@Data
class CheckListEntry {

    private URL url;
    private URL originalUrl;
    private String signature;
    private long lastUpdate;
    private boolean notified;

    CheckListEntry(URL originalUrl, URL url, String content, long lastUpdate) {
        this.originalUrl = originalUrl;
        this.url = url;
        this.signature = content;
        this.lastUpdate = lastUpdate;
    }
}
