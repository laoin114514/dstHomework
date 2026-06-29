package com.mapcoloring.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void testAddVertex() {
        Graph g = new Graph();
        g.addVertex("北京");
        g.addVertex("天津");
        assertEquals(2, g.getVertexCount());
        assertEquals("北京", g.getVertexName(0));
        assertEquals("天津", g.getVertexName(1));
    }

    @Test
    void testAddEdge() {
        Graph g = new Graph();
        g.addVertex("北京");
        g.addVertex("天津");
        g.addVertex("河北");
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        assertEquals(2, g.getDegree(0));
        assertEquals(1, g.getDegree(1));
    }

    @Test
    void testGetNeighbors() {
        Graph g = new Graph();
        g.addVertex("北京");
        g.addVertex("天津");
        g.addVertex("河北");
        g.addEdge(0, 1);
        g.addEdge(0, 2);

        MyArrayList<Integer> neighbors = g.getNeighbors(0);
        assertEquals(2, neighbors.size());
        assertTrue(contains(neighbors, 1));
        assertTrue(contains(neighbors, 2));
    }

    @Test
    void testGetVertexColorAndSetColor() {
        Graph g = new Graph();
        g.addVertex("北京");
        assertEquals(0, g.getColor(0));
        g.setColor(0, 3);
        assertEquals(3, g.getColor(0));
    }

    @Test
    void testRemoveVertex() {
        Graph g = new Graph();
        g.addVertex("北京");
        g.addVertex("天津");
        g.addVertex("河北");
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 2);

        g.removeVertex(0);
        assertEquals(2, g.getVertexCount());
        assertEquals("天津", g.getVertexName(0));
        assertEquals("河北", g.getVertexName(1));
        assertEquals(1, g.getDegree(0));
        assertTrue(contains(g.getNeighbors(0), 1));
    }

    @Test
    void testRemoveEdge() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addEdge(0, 1);
        assertEquals(1, g.getDegree(0));
        g.removeEdge(0, 1);
        assertEquals(0, g.getDegree(0));
        assertEquals(0, g.getDegree(1));
    }

    @Test
    void testAddEdgeDuplicatePrevention() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addEdge(0, 1);
        g.addEdge(0, 1);
        assertEquals(1, g.getDegree(0));
        assertEquals(1, g.getDegree(1));
    }

    @Test
    void testResetColors() {
        Graph g = new Graph();
        g.addVertex("北京");
        g.addVertex("天津");
        g.setColor(0, 2);
        g.setColor(1, 3);
        g.resetColors();
        assertEquals(0, g.getColor(0));
        assertEquals(0, g.getColor(1));
    }

    private boolean contains(MyArrayList<Integer> list, int value) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(value)) return true;
        }
        return false;
    }
}
