package com.bilibili.model;

import java.util.List;

public class AudioStream {
    private String url;
    private List<String> backupUrls;
    private String codec;
    private int quality;
    private long bandWidth;

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getBackupUrls() {
        return backupUrls;
    }

    public void setBackupUrls(List<String> backupUrls) {
        this.backupUrls = backupUrls;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public long getBandWidth() {
        return bandWidth;
    }

    public void setBandWidth(long bandWidth) {
        this.bandWidth = bandWidth;
    }
} 