package com.mapcoloring.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        FrontPanel frontPanel = new FrontPanel();
        Tab frontTab = new Tab("地图着色", frontPanel);
        frontTab.setClosable(false);

        AdminPanel adminPanel = new AdminPanel();
        Tab adminTab = new Tab("数据管理", adminPanel);
        adminTab.setClosable(false);

        tabPane.getTabs().addAll(frontTab, adminTab);

        Scene scene = new Scene(tabPane, 1000, 620);
        primaryStage.setTitle("地图着色系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
