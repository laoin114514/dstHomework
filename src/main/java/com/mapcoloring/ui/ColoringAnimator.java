package com.mapcoloring.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;

/**
 * 驱动着色步骤的逐帧播放。
 * 使用 JavaFX Timeline，每 delayMs 毫秒执行一个步骤。
 */
public class ColoringAnimator {

    private final MapCanvas canvas;
    private Timeline timeline;
    private List<ColoringStep> steps;
    private int currentStep;
    private long delayMs;
    private Runnable onFinished;
    private Runnable onStep;       // 每步回调，用于更新 UI 进度

    public ColoringAnimator(MapCanvas canvas) {
        this.canvas = canvas;
        this.delayMs = 300; // 默认 300ms
    }

    /**
     * 开始播放步骤列表。
     * @param steps 步骤列表
     * @param onFinished 播放完成回调
     * @param onStep 每步回调（用于更新步骤进度显示）
     */
    public void play(List<ColoringStep> steps,
                     Runnable onFinished, Runnable onStep) {
        if (steps == null || steps.isEmpty()) {
            if (onFinished != null) onFinished.run();
            return;
        }

        this.steps = steps;
        this.currentStep = 0;
        this.onFinished = onFinished;
        this.onStep = onStep;

        timeline = new Timeline(
            new KeyFrame(Duration.millis(delayMs), e -> onTick())
        );
        timeline.setCycleCount(steps.size());
        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        timeline.play();
    }

    private void onTick() {
        if (currentStep >= steps.size()) {
            timeline.stop();
            return;
        }

        ColoringStep step = steps.get(currentStep);
        switch (step.action) {
            case TRY:
            case COMMIT:
                canvas.colorProvince(step.provinceIndex, step.color);
                break;
            case UNDO:
                canvas.uncolorProvince(step.provinceIndex);
                break;
        }

        currentStep++;
        if (onStep != null) onStep.run();
    }

    public void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    public void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    /**
     * 调整播放速度。
     * @param delayMs 步骤间隔毫秒数，范围 30~1000
     */
    public void setSpeed(long delayMs) {
        this.delayMs = Math.max(30, Math.min(1000, delayMs));
    }

    public boolean isPlaying() {
        return timeline != null
            && timeline.getStatus() == Timeline.Status.RUNNING;
    }

    public boolean isPaused() {
        return timeline != null
            && timeline.getStatus() == Timeline.Status.PAUSED;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return steps != null ? steps.size() : 0;
    }
}
