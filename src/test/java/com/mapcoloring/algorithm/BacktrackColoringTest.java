package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BacktrackColoringTest {

    @Test
    void testTriangleNeedsThreeColors() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 2);

        ColoringResult result = BacktrackColoring.color(g);
        assertEquals(3, result.colorCount);

        assertNotEquals(g.getColor(0), g.getColor(1));
        assertNotEquals(g.getColor(1), g.getColor(2));
        assertNotEquals(g.getColor(0), g.getColor(2));
    }

    @Test
    void testLineNeedsTwoColors() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addEdge(0, 1);
        g.addEdge(1, 2);

        ColoringResult result = BacktrackColoring.color(g);
        assertEquals(2, result.colorCount);
    }

    @Test
    void testSingleVertexNeedsOneColor() {
        Graph g = new Graph();
        g.addVertex("A");
        ColoringResult result = BacktrackColoring.color(g);
        assertEquals(1, result.colorCount);
    }
}
