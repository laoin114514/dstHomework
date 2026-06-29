package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GreedyColoringTest {

    @Test
    void testSimpleGraph() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addEdge(0, 1);
        g.addEdge(1, 2);

        ColoringResult result = GreedyColoring.color(g);

        assertNotEquals(g.getColor(0), g.getColor(1));
        assertNotEquals(g.getColor(1), g.getColor(2));
        assertTrue(result.colorCount >= 2);
    }

    @Test
    void testTriangle() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 2);

        ColoringResult result = GreedyColoring.color(g);
        assertEquals(3, result.colorCount);
    }
}
