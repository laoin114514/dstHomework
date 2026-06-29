package com.mapcoloring.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        FrontPanel frontPanel = new FrontPanel();
        Scene scene = new Scene(frontPanel, 1000, 620);
        primaryStage.setTitle("中国地图着色");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
