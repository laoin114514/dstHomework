package com.mapcoloring.algorithm;

public class ColoringResult {
    public int colorCount;
    public long timeNanos;

    public ColoringResult(int colorCount, long timeNanos) {
        this.colorCount = colorCount;
        this.timeNanos = timeNanos;
    }

    public double timeMillis() {
        return timeNanos / 1_000_000.0;
    }
}
