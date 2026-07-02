package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.ui.ColoringStep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SortedGreedyColoringTest {

    @Test
    void testSameAsGreedyForSingle() {
        Graph g = new Graph();
        g.addVertex("A");
        ColoringResult r1 = GreedyColoring.color(g);
        ColoringResult r2 = SortedGreedyColoring.color(g);
        assertEquals(r1.colorCount, r2.colorCount);
        assertNotNull(r2.steps);
        assertEquals(1, r2.steps.size());
    }

    @Test
    void testAdjacentProvincesDifferentColors() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(0, 3);
        g.addEdge(1, 2);

        SortedGreedyColoring.color(g);
        for (int i = 0; i < g.getVertexCount(); i++) {
            var neighbors = g.getNeighbors(i);
            for (int j = 0; j < neighbors.size(); j++) {
                assertNotEquals(g.getColor(i), g.getColor(neighbors.get(j)),
                    "Vertex " + i + " and " + neighbors.get(j) + " have same color");
            }
        }
    }

    @Test
    void testStepsAllCommit() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addEdge(0, 1);
        g.addEdge(1, 2);

        ColoringResult result = SortedGreedyColoring.color(g);
        assertNotNull(result.steps);
        assertEquals(3, result.steps.size());
        for (ColoringStep step : result.steps) {
            assertEquals(ColoringStep.Action.COMMIT, step.action);
        }
    }
}
