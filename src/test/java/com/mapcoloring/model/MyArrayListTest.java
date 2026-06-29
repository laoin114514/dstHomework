package com.mapcoloring.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyArrayListTest {

    @Test
    void testAddAndGet() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    void testRemove() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        assertEquals("b", list.remove(1));
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    void testSet() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.set(0, "x");
        assertEquals("x", list.get(0));
    }

    @Test
    void testClear() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add("b");
        list.clear();
        assertEquals(0, list.size());
    }

    @Test
    void testIsEmpty() {
        MyArrayList<String> list = new MyArrayList<>();
        assertTrue(list.isEmpty());
        list.add("a");
        assertFalse(list.isEmpty());
    }

    @Test
    void testGrow() {
        MyArrayList<Integer> list = new MyArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        assertEquals(100, list.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, list.get(i));
        }
    }

    @Test
    void testIndexOf() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        assertEquals(0, list.indexOf("a"));
        assertEquals(2, list.indexOf("c"));
        assertEquals(-1, list.indexOf("x"));
    }

    @Test
    void testIndexOfNull() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add(null);
        list.add("c");
        assertEquals(1, list.indexOf(null));
        assertEquals(-1, list.indexOf("b"));
    }

    @Test
    void testGetOutOfBounds() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    void testRemoveOutOfBounds() {
        MyArrayList<String> list = new MyArrayList<>();
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    @Test
    void testIterator() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        int count = 0;
        String[] expected = {"a", "b", "c"};
        for (String s : list) {
            assertEquals(expected[count], s);
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    void testClearRemovesElements() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add("b");
        list.clear();
        assertEquals(-1, list.indexOf("a"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }
}
