package com.bilibili.util;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MediaMerger {
    public interface ProgressCallback {
        void onProgress(double progress);
    }

    public static void mergeVideoAudio(String videoPath, String audioPath, String outputPath, ProgressCallback callback) throws Exception {
        try {
            if (callback != null) {
                callback.onProgress(0.1);
            }

            // 读取视频和音频文件
            Movie video = MovieCreator.build(videoPath);
            Movie audio = MovieCreator.build(audioPath);

            if (callback != null) {
                callback.onProgress(0.3);
            }

            // 创建新的视频容器
            Movie result = new Movie();

            // 添加所有轨道
            List<Track> videoTracks = video.getTracks();
            List<Track> audioTracks = audio.getTracks();

            for (Track track : videoTracks) {
                result.addTrack(track);
            }

            if (callback != null) {
                callback.onProgress(0.5);
            }

            for (Track track : audioTracks) {
                result.addTrack(track);
            }

            if (callback != null) {
                callback.onProgress(0.7);
            }

            // 使用 FragmentedMp4Builder 来构建 MP4 文件
            // 这种方式会正确处理 moov 原子的位置
            FragmentedMp4Builder fragmentedBuilder = new FragmentedMp4Builder();
            
            // 构建最终的 MP4 文件
            com.coremedia.iso.boxes.Container container = fragmentedBuilder.build(result);

            if (callback != null) {
                callback.onProgress(0.9);
            }

            // 写入输出文件
            FileChannel fc = new FileOutputStream(outputPath).getChannel();
            container.writeContainer(fc);
            fc.close();

            if (callback != null) {
                callback.onProgress(1.0);
            }

            // 删除临时文件
            new File(videoPath).delete();
            new File(audioPath).delete();
        } catch (Exception e) {
            throw new Exception("合并音视频失败: " + e.getMessage(), e);
        }
    }
} 