package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;

public class BacktrackColoring {

    private Graph graph;
    private int bestColorCount;
    private int[] bestColors;

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

        // 先用排序贪心得到上界
        ColoringResult greedyResult = SortedGreedyColoring.color(g);
        int upperBound = greedyResult.colorCount;
        for (int i = 0; i < n; i++) {
            bestColors[i] = g.getColor(i);
        }
        bestColorCount = upperBound;
        g.resetColors();

        int[] currentColors = new int[n];
        backtrack(0, upperBound, currentColors);

        for (int i = 0; i < n; i++) {
            g.setColor(i, bestColors[i]);
        }

        long elapsed = System.nanoTime() - start;
        return new ColoringResult(bestColorCount, elapsed);
    }

    private void backtrack(int idx, int maxColors, int[] currentColors) {
        if (idx == graph.getVertexCount()) {
            int used = maxUsed(currentColors);
            if (used < bestColorCount) {
                bestColorCount = used;
                for (int i = 0; i < currentColors.length; i++) {
                    bestColors[i] = currentColors[i];
                }
            }
            return;
        }

        for (int c = 1; c <= maxColors && c < bestColorCount; c++) {
            if (isValid(idx, c, currentColors)) {
                currentColors[idx] = c;
                backtrack(idx + 1, maxColors, currentColors);
                currentColors[idx] = 0;
            }
        }
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

    private int maxUsed(int[] colors) {
        int max = 0;
        for (int c : colors) {
            if (c > max) max = c;
        }
        return max;
    }
}
