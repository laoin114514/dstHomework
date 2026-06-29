package com.mapcoloring.model;

public class Graph {

    private MyArrayList<String> names;
    private MyArrayList<Integer> colors;
    private MyArrayList<MyArrayList<Integer>> adjList;

    public Graph() {
        names = new MyArrayList<>();
        colors = new MyArrayList<>();
        adjList = new MyArrayList<>();
    }

    public void addVertex(String name) {
        names.add(name);
        colors.add(0);
        adjList.add(new MyArrayList<>());
    }

    public void removeVertex(int index) {
        names.remove(index);
        colors.remove(index);
        adjList.remove(index);
        for (int i = 0; i < adjList.size(); i++) {
            MyArrayList<Integer> neighbors = adjList.get(i);
            for (int j = neighbors.size() - 1; j >= 0; j--) {
                int n = neighbors.get(j);
                if (n == index) {
                    neighbors.remove(j);
                } else if (n > index) {
                    neighbors.set(j, n - 1);
                }
            }
        }
    }

    public void addEdge(int from, int to) {
        MyArrayList<Integer> neighbors = adjList.get(from);
        if (neighbors.indexOf(to) == -1) {
            neighbors.add(to);
        }
        MyArrayList<Integer> reverse = adjList.get(to);
        if (reverse.indexOf(from) == -1) {
            reverse.add(from);
        }
    }

    public void removeEdge(int from, int to) {
        MyArrayList<Integer> neighbors = adjList.get(from);
        int idx = neighbors.indexOf(to);
        if (idx != -1) neighbors.remove(idx);

        MyArrayList<Integer> reverse = adjList.get(to);
        idx = reverse.indexOf(from);
        if (idx != -1) reverse.remove(idx);
    }

    public MyArrayList<Integer> getNeighbors(int index) {
        return adjList.get(index);
    }

    public int getDegree(int index) {
        return adjList.get(index).size();
    }

    public int getVertexCount() {
        return names.size();
    }

    public String getVertexName(int index) {
        return names.get(index);
    }

    public void setVertexName(int index, String name) {
        names.set(index, name);
    }

    public int getColor(int index) {
        return colors.get(index);
    }

    public void setColor(int index, int color) {
        colors.set(index, color);
    }

    public void resetColors() {
        for (int i = 0; i < colors.size(); i++) {
            colors.set(i, 0);
        }
    }
}
