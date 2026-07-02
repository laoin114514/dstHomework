package com.mapcoloring.algorithm;

import com.mapcoloring.ui.ColoringStep;
import java.util.List;

public class ColoringResult {
    public int colorCount;
    public long timeNanos;
    public List<ColoringStep> steps;  // 新增：步骤列表，瞬间模式为 null

    public ColoringResult(int colorCount, long timeNanos) {
        this.colorCount = colorCount;
        this.timeNanos = timeNanos;
        this.steps = null;
    }

    public ColoringResult(int colorCount, long timeNanos,
                          List<ColoringStep> steps) {
        this.colorCount = colorCount;
        this.timeNanos = timeNanos;
        this.steps = steps;
    }

    public double timeMillis() {
        return timeNanos / 1_000_000.0;
    }
}
