package com.mapcoloring.model;

public class Province {
    public int id;
    public String name;
    public MyArrayList<Point> polygon;
    public int color;

    public Province() {
        polygon = new MyArrayList<>();
        color = 0;
    }

    public Province(int id, String name) {
        this.id = id;
        this.name = name;
        this.polygon = new MyArrayList<>();
        this.color = 0;
    }
}
