package com.mapcoloring.ui;

import com.mapcoloring.algorithm.BacktrackColoring;
import com.mapcoloring.algorithm.ColoringResult;
import com.mapcoloring.algorithm.GreedyColoring;
import com.mapcoloring.algorithm.SortedGreedyColoring;
import com.mapcoloring.io.MapFileHandler;
import com.mapcoloring.model.MapData;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class FrontPanel extends BorderPane {

    private MapCanvas canvas;
    private MapData chinaData;
    private MapData usaData;
    private Label colorCountLabel;
    private Label timeLabel;

    public FrontPanel() {
        canvas = new MapCanvas(750, 550);
        setCenter(canvas);

        VBox rightPanel = createRightPanel();
        setRight(rightPanel);
        BorderPane.setMargin(rightPanel, new Insets(10));
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(200);

        Label titleLabel = new Label("地图着色");
        titleLabel.setFont(new Font(18));

        Label mapLabel = new Label("选择地图");
        mapLabel.setFont(new Font(12));

        Button chinaBtn = new Button("中国地图");
        chinaBtn.setMaxWidth(Double.MAX_VALUE);
        chinaBtn.setOnAction(e -> loadChina());

        Button usaBtn = new Button("美国地图");
        usaBtn.setMaxWidth(Double.MAX_VALUE);
        usaBtn.setOnAction(e -> loadUsa());

        Separator sep1 = new Separator();

        Label algoLabel = new Label("着色算法");
        algoLabel.setFont(new Font(12));

        Button greedyBtn = new Button("贪心着色");
        greedyBtn.setMaxWidth(Double.MAX_VALUE);
        greedyBtn.setOnAction(e -> runGreedy());

        Button sortedBtn = new Button("贪心+排序");
        sortedBtn.setMaxWidth(Double.MAX_VALUE);
        sortedBtn.setOnAction(e -> runSortedGreedy());

        Button backtrackBtn = new Button("回溯求最优");
        backtrackBtn.setMaxWidth(Double.MAX_VALUE);
        backtrackBtn.setOnAction(e -> runBacktrack());

        Separator sep2 = new Separator();

        Label statsLabel = new Label("统计信息");
        statsLabel.setFont(new Font(12));

        colorCountLabel = new Label("颜色总数：--");
        timeLabel = new Label("耗时：--");

        panel.getChildren().addAll(
            titleLabel,
            mapLabel, chinaBtn, usaBtn,
            sep1,
            algoLabel, greedyBtn, sortedBtn, backtrackBtn,
            sep2,
            statsLabel, colorCountLabel, timeLabel
        );

        return panel;
    }

    private void loadChina() {
        chinaData = MapFileHandler.load("data/china.map");
        canvas.setMapData(chinaData);
    }

    private void loadUsa() {
        usaData = MapFileHandler.load("data/usa.map");
        canvas.setMapData(usaData);
    }

    private void runGreedy() {
        if (canvas.getMapData() == null) {
            showAlert("请先选择地图");
            return;
        }
        ColoringResult r = GreedyColoring.color(canvas.getMapData().graph);
        canvas.draw();
        updateStats(r);
    }

    private void runSortedGreedy() {
        if (canvas.getMapData() == null) {
            showAlert("请先选择地图");
            return;
        }
        ColoringResult r = SortedGreedyColoring.color(canvas.getMapData().graph);
        canvas.draw();
        updateStats(r);
    }

    private void runBacktrack() {
        if (canvas.getMapData() == null) {
            showAlert("请先选择地图");
            return;
        }
        ColoringResult r = BacktrackColoring.color(canvas.getMapData().graph);
        canvas.draw();
        updateStats(r);
    }

    private void updateStats(ColoringResult r) {
        colorCountLabel.setText("颜色总数：" + r.colorCount);
        timeLabel.setText(String.format("耗时：%.2f ms", r.timeMillis()));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.showAndWait();
    }
}
