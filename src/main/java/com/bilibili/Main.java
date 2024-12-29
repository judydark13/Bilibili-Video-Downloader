package com.bilibili;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bilibili/gui/MainWindow.fxml"));
            Parent root = loader.load();
            
            // 设置窗口图标
            var iconUrl = getClass().getResource("/com/bilibili/gui/favicon.png");
            if (iconUrl != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconUrl.toString()));
            }
            
            primaryStage.setTitle("B站视频下载器");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 