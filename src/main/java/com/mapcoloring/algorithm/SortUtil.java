package com.mapcoloring.algorithm;

import com.mapcoloring.model.MyArrayList;

public class SortUtil {

    public static MyArrayList<Integer> sortIndicesByValueDescending(
            MyArrayList<Integer> values) {
        int n = values.size();
        MyArrayList<Integer> indices = new MyArrayList<>();
        for (int i = 0; i < n; i++) {
            indices.add(i);
        }
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                int v1 = values.get(indices.get(j));
                int v2 = values.get(indices.get(j + 1));
                if (v1 < v2) {
                    int tmp = indices.get(j);
                    indices.set(j, indices.get(j + 1));
                    indices.set(j + 1, tmp);
                }
            }
        }
        return indices;
    }

    public static int linearSearch(MyArrayList<Integer> list, int target) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    public static int findSmallestUnusedColor(MyArrayList<Integer> neighborColors) {
        int color = 1;
        while (true) {
            if (linearSearch(neighborColors, color) == -1) {
                return color;
            }
            color++;
        }
    }
}
