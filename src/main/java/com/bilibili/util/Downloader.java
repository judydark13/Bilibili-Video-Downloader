package com.bilibili.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class Downloader {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final int BUFFER_SIZE = 8192;

    public interface ProgressCallback {
        void onProgress(long downloaded, long total);
    }

    public static void download(String url, String savePath, ProgressCallback callback) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Referer", "https://www.bilibili.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("下载失败: " + response);
            }

            long contentLength = response.body().contentLength();
            try (InputStream is = response.body().byteStream();
                 FileOutputStream fos = new FileOutputStream(savePath)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                long downloaded = 0;
                int read;

                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                    downloaded += read;
                    if (callback != null) {
                        callback.onProgress(downloaded, contentLength);
                    }
                }
            }
        }
    }
} 