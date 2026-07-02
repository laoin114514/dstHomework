package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;
import com.mapcoloring.ui.ColoringStep;
import java.util.ArrayList;
import java.util.List;

public class SortedGreedyColoring {

    public static ColoringResult color(Graph g) {
        long start = System.nanoTime();
        g.resetColors();

        List<ColoringStep> steps = new ArrayList<>();

        MyArrayList<Integer> degrees = new MyArrayList<>();
        for (int i = 0; i < g.getVertexCount(); i++) {
            degrees.add(g.getDegree(i));
        }
        MyArrayList<Integer> order = SortUtil.sortIndicesByValueDescending(degrees);

        for (int idx = 0; idx < order.size(); idx++) {
            int i = order.get(idx);
            MyArrayList<Integer> neighborColors = new MyArrayList<>();
            MyArrayList<Integer> neighbors = g.getNeighbors(i);
            for (int j = 0; j < neighbors.size(); j++) {
                int nc = g.getColor(neighbors.get(j));
                if (nc != 0) {
                    neighborColors.add(nc);
                }
            }
            int color = SortUtil.findSmallestUnusedColor(neighborColors);
            g.setColor(i, color);
            steps.add(new ColoringStep(ColoringStep.Action.COMMIT,
                    i, color, g.getVertexName(i)));
        }

        int maxColor = 0;
        for (int i = 0; i < g.getVertexCount(); i++) {
            if (g.getColor(i) > maxColor) {
                maxColor = g.getColor(i);
            }
        }
        long elapsed = System.nanoTime() - start;
        return new ColoringResult(maxColor, elapsed, steps);
    }
}
