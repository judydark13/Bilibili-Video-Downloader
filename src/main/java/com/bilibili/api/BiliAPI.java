package com.bilibili.api;

import com.bilibili.model.AudioStream;
import com.bilibili.model.VideoInfo;
import com.bilibili.model.VideoStream;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiliAPI {
    private static final String VIDEO_INFO_API = "https://api.bilibili.com/x/web-interface/view";
    private static final String VIDEO_STREAM_API = "https://api.bilibili.com/x/player/playurl";
    private static final Pattern BV_PATTERN = Pattern.compile("BV[0-9A-Za-z]{10}");
    private static final Gson gson = new Gson();
    private static String SESSDATA = null;  // 用户的SESSDATA cookie
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static void setSESSDATA(String sessdata) {
        SESSDATA = sessdata;
    }

    private static Request.Builder createRequestBuilder(String url) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://www.bilibili.com");
        
        if (SESSDATA != null && !SESSDATA.isEmpty()) {
            builder.header("Cookie", "SESSDATA=" + SESSDATA);
        }
        
        return builder;
    }

    public static String extractVideoId(String url) throws IOException {
        Matcher matcher = BV_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new IOException("无效的视频链接");
    }

    public static VideoInfo getVideoInfo(String bvid) throws IOException {
        String url = VIDEO_INFO_API + "?bvid=" + bvid;
        Request request = createRequestBuilder(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response);
            }

            String responseBody = response.body().string();
            JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            int code = jsonObject.get("code").getAsInt();
            if (code != 0) {
                String message = jsonObject.has("message") ? 
                        jsonObject.get("message").getAsString() : "未知错误";
                throw new IOException("API返回错误: " + message);
            }

            JsonObject data = jsonObject.getAsJsonObject("data");
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setBvid(data.get("bvid").getAsString());
            videoInfo.setAid(data.get("aid").getAsString());
            videoInfo.setCid(data.get("cid").getAsString());
            videoInfo.setTitle(data.get("title").getAsString());
            videoInfo.setDescription(data.get("desc").getAsString());
            videoInfo.setPicture(data.get("pic").getAsString());
            videoInfo.setPubdate(data.get("pubdate").getAsInt());
            videoInfo.setOwner(data.getAsJsonObject("owner").get("name").getAsString());

            return videoInfo;
        }
    }

    public static List<VideoStream> getVideoStreams(VideoInfo videoInfo) throws IOException {
        String url = VIDEO_STREAM_API + "?bvid=" + videoInfo.getBvid() + "&cid=" + videoInfo.getCid() + "&fnval=4048";
        Request request = createRequestBuilder(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response);
            }

            String responseBody = response.body().string();
            JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            int code = jsonObject.get("code").getAsInt();
            if (code != 0) {
                String message = jsonObject.has("message") ? 
                        jsonObject.get("message").getAsString() : "未知错误";
                throw new IOException("API返回错误: " + message);
            }

            JsonObject data = jsonObject.getAsJsonObject("data");
            if (!data.has("dash")) {
                throw new IOException("该视频不支持DASH格式");
            }

            JsonObject dash = data.getAsJsonObject("dash");
            List<VideoStream> videoStreams = new ArrayList<>();

            if (dash.has("video") && !dash.get("video").isJsonNull()) {
                JsonArray videos = dash.getAsJsonArray("video");
                for (JsonElement videoElement : videos) {
                    JsonObject video = videoElement.getAsJsonObject();
                    VideoStream stream = new VideoStream();
                    stream.setUrl(video.get("base_url").getAsString());
                    stream.setCodec(video.get("codecs").getAsString());
                    stream.setWidth(video.get("width").getAsInt());
                    stream.setHeight(video.get("height").getAsInt());
                    stream.setQuality(video.get("id").getAsInt());
                    stream.setBandWidth(video.get("bandwidth").getAsLong());

                    JsonElement backupUrlElement = video.get("backup_url");
                    if (backupUrlElement != null && !backupUrlElement.isJsonNull()) {
                        JsonArray backupUrls = backupUrlElement.getAsJsonArray();
                        List<String> backupList = new ArrayList<>();
                        for (JsonElement backupUrl : backupUrls) {
                            backupList.add(backupUrl.getAsString());
                        }
                        stream.setBackupUrls(backupList);
                    }

                    videoStreams.add(stream);
                }
            }

            return videoStreams;
        }
    }

    public static List<AudioStream> getAudioStreams(VideoInfo videoInfo) throws IOException {
        String url = VIDEO_STREAM_API + "?bvid=" + videoInfo.getBvid() + "&cid=" + videoInfo.getCid() + "&fnval=4048";
        Request request = createRequestBuilder(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response);
            }

            String responseBody = response.body().string();
            JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            int code = jsonObject.get("code").getAsInt();
            if (code != 0) {
                String message = jsonObject.has("message") ? 
                        jsonObject.get("message").getAsString() : "未知错误";
                throw new IOException("API返回错误: " + message);
            }

            JsonObject data = jsonObject.getAsJsonObject("data");
            if (!data.has("dash")) {
                throw new IOException("该视频不支持DASH格式");
            }

            JsonObject dash = data.getAsJsonObject("dash");
            List<AudioStream> audioStreams = new ArrayList<>();

            if (dash.has("audio") && !dash.get("audio").isJsonNull()) {
                JsonArray audios = dash.getAsJsonArray("audio");
                for (JsonElement audioElement : audios) {
                    JsonObject audio = audioElement.getAsJsonObject();
                    AudioStream stream = new AudioStream();
                    stream.setUrl(audio.get("base_url").getAsString());
                    stream.setCodec(audio.get("codecs").getAsString());
                    stream.setQuality(audio.get("id").getAsInt());
                    stream.setBandWidth(audio.get("bandwidth").getAsLong());

                    JsonElement backupUrlElement = audio.get("backup_url");
                    if (backupUrlElement != null && !backupUrlElement.isJsonNull()) {
                        JsonArray backupUrls = backupUrlElement.getAsJsonArray();
                        List<String> backupList = new ArrayList<>();
                        for (JsonElement backupUrl : backupUrls) {
                            backupList.add(backupUrl.getAsString());
                        }
                        stream.setBackupUrls(backupList);
                    }

                    audioStreams.add(stream);
                }
            }

            return audioStreams;
        }
    }
} 