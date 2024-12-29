package com.bilibili.gui;

import com.bilibili.api.BiliAPI;
import com.bilibili.model.AudioStream;
import com.bilibili.model.VideoInfo;
import com.bilibili.model.VideoStream;
import com.bilibili.util.Config;
import com.bilibili.util.Downloader;
import com.bilibili.util.MediaMerger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {
    @FXML private TextField urlInput;
    @FXML private VBox videoInfoPane;
    @FXML private Label titleText;
    @FXML private Label uploaderText;
    @FXML private ComboBox<Quality> qualityComboBox;
    @FXML private TextField savePathInput;
    @FXML private Button downloadButton;
    @FXML private VBox progressPane;
    @FXML private ProgressBar downloadProgress;
    @FXML private Label progressText;
    @FXML private TextArea logArea;

    private VideoInfo currentVideo;
    private List<VideoStream> videoStreams;
    private List<AudioStream> audioStreams;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static class Quality {
        private final int id;
        private final String name;
        private final VideoStream stream;

        public Quality(int id, String name, VideoStream stream) {
            this.id = id;
            this.name = name;
            this.stream = stream;
        }

        @Override
        public String toString() {
            double bandwidthMbps = stream.getBandWidth() / 1000000.0;
            String codec = stream.getCodec().split("\\.")[0];
            String resolution = String.format("%dx%d", stream.getWidth(), stream.getHeight());
            return String.format("%s (%s, %s, 码率: %.3fMbps)", name, codec, resolution, bandwidthMbps);
        }

        public VideoStream getStream() {
            return stream;
        }
    }

    @FXML
    private void initialize() {
        clearVideoInfo();
        
        // 从配置文件加载SESSDATA
        String sessdata = Config.getSESSDATA();
        if (!sessdata.isEmpty()) {
            BiliAPI.setSESSDATA(sessdata);
            log("已从配置文件加载SESSDATA");
        }
    }

    @FXML
    private void onParseVideo() {
        String url = urlInput.getText().trim();
        if (url.isEmpty()) {
            showError("请输入视频链接");
            return;
        }

        setUIEnabled(false);
        clearVideoInfo();

        executorService.submit(() -> {
            try {
                String videoId = BiliAPI.extractVideoId(url);
                currentVideo = BiliAPI.getVideoInfo(videoId);
                videoStreams = BiliAPI.getVideoStreams(currentVideo);
                audioStreams = BiliAPI.getAudioStreams(currentVideo);

                Platform.runLater(() -> {
                    titleText.setText(currentVideo.getTitle());
                    uploaderText.setText(currentVideo.getOwner());
                    videoInfoPane.setVisible(true);

                    String defaultPath = System.getProperty("user.home") + File.separator + "Downloads" 
                            + File.separator + sanitizeFileName(currentVideo.getTitle()) + ".mp4";
                    savePathInput.setText(defaultPath);

                    qualityComboBox.getItems().clear();
                    for (VideoStream stream : videoStreams) {
                        qualityComboBox.getItems().add(new Quality(
                                stream.getQuality(),
                                getQualityName(stream.getQuality(), stream.getWidth(), stream.getHeight()),
                                stream
                        ));
                    }
                    if (!qualityComboBox.getItems().isEmpty()) {
                        qualityComboBox.getSelectionModel().selectFirst();
                    }

                    downloadButton.setDisable(false);
                    setUIEnabled(true);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("解析视频失败: " + e.getMessage());
                    setUIEnabled(true);
                });
            }
        });
    }

    @FXML
    private void onBrowseSavePath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择保存目录");
        
        String currentPath = savePathInput.getText();
        if (!currentPath.isEmpty()) {
            File currentFile = new File(currentPath);
            if (currentFile.getParentFile() != null && currentFile.getParentFile().exists()) {
                directoryChooser.setInitialDirectory(currentFile.getParentFile());
            }
        }

        Window window = savePathInput.getScene().getWindow();
        File directory = directoryChooser.showDialog(window);
        
        if (directory != null) {
            String fileName = sanitizeFileName(currentVideo.getTitle()) + ".mp4";
            savePathInput.setText(directory.getAbsolutePath() + File.separator + fileName);
        }
    }

    @FXML
    private void onStartDownload() {
        if (currentVideo == null || qualityComboBox.getSelectionModel().isEmpty()) {
            showError("请先解析视频");
            return;
        }

        String savePath = savePathInput.getText().trim();
        if (savePath.isEmpty()) {
            showError("请选择保存路径");
            return;
        }

        Platform.runLater(() -> {
            setUIEnabled(false);
            progressPane.setVisible(true);
            downloadProgress.setProgress(0);
            progressText.setText("0%");
        });

        executorService.submit(() -> {
            try {
                VideoStream videoStream = qualityComboBox.getSelectionModel().getSelectedItem().getStream();
                AudioStream audioStream = audioStreams.get(0);

                String tempDir = System.getProperty("java.io.tmpdir");
                String tempVideoPath = tempDir + File.separator + "video_temp_" + System.currentTimeMillis() + ".m4s";
                String tempAudioPath = tempDir + File.separator + "audio_temp_" + System.currentTimeMillis() + ".m4s";

                Platform.runLater(() -> log("开始下载视频流..."));
                downloadStream(videoStream.getUrl(), tempVideoPath, 0.0, 0.4);

                Platform.runLater(() -> log("开始下载音频流..."));
                downloadStream(audioStream.getUrl(), tempAudioPath, 0.4, 0.8);

                Platform.runLater(() -> log("开始合并音视频..."));
                MediaMerger.mergeVideoAudio(tempVideoPath, tempAudioPath, savePath, progress -> {
                    double totalProgress = 0.8 + progress * 0.2;
                    Platform.runLater(() -> updateProgress(totalProgress));
                });

                Platform.runLater(() -> {
                    updateProgress(1.0);
                    log("下载完成！保存在：" + savePath);
                    setUIEnabled(true);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("下载失败: " + e.getMessage());
                    setUIEnabled(true);
                });
            }
        });
    }

    private void downloadStream(String url, String savePath, double progressStart, double progressEnd) throws IOException {
        Downloader.download(url, savePath, (downloaded, total) -> {
            double progress = total > 0 ? (double) downloaded / total : 0;
            double totalProgress = progressStart + progress * (progressEnd - progressStart);
            Platform.runLater(() -> updateProgress(totalProgress));
        });
    }

    private void updateProgress(double progress) {
        downloadProgress.setProgress(progress);
        progressText.setText(String.format("%.1f%%", progress * 100));
    }

    private void setUIEnabled(boolean enabled) {
        urlInput.setDisable(!enabled);
        qualityComboBox.setDisable(!enabled);
        savePathInput.setDisable(!enabled);
        downloadButton.setDisable(!enabled);
    }

    private void clearVideoInfo() {
        Platform.runLater(() -> {
            videoInfoPane.setVisible(false);
            progressPane.setVisible(false);
            titleText.setText("");
            uploaderText.setText("");
            qualityComboBox.getItems().clear();
            currentVideo = null;
            videoStreams = null;
            audioStreams = null;
        });
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String getQualityName(int quality, int width, int height) {
        String qualityName;
        switch (quality) {
            case 116 -> qualityName = "1080P 60fps";
            case 80 -> qualityName = "1080P";
            case 64 -> qualityName = "720P";
            case 32 -> qualityName = "480P";
            case 16 -> qualityName = "360P";
            default -> qualityName = width + "x" + height;
        }
        return qualityName;
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            log("错误：" + message);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @FXML
    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("使用帮助");
        alert.setHeaderText("Bilibili 视频下载器使用说明");
        
        String content = """
            1. 输入视频链接
               • 支持BV号（例如：BV1xx411c7mD）
               • 支持完整链接（例如：https://www.bilibili.com/video/BV1xx411c7mD）
               
            2. 点击"解析"按钮
               • 软件会自动获取视频信息
               • 显示视频标题和UP主
               • 列出可用的视频质量选项
               
            3. 选择下载选项
               • 从下拉菜单选择视频质量
               • 选择或修改保存路径
               • 点击"浏览"可以更改保存位置
               
            4. 开始下载
               • 点击"开始下载"按钮
               • 进度条显示下载进度
               • 日志区域显示详细信息
               
            5. 高清视频下载
               • 点击"设置SESSDATA"按钮
               • 按说明获取并设置你的SESSDATA
               • SESSDATA决定可下载的最高画质
               
            6. 注意事项
               • 下载的视频包含音频和视频
               • 支持视频进度条拖动播放
               • 保存路径不要包含特殊字符
               • SESSDATA有效期与登录状态相同
               
            7. 常见问题
               • 如果解析失败，请检查视频链接是否正确
               • 如果下载失败，请尝试选择其他视频质量
               • 确保有足够的磁盘空间
            """;
        
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(50);
        textArea.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");
        
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setStyle("-fx-font-family: 'Microsoft YaHei';");
        alert.showAndWait();
    }

    @FXML
    private void onSetSESSDATA() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("SESSDATA 设置");
        dialog.setHeaderText("管理SESSDATA配置");
        
        // 创建对话框内容
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField sessdataField = new TextField(Config.getSESSDATA());
        sessdataField.setMinWidth(300);
        
        grid.add(new Label("当前SESSDATA:"), 0, 0);
        grid.add(sessdataField, 1, 0);
        
        // 添加说明文本
        TextArea helpText = new TextArea("""
            如何获取SESSDATA：
            1. 在B站网页版登录你的账号
            2. 按F12打开开发者工具，切换到Application（应用）标签
            3. 选择Cookie，点击https://www.bilibili.com, 找到SESSDATA
            4. 复制SESSDATA的值（形如：abc123%2C1234567%2Cdef456*11）
            
            注意：
            - 视频清晰度取决于你的账号权限（是否为大会员）
            - SESSDATA有效期与你的登录状态相同
            - 留空则使用默认配置（仅支持低清晰度,b站不登陆默认最高只能看480P😭）
            """);
        helpText.setEditable(false);
        helpText.setWrapText(true);
        helpText.setPrefRowCount(8);
        helpText.setStyle("-fx-font-size: 12px;");
        
        grid.add(helpText, 0, 1, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // 添加按钮
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType clearButtonType = new ButtonType("清除", ButtonBar.ButtonData.LEFT);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, clearButtonType, cancelButtonType);
        
        // 处理按钮点击
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newSessdata = sessdataField.getText().trim();
                Config.setSESSDATA(newSessdata);
                BiliAPI.setSESSDATA(newSessdata);
                log("SESSDATA已保存到配置文件");
                return ButtonType.OK;
            }
            if (dialogButton == clearButtonType) {
                Config.setSESSDATA("");
                BiliAPI.setSESSDATA("");
                log("SESSDATA已清除");
                return ButtonType.OK;
            }
            return dialogButton;
        });
        
        dialog.showAndWait();
    }
} 