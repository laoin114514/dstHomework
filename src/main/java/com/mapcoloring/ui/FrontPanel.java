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
    private Label colorCountLabel;
    private Label timeLabel;
    private Label algoLabel;

    public FrontPanel() {
        canvas = new MapCanvas(780, 560);
        setCenter(canvas);

        VBox rightPanel = createRightPanel();
        setRight(rightPanel);
        BorderPane.setMargin(rightPanel, new Insets(10));

        // 自动加载中国地图
        loadChina();
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(200);

        Label titleLabel = new Label("中国地图着色");
        titleLabel.setFont(new Font(18));

        Separator sep1 = new Separator();

        Label algoLabelTitle = new Label("着色算法");
        algoLabelTitle.setFont(new Font(12));

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
        algoLabel = new Label("算法：--");

        Button reloadBtn = new Button("重新加载");
        reloadBtn.setMaxWidth(Double.MAX_VALUE);
        reloadBtn.setOnAction(e -> loadChina());

        panel.getChildren().addAll(
            titleLabel,
            sep1,
            algoLabelTitle, greedyBtn, sortedBtn, backtrackBtn,
            sep2,
            statsLabel, colorCountLabel, timeLabel, algoLabel,
            new Separator(),
            reloadBtn
        );

        return panel;
    }

    private void loadChina() {
        chinaData = MapFileHandler.load("data/china.map");
        canvas.setMapData(chinaData);
        colorCountLabel.setText("颜色总数：--");
        timeLabel.setText("耗时：--");
        algoLabel.setText("算法：--");
    }

    private void runGreedy() {
        ColoringResult r = GreedyColoring.color(chinaData.graph);
        canvas.draw();
        updateStats(r, "贪心着色");
    }

    private void runSortedGreedy() {
        ColoringResult r = SortedGreedyColoring.color(chinaData.graph);
        canvas.draw();
        updateStats(r, "贪心+排序");
    }

    private void runBacktrack() {
        ColoringResult r = BacktrackColoring.color(chinaData.graph);
        canvas.draw();
        updateStats(r, "回溯求最优");
    }

    private void updateStats(ColoringResult r, String algoName) {
        colorCountLabel.setText("颜色总数：" + r.colorCount);
        timeLabel.setText(String.format("耗时：%.2f ms", r.timeMillis()));
        algoLabel.setText("算法：" + algoName);
    }
}
