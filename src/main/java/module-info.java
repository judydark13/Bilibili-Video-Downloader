module com.bilibili {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires okhttp3;
    requires okio;
    requires kotlin.stdlib;
    requires isoparser;

    opens com.bilibili to javafx.fxml;
    opens com.bilibili.gui to javafx.fxml;
    opens com.bilibili.model to com.google.gson;
    
    exports com.bilibili;
    exports com.bilibili.gui;
    exports com.bilibili.model;
    exports com.bilibili.api;
    exports com.bilibili.util;
} 