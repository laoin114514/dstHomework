package com.mapcoloring.algorithm;

import com.mapcoloring.model.MyArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SortUtilTest {

    @Test
    void testBubbleSortDescending() {
        MyArrayList<Integer> values = new MyArrayList<>();
        values.add(3);
        values.add(1);
        values.add(4);
        values.add(1);
        values.add(5);

        MyArrayList<Integer> indices = SortUtil.sortIndicesByValueDescending(values);

        assertEquals(5, indices.size());
        assertEquals(5, values.get(indices.get(0)));
        assertEquals(4, values.get(indices.get(1)));
        assertEquals(3, values.get(indices.get(2)));
    }

    @Test
    void testLinearSearch() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(10);
        list.add(20);
        list.add(30);

        assertEquals(1, SortUtil.linearSearch(list, 20));
        assertEquals(-1, SortUtil.linearSearch(list, 99));
    }
}
