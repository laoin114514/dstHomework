package com.mapcoloring.ui;

import java.util.List;

/**
 * 着色过程中的单个步骤，用于动画播放。
 * 三种动作：TRY（试探着色）、COMMIT（确认着色）、UNDO（撤销着色）
 */
public class ColoringStep {

    public enum Action {
        TRY,    // 回溯试探：尝试给省份着色
        COMMIT, // 确认着色：贪心每步 / 回溯找到可行解后确认
        UNDO    // 撤销着色：回溯回退时取消之前的试探
    }

    public Action action;
    public int provinceIndex;
    public int color;       // UNDO 时忽略
    public String description;

    public ColoringStep(Action action, int provinceIndex, int color,
                        String provinceName) {
        this.action = action;
        this.provinceIndex = provinceIndex;
        this.color = color;
        this.description = buildDescription(provinceName);
    }

    private String buildDescription(String name) {
        switch (action) {
            case TRY:    return name + " ← 颜色" + color + " (尝试)";
            case COMMIT: return name + " ← 颜色" + color;
            case UNDO:   return name + " 撤销着色";
            default:     return "";
        }
    }

    /**
     * 从步骤列表提取最终颜色状态，应用到 graph。
     * 遍历所有 COMMIT 步骤，取每个省份最后一次 COMMIT 的颜色。
     */
    public static void applyFinalColors(
            List<ColoringStep> steps,
            com.mapcoloring.model.Graph graph) {
        for (ColoringStep step : steps) {
            if (step.action == Action.COMMIT) {
                graph.setColor(step.provinceIndex, step.color);
            }
        }
    }
}
