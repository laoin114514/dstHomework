package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;
import com.mapcoloring.ui.ColoringStep;
import java.util.ArrayList;
import java.util.List;

public class BacktrackColoring {

    private static final int MAX_STEPS = 50000;

    private Graph graph;
    private int bestColorCount;
    private int[] bestColors;
    private List<ColoringStep> bestSteps;

    public static ColoringResult color(Graph g) {
        BacktrackColoring solver = new BacktrackColoring();
        return solver.solve(g);
    }

    private ColoringResult solve(Graph g) {
        long start = System.nanoTime();
        this.graph = g;
        int n = g.getVertexCount();
        bestColorCount = n;
        bestColors = new int[n];
        bestSteps = null;

        // 先用排序贪心得到上界
        ColoringResult greedyResult = SortedGreedyColoring.color(g);
        int upperBound = greedyResult.colorCount;
        for (int i = 0; i < n; i++) {
            bestColors[i] = g.getColor(i);
        }
        // +1 保证第一轮回溯可以使用完整的 upperBound 种颜色
        bestColorCount = upperBound + 1;

        // 外层循环：从贪心上界开始逐步尝试更少的颜色
        for (int k = upperBound; k >= 1; k--) {
            g.resetColors();
            int[] currentColors = new int[n];
            List<ColoringStep> roundSteps = new ArrayList<>();

            boolean found = backtrack(0, k, currentColors, roundSteps);

            if (found) {
                // 追加 COMMIT 步骤确认该轮解
                for (int i = 0; i < n; i++) {
                    roundSteps.add(new ColoringStep(
                        ColoringStep.Action.COMMIT,
                        i, currentColors[i], g.getVertexName(i)));
                }
                bestSteps = roundSteps;
                bestColorCount = k;
                System.arraycopy(currentColors, 0, bestColors, 0, n);
            } else {
                break; // 无法用更少的颜色，结束搜索
            }

            // 安全阈值：步骤过多时停止
            if (roundSteps.size() > MAX_STEPS) {
                break;
            }
        }

        // 将最佳颜色应用到 graph
        for (int i = 0; i < n; i++) {
            g.setColor(i, bestColors[i]);
        }

        long elapsed = System.nanoTime() - start;
        return new ColoringResult(bestColorCount, elapsed, bestSteps);
    }

    /**
     * 递归回溯搜索。在每个顶点尝试颜色 1..maxColors。
     * 成功时返回 true，失败时返回 false。
     * 试探着色记录 TRY 步骤，回退记录 UNDO 步骤。
     */
    private boolean backtrack(int idx, int maxColors,
                              int[] currentColors, List<ColoringStep> steps) {
        // 安全检查：步骤数超限
        if (steps.size() > MAX_STEPS) {
            return false;
        }

        if (idx == graph.getVertexCount()) {
            return true; // 所有省份着色完毕
        }

        for (int c = 1; c <= maxColors && c < bestColorCount; c++) {
            if (isValid(idx, c, currentColors)) {
                currentColors[idx] = c;
                steps.add(new ColoringStep(ColoringStep.Action.TRY,
                        idx, c, graph.getVertexName(idx)));

                if (backtrack(idx + 1, maxColors, currentColors, steps)) {
                    return true;
                }

                // 回退：撤销当前尝试
                steps.add(new ColoringStep(ColoringStep.Action.UNDO,
                        idx, 0, graph.getVertexName(idx)));
                currentColors[idx] = 0;
            }
        }
        return false;
    }

    private boolean isValid(int vertex, int color, int[] currentColors) {
        MyArrayList<Integer> neighbors = graph.getNeighbors(vertex);
        for (int j = 0; j < neighbors.size(); j++) {
            int neighbor = neighbors.get(j);
            if (neighbor < currentColors.length
                    && currentColors[neighbor] == color) {
                return false;
            }
        }
        return true;
    }
}
