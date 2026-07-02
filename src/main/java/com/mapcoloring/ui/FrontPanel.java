package com.mapcoloring.ui;

import com.mapcoloring.algorithm.BacktrackColoring;
import com.mapcoloring.algorithm.ColoringResult;
import com.mapcoloring.algorithm.GreedyColoring;
import com.mapcoloring.algorithm.SortedGreedyColoring;
import com.mapcoloring.io.MapFileHandler;
import com.mapcoloring.model.MapData;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class FrontPanel extends BorderPane {

    private MapCanvas canvas;
    private MapData chinaData;
    private ColoringAnimator animator;
    private boolean animationMode = false; // false=瞬间, true=动画

    private Label colorCountLabel;
    private Label timeLabel;
    private Label algoLabel;
    private Label stepLabel;

    private Button greedyBtn;
    private Button sortedBtn;
    private Button backtrackBtn;
    private Button reloadBtn;
    private Slider speedSlider;
    private Label speedLabel;

    private ToggleButton instantToggle;
    private ToggleButton animToggle;
    private ToggleGroup modeGroup;

    public FrontPanel() {
        canvas = new MapCanvas();
        animator = new ColoringAnimator(canvas);
        setCenter(canvas);

        VBox rightPanel = createRightPanel();
        setRight(rightPanel);
        BorderPane.setMargin(rightPanel, new Insets(10));

        loadChina();
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(210);

        Label titleLabel = new Label("中国地图着色");
        titleLabel.setFont(new Font(18));

        Separator sep1 = new Separator();

        // === 着色算法按钮 ===
        Label algoLabelTitle = new Label("着色算法");
        algoLabelTitle.setFont(new Font(12));

        greedyBtn = new Button("贪心着色");
        greedyBtn.setMaxWidth(Double.MAX_VALUE);
        greedyBtn.setOnAction(e -> runGreedy());

        sortedBtn = new Button("贪心+排序");
        sortedBtn.setMaxWidth(Double.MAX_VALUE);
        sortedBtn.setOnAction(e -> runSortedGreedy());

        backtrackBtn = new Button("回溯求最优");
        backtrackBtn.setMaxWidth(Double.MAX_VALUE);
        backtrackBtn.setOnAction(e -> runBacktrack());

        Separator sep2 = new Separator();

        // === 模式切换 ===
        Label modeLabel = new Label("着色模式");
        modeLabel.setFont(new Font(12));

        modeGroup = new ToggleGroup();
        instantToggle = new ToggleButton("瞬间着色");
        instantToggle.setMaxWidth(Double.MAX_VALUE);
        instantToggle.setToggleGroup(modeGroup);
        instantToggle.setSelected(true);
        instantToggle.setOnAction(e -> {
            animationMode = false;
            speedSlider.setDisable(true);
        });

        animToggle = new ToggleButton("动画着色");
        animToggle.setMaxWidth(Double.MAX_VALUE);
        animToggle.setToggleGroup(modeGroup);
        animToggle.setOnAction(e -> {
            animationMode = true;
            speedSlider.setDisable(false);
        });

        // === 速度滑块 ===
        Label speedTitleLabel = new Label("动画速度");
        speedTitleLabel.setFont(new Font(12));

        speedSlider = new Slider(30, 1000, 300);
        speedSlider.setDisable(true);
        speedSlider.setBlockIncrement(10);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            animator.setSpeed(newVal.longValue());
            speedLabel.setText((int) Math.round(newVal.doubleValue()) + " ms");
        });

        speedLabel = new Label("300 ms");

        HBox speedRange = new HBox();
        speedRange.setSpacing(4);
        Label fastLabel = new Label("快");
        Label slowLabel = new Label("慢");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        speedRange.getChildren().addAll(fastLabel, spacer, slowLabel);

        Separator sep3 = new Separator();

        // === 统计信息 ===
        Label statsLabel = new Label("统计信息");
        statsLabel.setFont(new Font(12));

        colorCountLabel = new Label("颜色总数：--");
        timeLabel = new Label("耗时：--");
        algoLabel = new Label("算法：--");
        stepLabel = new Label("");

        reloadBtn = new Button("重新加载");
        reloadBtn.setMaxWidth(Double.MAX_VALUE);
        reloadBtn.setOnAction(e -> {
            animator.stop();
            loadChina();
        });

        panel.getChildren().addAll(
            titleLabel,
            sep1,
            algoLabelTitle, greedyBtn, sortedBtn, backtrackBtn,
            sep2,
            modeLabel, instantToggle, animToggle,
            speedTitleLabel, speedSlider, speedLabel, speedRange,
            sep3,
            statsLabel, colorCountLabel, timeLabel, algoLabel, stepLabel,
            new Separator(),
            reloadBtn
        );

        return panel;
    }

    private void loadChina() {
        chinaData = MapFileHandler.load("data/china.map");
        canvas.setMapData(chinaData);
        resetStats();
    }

    private void resetStats() {
        colorCountLabel.setText("颜色总数：--");
        timeLabel.setText("耗时：--");
        algoLabel.setText("算法：--");
        stepLabel.setText("");
    }

    // === 算法执行入口 ===

    private void runGreedy() {
        ColoringResult r = GreedyColoring.color(chinaData.graph);
        handleResult(r, "贪心着色");
    }

    private void runSortedGreedy() {
        ColoringResult r = SortedGreedyColoring.color(chinaData.graph);
        handleResult(r, "贪心+排序");
    }

    private void runBacktrack() {
        ColoringResult r = BacktrackColoring.color(chinaData.graph);
        handleResult(r, "回溯求最优");
    }

    private void handleResult(ColoringResult r, String algoName) {
        if (animationMode && r.steps != null && !r.steps.isEmpty()) {
            // 动画模式：先重置图颜色，再通过动画逐步着色
            chinaData.graph.resetColors();
            canvas.draw();
            setButtonsDisabled(true);
            stepLabel.setText("步骤：0 / " + r.steps.size());

            animator.setSpeed((long) speedSlider.getValue());
            animator.play(r.steps, () -> {
                // 播放完成
                Platform.runLater(() -> {
                    updateStats(r, algoName);
                    setButtonsDisabled(false);
                    stepLabel.setText("步骤：" + r.steps.size()
                            + " / " + r.steps.size());
                });
            }, () -> {
                // 每步回调：更新进度
                Platform.runLater(() -> {
                    stepLabel.setText("步骤：" + animator.getCurrentStep()
                            + " / " + animator.getTotalSteps());
                });
            });
        } else {
            // 瞬间模式：算法已着色 graph，直接重绘画布
            canvas.draw();
            updateStats(r, algoName);
        }
    }

    private void updateStats(ColoringResult r, String algoName) {
        colorCountLabel.setText("颜色总数：" + r.colorCount);
        timeLabel.setText(String.format("耗时：%.2f ms", r.timeMillis()));
        algoLabel.setText("算法：" + algoName);
    }

    private void setButtonsDisabled(boolean disabled) {
        greedyBtn.setDisable(disabled);
        sortedBtn.setDisable(disabled);
        backtrackBtn.setDisable(disabled);
        reloadBtn.setDisable(disabled);
        instantToggle.setDisable(disabled);
        animToggle.setDisable(disabled);
    }
}
