package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;

public class GreedyColoring {

    public static ColoringResult color(Graph g) {
        long start = System.nanoTime();
        g.resetColors();

        for (int i = 0; i < g.getVertexCount(); i++) {
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
        }

        int maxColor = 0;
        for (int i = 0; i < g.getVertexCount(); i++) {
            if (g.getColor(i) > maxColor) {
                maxColor = g.getColor(i);
            }
        }
        long elapsed = System.nanoTime() - start;
        return new ColoringResult(maxColor, elapsed);
    }
}
