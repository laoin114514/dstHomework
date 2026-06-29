# 地图着色 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现地图着色程序，自实现图数据结构、三种着色算法、文件存储，JavaFX 图形界面展示中国和美国地图着色方案。

**Architecture:** 分层架构：model（自实现数据结构）→ algorithm（三种着色算法）+ io（文件读写）→ ui（JavaFX 双标签页界面）。model 不依赖任何模块，ui 依赖所有下层。

**Tech Stack:** Java 17+, JavaFX 21, Maven, JUnit 5

---

### Task 1: 项目骨架搭建

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/mapcoloring/Main.java`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.mapcoloring</groupId>
    <artifactId>map-coloring</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>21.0.2</javafx.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-graphics</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.mapcoloring.Main</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建目录结构**

```bash
mkdir -p src/main/java/com/mapcoloring/model
mkdir -p src/main/java/com/mapcoloring/algorithm
mkdir -p src/main/java/com/mapcoloring/io
mkdir -p src/main/java/com/mapcoloring/ui
mkdir -p src/test/java/com/mapcoloring/model
mkdir -p src/test/java/com/mapcoloring/algorithm
mkdir -p src/test/java/com/mapcoloring/io
mkdir -p data
```

- [ ] **Step 3: 创建 Main.java 骨架**

```java
package com.mapcoloring;

public class Main {
    public static void main(String[] args) {
        System.out.println("Map Coloring Application");
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 5: 运行验证**

```bash
mvn javafx:run
```
Expected: 控制台输出 "Map Coloring Application"

- [ ] **Step 6: Commit**

```bash
git add pom.xml src/main/java/com/mapcoloring/Main.java
git commit -m "chore: init Maven project with JavaFX and JUnit"
```

---

### Task 2: MyArrayList 自实现动态数组

**Files:**
- Create: `src/main/java/com/mapcoloring/model/MyArrayList.java`
- Create: `src/test/java/com/mapcoloring/model/MyArrayListTest.java`

- [ ] **Step 1: 编写测试**

```java
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
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=MyArrayListTest
```
Expected: COMPILE ERROR (class not found)

- [ ] **Step 3: 实现 MyArrayList**

```java
package com.mapcoloring.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyArrayList<T> implements Iterable<T> {

    private Object[] data;
    private int size;

    public MyArrayList() {
        data = new Object[8];
        size = 0;
    }

    public void add(T value) {
        if (size == data.length) {
            grow();
        }
        data[size++] = value;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) data[index];
    }

    public void set(int index, T value) {
        checkIndex(index);
        data[index] = value;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkIndex(index);
        T old = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[--size] = null;
        return old;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
    }

    public int indexOf(T value) {
        for (int i = 0; i < size; i++) {
            if (value == null ? data[i] == null : value.equals(data[i])) {
                return i;
            }
        }
        return -1;
    }

    private void grow() {
        Object[] newData = new Object[data.length * 2];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }
        data = newData;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (T) data[pos++];
            }
        };
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -Dtest=MyArrayListTest
```
Expected: Tests run: 6, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mapcoloring/model/MyArrayList.java src/test/java/com/mapcoloring/model/MyArrayListTest.java
git commit -m "feat: add MyArrayList self-implemented dynamic array"
```

---

### Task 3: Graph 邻接表图数据结构

**Files:**
- Create: `src/main/java/com/mapcoloring/model/Graph.java`
- Create: `src/test/java/com/mapcoloring/model/GraphTest.java`

- [ ] **Step 1: 编写测试**

```java
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
        // 原来 index 1 变成 index 0
        assertEquals("天津", g.getVertexName(0));
        assertEquals("河北", g.getVertexName(1));
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
            if (list.get(i) == value) return true;
        }
        return false;
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=GraphTest
```
Expected: COMPILE ERROR

- [ ] **Step 3: 实现 Graph**

```java
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
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -Dtest=GraphTest
```
Expected: Tests run: 6, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mapcoloring/model/Graph.java src/test/java/com/mapcoloring/model/GraphTest.java
git commit -m "feat: add Graph adjacency list data structure"
```

---

### Task 4: Province, Point, MapData 数据模型

**Files:**
- Create: `src/main/java/com/mapcoloring/model/Point.java`
- Create: `src/main/java/com/mapcoloring/model/Province.java`
- Create: `src/main/java/com/mapcoloring/model/MapData.java`

- [ ] **Step 1: 实现 Point**

```java
package com.mapcoloring.model;

public class Point {
    public double x;
    public double y;

    public Point() {}

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
```

- [ ] **Step 2: 实现 Province**

```java
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
```

- [ ] **Step 3: 实现 MapData**

```java
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
```

- [ ] **Step 4: 编译验证**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mapcoloring/model/Point.java src/main/java/com/mapcoloring/model/Province.java src/main/java/com/mapcoloring/model/MapData.java
git commit -m "feat: add Province, Point, MapData model classes"
```

---

### Task 5: MapFileHandler 文件读写

**Files:**
- Create: `src/main/java/com/mapcoloring/io/MapFileHandler.java`
- Create: `src/test/java/com/mapcoloring/io/MapFileHandlerTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.mapcoloring.io;

import com.mapcoloring.model.MapData;
import com.mapcoloring.model.Province;
import com.mapcoloring.model.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class MapFileHandlerTest {

    @Test
    void testSaveAndLoad(@TempDir Path tempDir) throws Exception {
        MapData original = new MapData();

        Province p0 = new Province(0, "北京");
        p0.polygon.add(new Point(100, 50));
        p0.polygon.add(new Point(110, 55));
        p0.polygon.add(new Point(105, 60));
        original.addProvince(p0);

        Province p1 = new Province(1, "天津");
        p1.polygon.add(new Point(110, 55));
        p1.polygon.add(new Point(115, 58));
        p1.polygon.add(new Point(112, 60));
        original.addProvince(p1);

        Province p2 = new Province(2, "河北");
        p2.polygon.add(new Point(95, 50));
        p2.polygon.add(new Point(120, 50));
        p2.polygon.add(new Point(120, 65));
        p2.polygon.add(new Point(95, 65));
        original.addProvince(p2);

        original.addAdjacency(0, 1);
        original.addAdjacency(0, 2);

        String filePath = tempDir.resolve("test.map").toString();
        MapFileHandler.save(original, filePath);

        MapData loaded = MapFileHandler.load(filePath);
        assertEquals(3, loaded.provinces.size());
        assertEquals("北京", loaded.provinces.get(0).name);
        assertEquals("天津", loaded.provinces.get(1).name);
        assertEquals(3, loaded.provinces.get(0).polygon.size());
        assertEquals(2, loaded.graph.getDegree(0));
        assertEquals(1, loaded.graph.getDegree(1));
    }

    @Test
    void testLoadNonexistentFile() {
        assertThrows(RuntimeException.class, () -> {
            MapFileHandler.load("nonexistent.map");
        });
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=MapFileHandlerTest
```
Expected: COMPILE ERROR

- [ ] **Step 3: 实现 MapFileHandler**

```java
package com.mapcoloring.io;

import com.mapcoloring.model.MapData;
import com.mapcoloring.model.Point;
import com.mapcoloring.model.Province;
import java.io.*;

public class MapFileHandler {

    public static void save(MapData data, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < data.provinces.size(); i++) {
                Province p = data.provinces.get(i);
                writer.write("PROVINCE " + p.id + " " + p.name);
                for (int j = 0; j < p.polygon.size(); j++) {
                    Point pt = p.polygon.get(j);
                    writer.write(" " + pt.x + "," + pt.y);
                }
                writer.newLine();
            }
            for (int i = 0; i < data.graph.getVertexCount(); i++) {
                var neighbors = data.graph.getNeighbors(i);
                if (neighbors.size() > 0) {
                    writer.write("EDGES " + i);
                    for (int j = 0; j < neighbors.size(); j++) {
                        writer.write(" " + neighbors.get(j));
                    }
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save map: " + e.getMessage(), e);
        }
    }

    public static MapData load(String filePath) {
        MapData data = new MapData();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 3) continue;

                if ("PROVINCE".equals(parts[0])) {
                    Province p = new Province();
                    p.id = Integer.parseInt(parts[1]);
                    p.name = parts[2];
                    for (int i = 3; i < parts.length; i++) {
                        String[] coord = parts[i].split(",");
                        Point pt = new Point(
                            Double.parseDouble(coord[0]),
                            Double.parseDouble(coord[1]));
                        p.polygon.add(pt);
                    }
                    data.addProvince(p);
                } else if ("EDGES".equals(parts[0])) {
                    int from = Integer.parseInt(parts[1]);
                    for (int i = 2; i < parts.length; i++) {
                        String[] subParts = parts[i].split(",");
                        for (String s : subParts) {
                            if (!s.isEmpty()) {
                                int to = Integer.parseInt(s);
                                data.addAdjacency(from, to);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load map: " + e.getMessage(), e);
        }
        return data;
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -Dtest=MapFileHandlerTest
```
Expected: Tests run: 2, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mapcoloring/io/MapFileHandler.java src/test/java/com/mapcoloring/io/MapFileHandlerTest.java
git commit -m "feat: add MapFileHandler save/load with custom format"
```

---

### Task 6: 排序工具类

**Files:**
- Create: `src/main/java/com/mapcoloring/algorithm/SortUtil.java`
- Create: `src/test/java/com/mapcoloring/algorithm/SortUtilTest.java`

- [ ] **Step 1: 编写测试**

```java
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
        assertEquals(5, values.get(indices.get(0))); // 最大在前
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
```

- [ ] **Step 2: 实现 SortUtil**

```java
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

    public static int findSmallestUnusedColor(
            MyArrayList<Integer> neighborColors) {
        int color = 1;
        while (true) {
            if (linearSearch(neighborColors, color) == -1) {
                return color;
            }
            color++;
        }
    }
}
```

- [ ] **Step 3: 运行测试确认通过**

```bash
mvn test -Dtest=SortUtilTest
```
Expected: Tests run: 2, Failures: 0

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mapcoloring/algorithm/SortUtil.java src/test/java/com/mapcoloring/algorithm/SortUtilTest.java
git commit -m "feat: add SortUtil bubble sort and linear search"
```

---

### Task 7: GreedyColoring 贪心着色

**Files:**
- Create: `src/main/java/com/mapcoloring/algorithm/GreedyColoring.java`
- Create: `src/test/java/com/mapcoloring/algorithm/GreedyColoringTest.java`

- [ ] **Step 1: 编写测试**

```java
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

        // 相邻省颜色不同
        assertNotEquals(g.getColor(0), g.getColor(1));
        assertNotEquals(g.getColor(1), g.getColor(2));
        // A-C 不相邻，可以同色
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
```

- [ ] **Step 2: 实现 GreedyColoring + ColoringResult**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;

public class GreedyColoring {

    public static ColoringResult color(Graph g) {
        long start = System.nanoTime();
        g.resetColors();

        for (int i = 0; i < g.getVertexCount(); i++) {
            MyArrayList<Integer> neighborColors = new MyArrayList<>();
            MyArrayList<Integer> neighbors = g.getNeighbors(i);
            for (int j = 0; j < neighbors.size(); j++) {
                int nc = g.getColor(neighbors.get(j));
                if (nc != 0) {
                    neighborColors.add(nc);
                }
            }
            int color = SortUtil.findSmallestUnusedColor(neighborColors);
            g.setColor(i, color);
        }

        int maxColor = 0;
        for (int i = 0; i < g.getVertexCount(); i++) {
            if (g.getColor(i) > maxColor) {
                maxColor = g.getColor(i);
            }
        }
        long elapsed = System.nanoTime() - start;
        return new ColoringResult(maxColor, elapsed);
    }
}
```

```java
package com.mapcoloring.algorithm;

public class ColoringResult {
    public int colorCount;
    public long timeNanos;

    public ColoringResult(int colorCount, long timeNanos) {
        this.colorCount = colorCount;
        this.timeNanos = timeNanos;
    }

    public double timeMillis() {
        return timeNanos / 1_000_000.0;
    }
}
```

- [ ] **Step 3: 运行测试确认通过**

```bash
mvn test -Dtest=GreedyColoringTest
```
Expected: Tests run: 2, Failures: 0

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mapcoloring/algorithm/GreedyColoring.java src/main/java/com/mapcoloring/algorithm/ColoringResult.java src/test/java/com/mapcoloring/algorithm/GreedyColoringTest.java
git commit -m "feat: add GreedyColoring algorithm"
```

---

### Task 8: SortedGreedyColoring 按度数排序贪心

**Files:**
- Create: `src/main/java/com/mapcoloring/algorithm/SortedGreedyColoring.java`
- Create: `src/test/java/com/mapcoloring/algorithm/SortedGreedyColoringTest.java`

- [ ] **Step 1: 编写测试**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
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
}
```

- [ ] **Step 2: 实现 SortedGreedyColoring**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;

public class SortedGreedyColoring {

    public static ColoringResult color(Graph g) {
        long start = System.nanoTime();
        g.resetColors();

        MyArrayList<Integer> degrees = new MyArrayList<>();
        for (int i = 0; i < g.getVertexCount(); i++) {
            degrees.add(g.getDegree(i));
        }
        MyArrayList<Integer> order = SortUtil.sortIndicesByValueDescending(degrees);

        for (int idx = 0; idx < order.size(); idx++) {
            int i = order.get(idx);
            MyArrayList<Integer> neighborColors = new MyArrayList<>();
            MyArrayList<Integer> neighbors = g.getNeighbors(i);
            for (int j = 0; j < neighbors.size(); j++) {
                int nc = g.getColor(neighbors.get(j));
                if (nc != 0) {
                    neighborColors.add(nc);
                }
            }
            int color = SortUtil.findSmallestUnusedColor(neighborColors);
            g.setColor(i, color);
        }

        int maxColor = 0;
        for (int i = 0; i < g.getVertexCount(); i++) {
            if (g.getColor(i) > maxColor) {
                maxColor = g.getColor(i);
            }
        }
        long elapsed = System.nanoTime() - start;
        return new ColoringResult(maxColor, elapsed);
    }
}
```

- [ ] **Step 3: 运行测试确认通过**

```bash
mvn test -Dtest=SortedGreedyColoringTest
```
Expected: Tests run: 2, Failures: 0

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mapcoloring/algorithm/SortedGreedyColoring.java src/test/java/com/mapcoloring/algorithm/SortedGreedyColoringTest.java
git commit -m "feat: add SortedGreedyColoring algorithm"
```

---

### Task 9: BacktrackColoring 回溯求最优解

**Files:**
- Create: `src/main/java/com/mapcoloring/algorithm/BacktrackColoring.java`
- Create: `src/test/java/com/mapcoloring/algorithm/BacktrackColoringTest.java`

- [ ] **Step 1: 编写测试**

```java
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

        // 验证相邻颜色不同
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
```

- [ ] **Step 2: 实现 BacktrackColoring**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;

public class BacktrackColoring {

    private Graph graph;
    private int bestColorCount;
    private int[] bestColors;

    public static ColoringResult color(Graph g) {
        BacktrackColoring solver = new BacktrackColoring();
        return solver.solve(g);
    }

    private ColoringResult solve(Graph g) {
        long start = System.nanoTime();
        this.graph = g;
        int n = g.getVertexCount();
        bestColorCount = n;
        bestColors = new int[n];

        // 先用排序贪心得到上界
        ColoringResult greedyResult = SortedGreedyColoring.color(g);
        int upperBound = greedyResult.colorCount;
        for (int i = 0; i < n; i++) {
            bestColors[i] = g.getColor(i);
        }
        bestColorCount = upperBound;
        g.resetColors();

        int[] currentColors = new int[n];
        backtrack(0, upperBound, currentColors);

        for (int i = 0; i < n; i++) {
            g.setColor(i, bestColors[i]);
        }

        long elapsed = System.nanoTime() - start;
        return new ColoringResult(bestColorCount, elapsed);
    }

    private void backtrack(int idx, int maxColors, int[] currentColors) {
        if (idx == graph.getVertexCount()) {
            int used = maxUsed(currentColors);
            if (used < bestColorCount) {
                bestColorCount = used;
                for (int i = 0; i < currentColors.length; i++) {
                    bestColors[i] = currentColors[i];
                }
            }
            return;
        }

        for (int c = 1; c <= maxColors && c < bestColorCount; c++) {
            if (isValid(idx, c, currentColors)) {
                currentColors[idx] = c;
                backtrack(idx + 1, maxColors, currentColors);
                currentColors[idx] = 0;
            }
        }
    }

    private boolean isValid(int vertex, int color, int[] currentColors) {
        MyArrayList<Integer> neighbors = graph.getNeighbors(vertex);
        for (int j = 0; j < neighbors.size(); j++) {
            int neighbor = neighbors.get(j);
            if (neighbor < currentColors.length
                    && currentColors[neighbor] == color) {
                return false;
            }
        }
        return true;
    }

    private int maxUsed(int[] colors) {
        int max = 0;
        for (int c : colors) {
            if (c > max) max = c;
        }
        return max;
    }
}
```

- [ ] **Step 3: 运行测试确认通过**

```bash
mvn test -Dtest=BacktrackColoringTest
```
Expected: Tests run: 3, Failures: 0

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mapcoloring/algorithm/BacktrackColoring.java src/test/java/com/mapcoloring/algorithm/BacktrackColoringTest.java
git commit -m "feat: add BacktrackColoring optimal algorithm"
```

---

### Task 10: MapCanvas 地图画布

**Files:**
- Create: `src/main/java/com/mapcoloring/ui/MapCanvas.java`

- [ ] **Step 1: 实现 MapCanvas**

```java
package com.mapcoloring.ui;

import com.mapcoloring.model.MapData;
import com.mapcoloring.model.MyArrayList;
import com.mapcoloring.model.Point;
import com.mapcoloring.model.Province;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MapCanvas extends Canvas {

    private static final Color[] PALETTE = {
        Color.rgb(255, 179, 186), // 浅红
        Color.rgb(186, 255, 201), // 浅绿
        Color.rgb(186, 225, 255), // 浅蓝
        Color.rgb(255, 255, 186), // 浅黄
        Color.rgb(255, 210, 140), // 橙色
        Color.rgb(220, 186, 255), // 浅紫
        Color.rgb(186, 255, 255), // 青色
        Color.rgb(255, 200, 220), // 粉色
    };

    private MapData mapData;
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double lastMouseX, lastMouseY;
    private int highlightedIndex = -1;

    public MapCanvas(double width, double height) {
        super(width, height);
        setupMouseHandlers();
    }

    public void setMapData(MapData data) {
        this.mapData = data;
        fitToCanvas();
        draw();
    }

    private void fitToCanvas() {
        if (mapData == null || mapData.provinces.size() == 0) return;
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (int i = 0; i < mapData.provinces.size(); i++) {
            Province p = mapData.provinces.get(i);
            for (int j = 0; j < p.polygon.size(); j++) {
                Point pt = p.polygon.get(j);
                if (pt.x < minX) minX = pt.x;
                if (pt.y < minY) minY = pt.y;
                if (pt.x > maxX) maxX = pt.x;
                if (pt.y > maxY) maxY = pt.y;
            }
        }

        double dataW = maxX - minX;
        double dataH = maxY - minY;
        double padding = 40;
        scale = Math.min(
            (getWidth() - padding * 2) / dataW,
            (getHeight() - padding * 2) / dataH);
        offsetX = -minX * scale + padding;
        offsetY = -minY * scale + padding;
    }

    public void draw() {
        if (mapData == null) return;
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < mapData.provinces.size(); i++) {
            Province p = mapData.provinces.get(i);
            int colorIdx = mapData.graph.getColor(i);
            drawProvince(gc, p, colorIdx, i == highlightedIndex);
        }
    }

    private void drawProvince(GraphicsContext gc, Province p,
                              int colorIdx, boolean highlighted) {
        if (p.polygon.size() < 3) return;

        double[] xs = new double[p.polygon.size()];
        double[] ys = new double[p.polygon.size()];
        for (int j = 0; j < p.polygon.size(); j++) {
            Point pt = p.polygon.get(j);
            xs[j] = pt.x * scale + offsetX;
            ys[j] = pt.y * scale + offsetY;
        }

        if (colorIdx > 0) {
            gc.setFill(PALETTE[(colorIdx - 1) % PALETTE.length]);
        } else {
            gc.setFill(Color.LIGHTGRAY);
        }
        gc.fillPolygon(xs, ys, p.polygon.size());

        gc.setStroke(highlighted ? Color.RED : Color.DARKGRAY);
        gc.setLineWidth(highlighted ? 2.0 : 1.0);
        gc.strokePolygon(xs, ys, p.polygon.size());

        if (p.polygon.size() > 0) {
            double cx = 0, cy = 0;
            for (int j = 0; j < p.polygon.size(); j++) {
                cx += xs[j];
                cy += ys[j];
            }
            cx /= p.polygon.size();
            cy /= p.polygon.size();
            gc.setFill(Color.BLACK);
            gc.fillText(p.name, cx - 15, cy + 4);
        }
    }

    private void setupMouseHandlers() {
        setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            double mx = e.getX();
            double my = e.getY();
            offsetX = mx - (mx - offsetX) * factor;
            offsetY = my - (my - offsetY) * factor;
            scale *= factor;
            draw();
        });

        setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        setOnMouseDragged(e -> {
            offsetX += e.getX() - lastMouseX;
            offsetY += e.getY() - lastMouseY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            draw();
        });

        setOnMouseMoved(e -> {
            if (mapData == null) return;
            int newHighlight = findProvinceAt(e.getX(), e.getY());
            if (newHighlight != highlightedIndex) {
                highlightedIndex = newHighlight;
                draw();
            }
        });
    }

    private int findProvinceAt(double mx, double my) {
        for (int i = mapData.provinces.size() - 1; i >= 0; i--) {
            Province p = mapData.provinces.get(i);
            if (p.polygon.size() < 3) continue;
            if (pointInPolygon(mx, my, p)) return i;
        }
        return -1;
    }

    private boolean pointInPolygon(double px, double py, Province p) {
        boolean inside = false;
        int n = p.polygon.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = p.polygon.get(i);
            Point pj = p.polygon.get(j);
            double xi = pi.x * scale + offsetX;
            double yi = pi.y * scale + offsetY;
            double xj = pj.x * scale + offsetX;
            double yj = pj.y * scale + offsetY;
            if ((yi > py) != (yj > py)
                    && px < (xj - xi) * (py - yi) / (yj - yi) + xi) {
                inside = !inside;
            }
        }
        return inside;
    }

    public MapData getMapData() {
        return mapData;
    }

    public int getHighlightedIndex() {
        return highlightedIndex;
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/mapcoloring/ui/MapCanvas.java
git commit -m "feat: add MapCanvas with zoom, pan, and highlight"
```

---

### Task 11: FrontPanel 前台面板

**Files:**
- Create: `src/main/java/com/mapcoloring/ui/FrontPanel.java`

- [ ] **Step 1: 实现 FrontPanel**

```java
package com.mapcoloring.ui;

import com.mapcoloring.algorithm.BacktrackColoring;
import com.mapcoloring.algorithm.ColoringResult;
import com.mapcoloring.algorithm.GreedyColoring;
import com.mapcoloring.algorithm.SortedGreedyColoring;
import com.mapcoloring.io.MapFileHandler;
import com.mapcoloring.model.MapData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class FrontPanel extends BorderPane {

    private MapCanvas canvas;
    private MapData chinaData;
    private MapData usaData;
    private Label colorCountLabel;
    private Label timeLabel;

    public FrontPanel() {
        canvas = new MapCanvas(750, 550);
        setCenter(canvas);

        VBox rightPanel = createRightPanel();
        setRight(rightPanel);
        BorderPane.setMargin(rightPanel, new Insets(10));
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(200);

        Label titleLabel = new Label("地图着色");
        titleLabel.setFont(new Font(18));

        Label mapLabel = new Label("选择地图");
        mapLabel.setFont(new Font(12));

        Button chinaBtn = new Button("中国地图");
        chinaBtn.setMaxWidth(Double.MAX_VALUE);
        chinaBtn.setOnAction(e -> loadChina());

        Button usaBtn = new Button("美国地图");
        usaBtn.setMaxWidth(Double.MAX_VALUE);
        usaBtn.setOnAction(e -> loadUsa());

        Separator sep1 = new Separator();

        Label algoLabel = new Label("着色算法");
        algoLabel.setFont(new Font(12));

        Button greedyBtn = new Button("贪心着色");
        greedyBtn.setMaxWidth(Double.MAX_VALUE);
        greedyBtn.setOnAction(e -> runGreedy());

        Button sortedBtn = new Button("贪心+排序");
        sortedBtn.setMaxWidth(Double.MAX_VALUE);
        sortedBtn.setOnAction(e -> runSortedGreedy());

        Button backtrackBtn = new Button("回溯求最优");
        backtrackBtn.setMaxWidth(Double.MAX_VALUE);
        backtrackBtn.setOnAction(e -> runBacktrack());

        Separator sep2 = new Separator();

        Label statsLabel = new Label("统计信息");
        statsLabel.setFont(new Font(12));

        colorCountLabel = new Label("颜色总数：--");
        timeLabel = new Label("耗时：--");

        panel.getChildren().addAll(
            titleLabel,
            mapLabel, chinaBtn, usaBtn,
            sep1,
            algoLabel, greedyBtn, sortedBtn, backtrackBtn,
            sep2,
            statsLabel, colorCountLabel, timeLabel
        );

        return panel;
    }

    private void loadChina() {
        chinaData = MapFileHandler.load("data/china.map");
        canvas.setMapData(chinaData);
    }

    private void loadUsa() {
        usaData = MapFileHandler.load("data/usa.map");
        canvas.setMapData(usaData);
    }

    private void runGreedy() {
        if (canvas.getMapData() == null) {
            showAlert("请先选择地图");
            return;
        }
        ColoringResult r = GreedyColoring.color(canvas.getMapData().graph);
        canvas.draw();
        updateStats(r);
    }

    private void runSortedGreedy() {
        if (canvas.getMapData() == null) {
            showAlert("请先选择地图");
            return;
        }
        ColoringResult r = SortedGreedyColoring.color(canvas.getMapData().graph);
        canvas.draw();
        updateStats(r);
    }

    private void runBacktrack() {
        if (canvas.getMapData() == null) {
            showAlert("请先选择地图");
            return;
        }
        ColoringResult r = BacktrackColoring.color(canvas.getMapData().graph);
        canvas.draw();
        updateStats(r);
    }

    private void updateStats(ColoringResult r) {
        colorCountLabel.setText("颜色总数：" + r.colorCount);
        timeLabel.setText(String.format("耗时：%.2f ms", r.timeMillis()));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.showAndWait();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/mapcoloring/ui/FrontPanel.java
git commit -m "feat: add FrontPanel with map switching and algorithm buttons"
```

---

### Task 12: AdminPanel 后台管理面板

**Files:**
- Create: `src/main/java/com/mapcoloring/ui/AdminPanel.java`

- [ ] **Step 1: 实现 AdminPanel**

```java
package com.mapcoloring.ui;

import com.mapcoloring.io.MapFileHandler;
import com.mapcoloring.model.MapData;
import com.mapcoloring.model.MyArrayList;
import com.mapcoloring.model.Point;
import com.mapcoloring.model.Province;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class AdminPanel extends BorderPane {

    private MapData mapData;
    private String currentFilePath;
    private ListView<String> provinceList;
    private ListView<String> connectedList;
    private ListView<String> unconnectedList;
    private Label editTargetLabel;
    private MapCanvas previewCanvas;

    public AdminPanel() {
        VBox leftPanel = createLeftPanel();
        setLeft(leftPanel);

        VBox centerPanel = createCenterPanel();
        setCenter(centerPanel);

        BorderPane.setMargin(leftPanel, new Insets(10));
        BorderPane.setMargin(centerPanel, new Insets(10));
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(8);
        panel.setPrefWidth(220);

        Label titleLabel = new Label("省份列表");
        titleLabel.setFont(new Font(14));

        provinceList = new ListView<>();
        provinceList.setPrefHeight(300);
        provinceList.getSelectionModel().selectedIndexProperty()
            .addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.intValue() >= 0) {
                    onProvinceSelected(newVal.intValue());
                }
            });

        HBox btnRow = new HBox(8);
        Button addBtn = new Button("添加");
        addBtn.setOnAction(e -> addProvince());
        Button delBtn = new Button("删除");
        delBtn.setOnAction(e -> deleteProvince());
        btnRow.getChildren().addAll(addBtn, delBtn);

        Separator sep = new Separator();

        Label fileLabel = new Label("文件操作");
        fileLabel.setFont(new Font(12));

        Button loadBtn = new Button("加载文件");
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setOnAction(e -> loadFile());

        Button saveBtn = new Button("保存到文件");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveFile());

        panel.getChildren().addAll(
            titleLabel, provinceList, btnRow, sep,
            fileLabel, loadBtn, saveBtn
        );

        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(8);

        editTargetLabel = new Label("邻接关系编辑 — 未选中省份");
        editTargetLabel.setFont(new Font(14));

        HBox listsRow = new HBox(16);

        VBox connectedBox = new VBox(4);
        Label connLabel = new Label("已连接的省份");
        connLabel.setFont(new Font(12));
        connectedList = new ListView<>();
        connectedList.setPrefHeight(180);
        connectedBox.getChildren().addAll(connLabel, connectedList);

        VBox unconnectedBox = new VBox(4);
        Label unconnLabel = new Label("未连接的省份");
        unconnLabel.setFont(new Font(12));
        unconnectedList = new ListView<>();
        unconnectedList.setPrefHeight(180);
        unconnectedBox.getChildren().addAll(unconnLabel, unconnectedList);

        listsRow.getChildren().addAll(connectedBox, unconnectedBox);
        HBox.setHgrow(connectedBox, Priority.ALWAYS);
        HBox.setHgrow(unconnectedBox, Priority.ALWAYS);

        HBox btnRow = new HBox(8);
        Button connectBtn = new Button("连接选中");
        connectBtn.setOnAction(e -> connectSelected());
        Button disconnectBtn = new Button("断开选中");
        disconnectBtn.setOnAction(e -> disconnectSelected());
        btnRow.getChildren().addAll(connectBtn, disconnectBtn);

        panel.getChildren().addAll(editTargetLabel, listsRow, btnRow);

        return panel;
    }

    public void setMapData(MapData data, String filePath) {
        this.mapData = data;
        this.currentFilePath = filePath;
        refreshProvinceList();
    }

    private void refreshProvinceList() {
        provinceList.getItems().clear();
        if (mapData == null) return;
        for (int i = 0; i < mapData.provinces.size(); i++) {
            provinceList.getItems().add(mapData.provinces.get(i).name);
        }
    }

    private void onProvinceSelected(int index) {
        editTargetLabel.setText("邻接关系编辑 — "
            + mapData.provinces.get(index).name);
        refreshAdjacencyLists(index);
    }

    private void refreshAdjacencyLists(int provinceIndex) {
        connectedList.getItems().clear();
        unconnectedList.getItems().clear();

        MyArrayList<Integer> neighbors = mapData.graph.getNeighbors(provinceIndex);
        for (int i = 0; i < mapData.provinces.size(); i++) {
            if (i == provinceIndex) continue;
            boolean isNeighbor = false;
            for (int j = 0; j < neighbors.size(); j++) {
                if (neighbors.get(j) == i) {
                    isNeighbor = true;
                    break;
                }
            }
            if (isNeighbor) {
                connectedList.getItems().add(mapData.provinces.get(i).name);
            } else {
                unconnectedList.getItems().add(mapData.provinces.get(i).name);
            }
        }
    }

    private void addProvince() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("添加省份");
        dialog.setHeaderText("输入省份名称");
        dialog.showAndWait().ifPresent(name -> {
            int id = mapData.provinces.size();
            Province p = new Province(id, name);
            p.polygon.add(new Point(10, 10));
            p.polygon.add(new Point(20, 10));
            p.polygon.add(new Point(15, 20));
            mapData.addProvince(p);
            refreshProvinceList();
        });
    }

    private void deleteProvince() {
        int idx = provinceList.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            showAlert("请先选择要删除的省份");
            return;
        }
        mapData.removeProvince(idx);
        refreshProvinceList();
    }

    private void connectSelected() {
        int provinceIdx = provinceList.getSelectionModel().getSelectedIndex();
        int targetIdx = unconnectedList.getSelectionModel().getSelectedIndex();
        if (provinceIdx < 0 || targetIdx < 0) {
            showAlert("请选择省份和要连接的省份");
            return;
        }
        String targetName = unconnectedList.getItems().get(targetIdx);
        int actualIdx = findProvinceIndex(targetName);
        if (actualIdx >= 0) {
            mapData.graph.addEdge(provinceIdx, actualIdx);
            refreshAdjacencyLists(provinceIdx);
        }
    }

    private void disconnectSelected() {
        int provinceIdx = provinceList.getSelectionModel().getSelectedIndex();
        int targetIdx = connectedList.getSelectionModel().getSelectedIndex();
        if (provinceIdx < 0 || targetIdx < 0) {
            showAlert("请选择省份和要断开的省份");
            return;
        }
        String targetName = connectedList.getItems().get(targetIdx);
        int actualIdx = findProvinceIndex(targetName);
        if (actualIdx >= 0) {
            mapData.graph.removeEdge(provinceIdx, actualIdx);
            refreshAdjacencyLists(provinceIdx);
        }
    }

    private int findProvinceIndex(String name) {
        for (int i = 0; i < mapData.provinces.size(); i++) {
            if (mapData.provinces.get(i).name.equals(name)) return i;
        }
        return -1;
    }

    private void loadFile() {
        try {
            // 使用文件选择器或直接加载
            MapData data = MapFileHandler.load(currentFilePath);
            this.mapData = data;
            refreshProvinceList();
        } catch (Exception ex) {
            showAlert("加载失败：" + ex.getMessage());
        }
    }

    private void saveFile() {
        try {
            MapFileHandler.save(mapData, currentFilePath);
            showAlert("保存成功");
        } catch (Exception ex) {
            showAlert("保存失败：" + ex.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/mapcoloring/ui/AdminPanel.java
git commit -m "feat: add AdminPanel for province and adjacency management"
```

---

### Task 13: MainApp 主窗口整合

**Files:**
- Create: `src/main/java/com/mapcoloring/ui/MainApp.java`
- Modify: `src/main/java/com/mapcoloring/Main.java`

- [ ] **Step 1: 实现 MainApp**

```java
package com.mapcoloring.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();

        FrontPanel frontPanel = new FrontPanel();
        Tab frontTab = new Tab("地图着色", frontPanel);
        frontTab.setClosable(false);

        AdminPanel adminPanel = new AdminPanel();
        Tab adminTab = new Tab("数据管理", adminPanel);
        adminTab.setClosable(false);

        tabPane.getTabs().addAll(frontTab, adminTab);

        Scene scene = new Scene(tabPane, 1000, 620);
        primaryStage.setTitle("地图着色系统");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

- [ ] **Step 2: 修改 Main.java**

```java
package com.mapcoloring;

import com.mapcoloring.ui.MainApp;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/mapcoloring/ui/MainApp.java src/main/java/com/mapcoloring/Main.java
git commit -m "feat: add MainApp with tab integration"
```

---

### Task 14: 地图数据文件

**Files:**
- Create: `data/china.map`
- Create: `data/usa.map`

- [ ] **Step 1: 创建中国地图数据 china.map**

中国 34 个省级行政区（含台湾），使用简化多边形坐标。以下为完整数据：

```
# 中国地图数据 — 34个省级行政区简化多边形坐标
# 坐标基于简化地图，单位：像素坐标（画布 750×550 适配后）

PROVINCE 0 北京 550,230 555,225 560,228 562,232 558,237 552,236
PROVINCE 1 天津 555,240 562,238 565,242 560,246 553,244
PROVINCE 2 河北 560,200 590,195 595,215 590,235 575,250 560,250 548,240 540,230 540,215 545,205
PROVINCE 3 山西 530,210 545,205 540,230 525,245 510,245 500,235 510,215 520,210
PROVINCE 4 内蒙古 440,80 520,85 560,110 570,130 560,170 580,200 560,200 545,205 520,210 510,220 480,230 440,215 400,210 370,190 380,150 400,110
PROVINCE 5 辽宁 590,160 610,155 620,165 618,180 605,190 600,210 590,205 588,188
PROVINCE 6 吉林 600,120 620,118 630,130 628,150 618,165 610,160 600,165 590,155 595,135
PROVINCE 7 黑龙江 580,60 610,50 640,60 650,80 640,110 630,130 620,118 600,120 590,100 580,85
PROVINCE 8 山东 560,250 575,250 590,260 582,278 560,280 540,272 545,258
PROVINCE 9 河南 540,272 560,280 565,295 540,305 510,298 500,280 510,265 530,258
PROVINCE 10 陕西 500,235 520,245 510,265 500,280 480,285 465,270 460,250 478,238
PROVINCE 11 甘肃 400,220 460,222 478,238 460,250 440,278 400,285 360,270 340,250 360,230 380,222
PROVINCE 12 宁夏 460,222 478,238 465,230 455,218
PROVINCE 13 青海 330,200 360,225 400,220 380,222 360,230 340,250 310,260 280,240 290,215 310,205
PROVINCE 14 新疆 200,80 280,50 330,60 380,80 400,110 380,150 370,190 340,200 310,205 280,210 240,200 200,180 170,150 150,130 160,100
PROVINCE 15 西藏 200,200 280,210 290,215 310,260 290,310 250,330 210,320 170,290 150,250 170,220
PROVINCE 16 四川 340,250 400,285 440,278 430,320 390,360 380,390 340,380 310,350 310,320 310,290
PROVINCE 17 重庆 440,300 430,320 415,325 410,310 420,300
PROVINCE 18 湖北 500,280 510,298 540,305 535,320 510,340 490,330 480,300 490,285
PROVINCE 19 安徽 560,280 582,278 590,295 580,315 560,325 540,320 535,305
PROVINCE 20 江苏 590,260 600,270 595,290 582,300 572,290
PROVINCE 21 上海 595,275 602,275 602,280 595,280
PROVINCE 22 浙江 582,300 595,290 600,310 590,330 575,335 572,315
PROVINCE 23 江西 540,320 560,325 565,345 545,355 525,350 520,335 535,320
PROVINCE 24 湖南 500,320 510,340 535,340 545,355 530,375 500,380 480,360 490,340
PROVINCE 25 福建 572,345 590,330 600,340 595,365 578,370 565,360
PROVINCE 26 广东 530,375 545,380 555,410 530,430 500,420 490,400 500,390
PROVINCE 27 广西 480,360 500,380 500,420 480,420 450,400 440,380 460,365
PROVINCE 28 海南 520,430 530,430 530,440 520,440
PROVINCE 29 云南 380,360 410,355 440,380 450,400 430,420 390,420 360,400 350,380
PROVINCE 30 贵州 410,355 440,360 460,365 450,380 440,400 420,400 400,380
PROVINCE 31 台湾 610,355 620,352 622,362 612,365
PROVINCE 32 香港 545,420 550,420 550,425 545,425
PROVINCE 33 澳门 535,422 540,422 540,426 535,426

# 邻接关系
EDGES 0 1,2
EDGES 1 0,2
EDGES 2 0,1,3,4,8,9
EDGES 3 2,4,9,10
EDGES 4 2,3,5,6,7,10,11,12,14
EDGES 5 4,6
EDGES 6 4,5,7
EDGES 7 4,6
EDGES 8 2,9,19,20
EDGES 9 2,3,8,10,18,19
EDGES 10 3,4,9,11,12,16,18
EDGES 11 4,10,12,13,14,16
EDGES 12 4,10,11
EDGES 13 11,14,15,16
EDGES 14 4,11,13,15
EDGES 15 13,14,16,29
EDGES 16 10,11,13,15,17,18,24,27,29,30
EDGES 17 16,18
EDGES 18 9,10,16,17,19,23,24
EDGES 19 8,9,18,20,22,23
EDGES 20 8,19,21,22
EDGES 21 20,22
EDGES 22 19,20,21,25
EDGES 23 18,19,22,24,25
EDGES 24 16,18,23,26,27,30
EDGES 25 22,23,26
EDGES 26 24,25,27,28
EDGES 27 16,24,26,29,30
EDGES 28 26
EDGES 29 15,16,27,30
EDGES 30 16,24,27,29
EDGES 31 
EDGES 32 
EDGES 33 
```

- [ ] **Step 2: 创建美国地图数据 usa.map**

美国 50 州（简化多边形坐标，粗略地理位置）：

```
# 美国地图数据 — 50州简化多边形坐标
# 坐标基于简化地图，单位：像素坐标

PROVINCE 0 阿拉巴马 525,380 540,375 548,390 540,405 525,400
PROVINCE 1 阿拉斯加 80,80 160,60 200,120 160,180 100,160
PROVINCE 2 亚利桑那 200,320 230,310 250,335 240,360 210,360
PROVINCE 3 阿肯色 455,360 470,355 478,370 465,385 450,378
PROVINCE 4 加利福尼亚 110,270 160,250 190,270 200,310 180,340 130,340 100,310
PROVINCE 5 科罗拉多 260,250 310,245 320,275 300,300 255,290
PROVINCE 6 康涅狄格 650,210 660,208 662,218 652,220
PROVINCE 7 特拉华 625,280 635,278 637,288 627,290
PROVINCE 8 佛罗里达 540,440 580,435 600,460 570,480 540,470 530,455
PROVINCE 9 佐治亚 555,385 575,378 582,398 565,410 550,405
PROVINCE 10 夏威夷 140,480 160,478 165,492 145,494
PROVINCE 11 爱达荷 130,160 180,150 200,185 180,215 140,200 125,180
PROVINCE 12 伊利诺伊 490,250 510,245 515,265 505,280 485,275 478,260
PROVINCE 13 印第安纳 520,260 535,255 540,275 530,285 515,282
PROVINCE 14 爱荷华 440,225 465,218 478,240 465,260 440,255 430,240
PROVINCE 15 堪萨斯 380,290 420,280 440,305 420,325 380,320 370,305
PROVINCE 16 肯塔基 520,310 540,305 548,325 535,340 518,335
PROVINCE 17 路易斯安那 450,415 480,405 490,425 470,440 445,435
PROVINCE 18 缅因 660,120 680,115 685,140 665,145 655,135
PROVINCE 19 马里兰 610,285 625,280 627,295 615,300
PROVINCE 20 马萨诸塞 655,200 670,195 675,210 660,215
PROVINCE 21 密歇根 510,190 530,185 540,210 525,225 510,220
PROVINCE 22 明尼苏达 400,170 440,165 455,195 440,215 400,210 385,190
PROVINCE 23 密西西比 500,390 520,382 528,400 510,415 495,408
PROVINCE 24 密苏里 460,300 485,292 495,315 480,335 455,325 448,310
PROVINCE 25 蒙大拿 180,100 260,95 290,130 270,170 230,175 180,155 160,130
PROVINCE 26 内布拉斯加 340,230 380,220 400,250 395,275 360,275 335,260
PROVINCE 27 内华达 110,210 160,200 170,230 150,260 105,250
PROVINCE 28 新罕布什尔 650,175 665,170 668,185 655,188
PROVINCE 29 新泽西 635,255 650,252 652,265 642,268
PROVINCE 30 新墨西哥 240,310 290,300 310,335 280,360 235,350
PROVINCE 31 纽约 630,195 650,190 655,215 640,230 625,222
PROVINCE 32 北卡罗来纳 580,325 600,318 608,340 592,355 575,345
PROVINCE 33 北达科他 330,155 390,150 400,175 380,195 335,185 320,170
PROVINCE 34 俄亥俄 535,270 555,265 560,288 545,300 530,295
PROVINCE 35 俄克拉荷马 370,340 420,330 440,355 420,380 375,375 360,360
PROVINCE 36 俄勒冈 60,160 120,152 130,185 100,215 55,200
PROVINCE 37 宾夕法尼亚 600,250 618,245 625,268 610,280 595,275
PROVINCE 38 罗德岛 665,210 672,208 674,216 666,218
PROVINCE 39 南卡罗来纳 570,355 588,348 595,365 580,378 565,372
PROVINCE 40 南达科他 320,200 370,195 390,225 370,250 325,242 310,225
PROVINCE 41 田纳西 520,340 540,335 548,355 535,370 518,362
PROVINCE 42 德克萨斯 340,390 420,370 450,405 420,450 360,450 320,425
PROVINCE 43 犹他 180,230 230,220 250,255 225,280 165,275 168,250
PROVINCE 44 佛蒙特 640,160 655,158 658,172 645,175
PROVINCE 45 弗吉尼亚 590,295 610,288 618,310 600,325 585,318
PROVINCE 46 华盛顿 65,100 130,95 140,130 110,160 55,150
PROVINCE 47 西弗吉尼亚 580,300 595,295 600,310 585,318 572,312
PROVINCE 48 威斯康星 430,190 460,185 470,210 455,225 432,218
PROVINCE 49 怀俄明 220,170 275,160 300,195 280,230 225,220 210,195

# 邻接关系
EDGES 0 3,8,9,23,41
EDGES 1 
EDGES 2 4,5,27,30,43
EDGES 3 0,17,23,24,35,41,42
EDGES 4 2,27,36
EDGES 5 2,15,26,30,35,43,49
EDGES 6 20,31,38
EDGES 7 19,29
EDGES 8 0,9
EDGES 9 0,8,32,39,41
EDGES 10 
EDGES 11 27,36,43,46,49
EDGES 12 13,14,16,21,24,48
EDGES 13 12,16,21,34
EDGES 14 12,22,24,26,48
EDGES 15 5,24,26,35
EDGES 16 12,13,34,41,45,47
EDGES 17 3,23,35,42
EDGES 18 28
EDGES 19 7,29,37,45
EDGES 20 6,28,31,38
EDGES 21 12,13,34,48
EDGES 22 14,26,33,40,48
EDGES 23 0,3,17,41
EDGES 24 3,12,14,15,26,35
EDGES 25 11,22,33,40,49
EDGES 26 5,14,15,22,24,33,40
EDGES 27 2,4,11,36,43
EDGES 28 18,20,44
EDGES 29 7,31,37
EDGES 30 2,5,35,42
EDGES 31 6,20,29,37,44
EDGES 32 9,39,41,45
EDGES 33 22,25,26,40
EDGES 34 13,16,21,37
EDGES 35 3,15,17,23,24,30,42
EDGES 36 4,11,27,46
EDGES 37 19,29,31,34,45,47
EDGES 38 6,20
EDGES 39 9,32,41
EDGES 40 22,25,26,33
EDGES 41 0,3,9,16,23,32,39,45
EDGES 42 3,17,30,35
EDGES 43 2,5,11,25,27
EDGES 44 18,28,31
EDGES 45 16,19,32,37,41,47
EDGES 46 11,36
EDGES 47 16,37,45
EDGES 48 12,14,21,22
EDGES 49 5,11,25,40
```

- [ ] **Step 3: Commit**

```bash
git add data/china.map data/usa.map
git commit -m "feat: add China and USA map data files"
```

---

### Task 15: 最终验证

- [ ] **Step 1: 运行全部单元测试**

```bash
mvn test
```
Expected: All tests pass

- [ ] **Step 2: 启动程序验证**

```bash
mvn javafx:run
```

手动验证：
1. 点击"中国地图" → 地图显示
2. 点击"贪心着色" → 省份着色，相邻颜色不同
3. 点击"贪心+排序" → 对比颜色数
4. 点击"回溯求最优" → 颜色数 ≤ 贪心
5. 切换到"数据管理" → 省份列表显示
6. 选中省份 → 邻接关系显示
7. 地图缩放/拖拽正常

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: final verification, all tests pass"
```
