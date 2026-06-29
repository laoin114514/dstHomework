package com.mapcoloring.model;

public class MapData {
    public Graph graph;
    public MyArrayList<Province> provinces;

    public MapData() {
        graph = new Graph();
        provinces = new MyArrayList<>();
    }

    public void addProvince(Province p) {
        provinces.add(p);
        graph.addVertex(p.name);
    }

    public void removeProvince(int index) {
        provinces.remove(index);
        graph.removeVertex(index);
    }

    public void addAdjacency(int fromIndex, int toIndex) {
        graph.addEdge(fromIndex, toIndex);
    }
}
