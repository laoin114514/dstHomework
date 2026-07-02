# 地图着色动画 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为三种着色算法添加动画模式，支持逐省份着色动画和回溯算法试错过程的可视化，通过"瞬间/动画"模式切换控制。

**Architecture:** 新增 `ColoringStep` 步骤模型和 `ColoringAnimator` 动画控制器。三种算法从"直接修改 Graph 颜色"改为"返回 `List<ColoringStep>`"，由 Timeline 驱动逐帧播放。瞬间模式下算法生成步骤但跳过动画直接应用最终结果。

**Tech Stack:** Java 17+, JavaFX 21, Maven, JUnit 5

---

### Task 1: ColoringStep 步骤模型

**Files:**
- Create: `src/main/java/com/mapcoloring/ui/ColoringStep.java`

- [ ] **Step 1: 创建 ColoringStep**

```java
package com.mapcoloring.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * 着色过程中的单个步骤，用于动画播放。
 * 三种动作：TRY（试探着色）、COMMIT（确认着色）、UNDO（撤销着色）
 */
public class ColoringStep {

    public enum Action {
        TRY,    // 回溯试探：尝试给省份着色
        COMMIT, // 确认着色：贪心每步 / 回溯找到可行解后确认
        UNDO    // 撤销着色：回溯回退时取消之前的试探
    }

    public Action action;
    public int provinceIndex;
    public int color;       // UNDO 时忽略
    public String description;

    public ColoringStep(Action action, int provinceIndex, int color,
                        String provinceName) {
        this.action = action;
        this.provinceIndex = provinceIndex;
        this.color = color;
        this.description = buildDescription(provinceName);
    }

    private String buildDescription(String name) {
        switch (action) {
            case TRY:    return name + " ← 颜色" + color + " (尝试)";
            case COMMIT: return name + " ← 颜色" + color;
            case UNDO:   return name + " 撤销着色";
            default:     return "";
        }
    }

    /**
     * 从步骤列表提取最终颜色状态，应用到 graph。
     * 遍历所有 COMMIT 步骤，取每个省份最后一次 COMMIT 的颜色。
     */
    public static void applyFinalColors(
            List<ColoringStep> steps,
            com.mapcoloring.model.Graph graph) {
        for (ColoringStep step : steps) {
            if (step.action == Action.COMMIT) {
                graph.setColor(step.provinceIndex, step.color);
            }
        }
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
git add src/main/java/com/mapcoloring/ui/ColoringStep.java
git commit -m "feat: add ColoringStep model for animation steps"
```

---

### Task 2: ColoringResult 扩展

**Files:**
- Modify: `src/main/java/com/mapcoloring/algorithm/ColoringResult.java`

- [ ] **Step 1: 修改 ColoringResult，新增 steps 字段**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.ui.ColoringStep;
import java.util.ArrayList;
import java.util.List;

public class ColoringResult {
    public int colorCount;
    public long timeNanos;
    public List<ColoringStep> steps;  // 新增：步骤列表，瞬间模式为 null

    public ColoringResult(int colorCount, long timeNanos) {
        this.colorCount = colorCount;
        this.timeNanos = timeNanos;
        this.steps = null;
    }

    public ColoringResult(int colorCount, long timeNanos,
                          List<ColoringStep> steps) {
        this.colorCount = colorCount;
        this.timeNanos = timeNanos;
        this.steps = steps;
    }

    public double timeMillis() {
        return timeNanos / 1_000_000.0;
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
git add src/main/java/com/mapcoloring/algorithm/ColoringResult.java
git commit -m "feat: add steps field to ColoringResult for animation support"
```

---

### Task 3: MapCanvas 新增单省着色方法

**Files:**
- Modify: `src/main/java/com/mapcoloring/ui/MapCanvas.java`

- [ ] **Step 1: 新增 colorProvince 和 uncolorProvince 方法**

在 `MapCanvas.java` 末尾（`pointInPolygon` 方法之后、`getHighlightedIndex` 之前）添加两个方法：

```java
    /**
     * 给单个省份着色并重绘画布。
     * 供 ColoringAnimator 在动画播放时调用。
     */
    public void colorProvince(int index, int color) {
        if (mapData == null || index < 0 || index >= mapData.graph.getVertexCount()) {
            return;
        }
        mapData.graph.setColor(index, color);
        draw();
    }

    /**
     * 取消单个省份颜色（恢复灰色/未着色状态）并重绘画布。
     * 供 ColoringAnimator 在回溯撤销时调用。
     */
    public void uncolorProvince(int index) {
        if (mapData == null || index < 0 || index >= mapData.graph.getVertexCount()) {
            return;
        }
        mapData.graph.setColor(index, 0);
        draw();
    }
```

在 `getMapData()` 方法之前、`pointInPolygon` 方法之后插入。

- [ ] **Step 2: 编译验证**

```bash
mvn compile
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/mapcoloring/ui/MapCanvas.java
git commit -m "feat: add colorProvince/uncolorProvince to MapCanvas for animation"
```

---

### Task 4: GreedyColoring 改造为返回步骤列表

**Files:**
- Modify: `src/main/java/com/mapcoloring/algorithm/GreedyColoring.java`
- Modify: `src/test/java/com/mapcoloring/algorithm/GreedyColoringTest.java`

- [ ] **Step 1: 更新测试，增加步骤验证**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.ui.ColoringStep;
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

        // 新增：验证步骤列表
        assertNotNull(result.steps);
        assertEquals(3, result.steps.size());
        for (ColoringStep step : result.steps) {
            assertEquals(ColoringStep.Action.COMMIT, step.action);
        }
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
        assertNotNull(result.steps);
        assertEquals(3, result.steps.size());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=GreedyColoringTest
```
Expected: FAIL — `result.steps` 为 null

- [ ] **Step 3: 改造 GreedyColoring.color() 生成步骤列表**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;
import com.mapcoloring.ui.ColoringStep;
import java.util.ArrayList;
import java.util.List;

public class GreedyColoring {

    public static ColoringResult color(Graph g) {
        long start = System.nanoTime();
        g.resetColors();

        List<ColoringStep> steps = new ArrayList<>();

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
            steps.add(new ColoringStep(ColoringStep.Action.COMMIT,
                    i, color, g.getVertexName(i)));
        }

        int maxColor = 0;
        for (int i = 0; i < g.getVertexCount(); i++) {
            if (g.getColor(i) > maxColor) {
                maxColor = g.getColor(i);
            }
        }
        long elapsed = System.nanoTime() - start;
        return new ColoringResult(maxColor, elapsed, steps);
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -Dtest=GreedyColoringTest
```
Expected: Tests run: 2, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mapcoloring/algorithm/GreedyColoring.java src/test/java/com/mapcoloring/algorithm/GreedyColoringTest.java
git commit -m "feat: GreedyColoring returns step list for animation"
```

---

### Task 5: SortedGreedyColoring 改造为返回步骤列表

**Files:**
- Modify: `src/main/java/com/mapcoloring/algorithm/SortedGreedyColoring.java`
- Modify: `src/test/java/com/mapcoloring/algorithm/SortedGreedyColoringTest.java`

- [ ] **Step 1: 更新测试**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.ui.ColoringStep;
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
        assertNotNull(r2.steps);
        assertEquals(1, r2.steps.size());
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

    @Test
    void testStepsAllCommit() {
        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addEdge(0, 1);
        g.addEdge(1, 2);

        ColoringResult result = SortedGreedyColoring.color(g);
        assertNotNull(result.steps);
        assertEquals(3, result.steps.size());
        for (ColoringStep step : result.steps) {
            assertEquals(ColoringStep.Action.COMMIT, step.action);
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=SortedGreedyColoringTest
```
Expected: FAIL — `result.steps` 为 null 或只有 1 个 test 方法

- [ ] **Step 3: 改造 SortedGreedyColoring.color()**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;
import com.mapcoloring.ui.ColoringStep;
import java.util.ArrayList;
import java.util.List;

public class SortedGreedyColoring {

    public static ColoringResult color(Graph g) {
        long start = System.nanoTime();
        g.resetColors();

        List<ColoringStep> steps = new ArrayList<>();

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
            steps.add(new ColoringStep(ColoringStep.Action.COMMIT,
                    i, color, g.getVertexName(i)));
        }

        int maxColor = 0;
        for (int i = 0; i < g.getVertexCount(); i++) {
            if (g.getColor(i) > maxColor) {
                maxColor = g.getColor(i);
            }
        }
        long elapsed = System.nanoTime() - start;
        return new ColoringResult(maxColor, elapsed, steps);
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -Dtest=SortedGreedyColoringTest
```
Expected: Tests run: 3, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mapcoloring/algorithm/SortedGreedyColoring.java src/test/java/com/mapcoloring/algorithm/SortedGreedyColoringTest.java
git commit -m "feat: SortedGreedyColoring returns step list for animation"
```

---

### Task 6: BacktrackColoring 重构为返回步骤列表

**Files:**
- Modify: `src/main/java/com/mapcoloring/algorithm/BacktrackColoring.java`
- Modify: `src/test/java/com/mapcoloring/algorithm/BacktrackColoringTest.java`

这是最复杂的改造。核心思路：
1. 外层循环从排序贪心上界 k 开始，逐步降低颜色数
2. 每轮调用 `backtrack()` 记录 TRY/UNDO 步骤
3. 找到可行解后追加 COMMIT 步骤确认该轮结果
4. 记录最佳解（最少颜色数）对应的完整步骤列表

- [ ] **Step 1: 更新测试**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.ui.ColoringStep;
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

        // 新增：验证步骤列表
        assertNotNull(result.steps);
        assertFalse(result.steps.isEmpty(),
            "回溯应该生成步骤列表");
        // 验证有 COMMIT 步骤
        long commitCount = result.steps.stream()
            .filter(s -> s.action == ColoringStep.Action.COMMIT).count();
        assertEquals(3, commitCount,
            "三角形需要 3 个省各一条 COMMIT");
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
        assertNotNull(result.steps);

        long commitCount = result.steps.stream()
            .filter(s -> s.action == ColoringStep.Action.COMMIT).count();
        assertEquals(3, commitCount);
    }

    @Test
    void testSingleVertexNeedsOneColor() {
        Graph g = new Graph();
        g.addVertex("A");
        ColoringResult result = BacktrackColoring.color(g);
        assertEquals(1, result.colorCount);
        assertNotNull(result.steps);

        long commitCount = result.steps.stream()
            .filter(s -> s.action == ColoringStep.Action.COMMIT).count();
        assertEquals(1, commitCount);
    }

    @Test
    void testNoAdjacentCommitConflicts() {
        // 验证最终 COMMIT 中相邻省份颜色不同
        Graph g = new Graph();
        for (int i = 0; i < 6; i++) {
            g.addVertex("V" + i);
        }
        // 构造一个需要 3 色的图: 0-1-2-3-4-5 链 + 0-2, 1-3, 2-4, 3-5
        g.addEdge(0, 1); g.addEdge(1, 2); g.addEdge(2, 3);
        g.addEdge(3, 4); g.addEdge(4, 5);
        g.addEdge(0, 2); g.addEdge(1, 3); g.addEdge(2, 4); g.addEdge(3, 5);

        ColoringResult result = BacktrackColoring.color(g);
        assertNotNull(result.steps);

        // 收集每个省份的最终 COMMIT 颜色
        int[] finalColors = new int[6];
        for (ColoringStep step : result.steps) {
            if (step.action == ColoringStep.Action.COMMIT) {
                finalColors[step.provinceIndex] = step.color;
            }
        }
        // 验证相邻省份颜色不同
        for (int i = 0; i < g.getVertexCount(); i++) {
            var neighbors = g.getNeighbors(i);
            for (int j = 0; j < neighbors.size(); j++) {
                int n = neighbors.get(j);
                assertNotEquals(finalColors[i], finalColors[n],
                    "相邻省份 " + i + " 和 " + n + " 颜色相同");
            }
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -Dtest=BacktrackColoringTest
```
Expected: FAIL — `result.steps` 为 null

- [ ] **Step 3: 重写 BacktrackColoring**

```java
package com.mapcoloring.algorithm;

import com.mapcoloring.model.Graph;
import com.mapcoloring.model.MyArrayList;
import com.mapcoloring.ui.ColoringStep;
import java.util.ArrayList;
import java.util.List;

public class BacktrackColoring {

    private static final int MAX_STEPS = 50000;

    private Graph graph;
    private int bestColorCount;
    private int[] bestColors;
    private List<ColoringStep> bestSteps;

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
        bestSteps = null;

        // 先用排序贪心得到上界
        ColoringResult greedyResult = SortedGreedyColoring.color(g);
        int upperBound = greedyResult.colorCount;
        for (int i = 0; i < n; i++) {
            bestColors[i] = g.getColor(i);
        }
        // +1 保证第一轮回溯可以使用完整的 upperBound 种颜色
        bestColorCount = upperBound + 1;

        // 外层循环：从贪心上界开始逐步尝试更少的颜色
        for (int k = upperBound; k >= 1; k--) {
            g.resetColors();
            int[] currentColors = new int[n];
            List<ColoringStep> roundSteps = new ArrayList<>();

            boolean found = backtrack(0, k, currentColors, roundSteps);

            if (found) {
                // 追加 COMMIT 步骤确认该轮解
                for (int i = 0; i < n; i++) {
                    roundSteps.add(new ColoringStep(
                        ColoringStep.Action.COMMIT,
                        i, currentColors[i], g.getVertexName(i)));
                }
                bestSteps = roundSteps;
                bestColorCount = k;
                System.arraycopy(currentColors, 0, bestColors, 0, n);
            } else {
                break; // 无法用更少的颜色，结束搜索
            }

            // 安全阈值：步骤过多时停止
            if (roundSteps.size() > MAX_STEPS) {
                break;
            }
        }

        // 将最佳颜色应用到 graph
        for (int i = 0; i < n; i++) {
            g.setColor(i, bestColors[i]);
        }

        long elapsed = System.nanoTime() - start;
        return new ColoringResult(bestColorCount, elapsed, bestSteps);
    }

    /**
     * 递归回溯搜索。在每个顶点尝试颜色 1..maxColors。
     * 成功时返回 true，失败时返回 false。
     * 试探着色记录 TRY 步骤，回退记录 UNDO 步骤。
     */
    private boolean backtrack(int idx, int maxColors,
                              int[] currentColors, List<ColoringStep> steps) {
        // 安全检查：步骤数超限
        if (steps.size() > MAX_STEPS) {
            return false;
        }

        if (idx == graph.getVertexCount()) {
            return true; // 所有省份着色完毕
        }

        for (int c = 1; c <= maxColors && c < bestColorCount; c++) {
            if (isValid(idx, c, currentColors)) {
                currentColors[idx] = c;
                steps.add(new ColoringStep(ColoringStep.Action.TRY,
                        idx, c, graph.getVertexName(idx)));

                if (backtrack(idx + 1, maxColors, currentColors, steps)) {
                    return true;
                }

                // 回退：撤销当前尝试
                steps.add(new ColoringStep(ColoringStep.Action.UNDO,
                        idx, 0, graph.getVertexName(idx)));
                currentColors[idx] = 0;
            }
        }
        return false;
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
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -Dtest=BacktrackColoringTest
```
Expected: Tests run: 4, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/mapcoloring/algorithm/BacktrackColoring.java src/test/java/com/mapcoloring/algorithm/BacktrackColoringTest.java
git commit -m "feat: BacktrackColoring returns step list with TRY/UNDO/COMMIT"
```

---

### Task 7: ColoringAnimator 动画控制器

**Files:**
- Create: `src/main/java/com/mapcoloring/ui/ColoringAnimator.java`

- [ ] **Step 1: 创建 ColoringAnimator**

```java
package com.mapcoloring.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;

/**
 * 驱动着色步骤的逐帧播放。
 * 使用 JavaFX Timeline，每 delayMs 毫秒执行一个步骤。
 */
public class ColoringAnimator {

    private final MapCanvas canvas;
    private Timeline timeline;
    private List<ColoringStep> steps;
    private int currentStep;
    private long delayMs;
    private Runnable onFinished;
    private Runnable onStep;       // 每步回调，用于更新 UI 进度

    public ColoringAnimator(MapCanvas canvas) {
        this.canvas = canvas;
        this.delayMs = 300; // 默认 300ms
    }

    /**
     * 开始播放步骤列表。
     * @param steps 步骤列表
     * @param onFinished 播放完成回调
     * @param onStep 每步回调（用于更新步骤进度显示）
     */
    public void play(List<ColoringStep> steps,
                     Runnable onFinished, Runnable onStep) {
        if (steps == null || steps.isEmpty()) {
            if (onFinished != null) onFinished.run();
            return;
        }

        this.steps = steps;
        this.currentStep = 0;
        this.onFinished = onFinished;
        this.onStep = onStep;

        timeline = new Timeline(
            new KeyFrame(Duration.millis(delayMs), e -> onTick())
        );
        timeline.setCycleCount(steps.size());
        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        timeline.play();
    }

    private void onTick() {
        if (currentStep >= steps.size()) {
            timeline.stop();
            return;
        }

        ColoringStep step = steps.get(currentStep);
        switch (step.action) {
            case TRY:
            case COMMIT:
                canvas.colorProvince(step.provinceIndex, step.color);
                break;
            case UNDO:
                canvas.uncolorProvince(step.provinceIndex);
                break;
        }

        currentStep++;
        if (onStep != null) onStep.run();
    }

    public void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    public void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    /**
     * 调整播放速度。
     * @param delayMs 步骤间隔毫秒数，范围 30~1000
     */
    public void setSpeed(long delayMs) {
        this.delayMs = Math.max(30, Math.min(1000, delayMs));
    }

    public boolean isPlaying() {
        return timeline != null
            && timeline.getStatus() == Timeline.Status.RUNNING;
    }

    public boolean isPaused() {
        return timeline != null
            && timeline.getStatus() == Timeline.Status.PAUSED;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return steps != null ? steps.size() : 0;
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
git add src/main/java/com/mapcoloring/ui/ColoringAnimator.java
git commit -m "feat: add ColoringAnimator with Timeline-driven step playback"
```

---

### Task 8: FrontPanel UI 改造

**Files:**
- Modify: `src/main/java/com/mapcoloring/ui/FrontPanel.java`

这是 UI 改动最大的部分，新增模式切换、速度滑块、步骤进度显示，以及动画播放时的按钮锁定逻辑。

- [ ] **Step 1: 重写 FrontPanel**

```java
package com.mapcoloring.ui;

import com.mapcoloring.algorithm.BacktrackColoring;
import com.mapcoloring.algorithm.ColoringResult;
import com.mapcoloring.algorithm.GreedyColoring;
import com.mapcoloring.algorithm.SortedGreedyColoring;
import com.mapcoloring.io.MapFileHandler;
import com.mapcoloring.model.MapData;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class FrontPanel extends BorderPane {

    private MapCanvas canvas;
    private MapData chinaData;
    private ColoringAnimator animator;
    private boolean animationMode = false; // false=瞬间, true=动画

    private Label colorCountLabel;
    private Label timeLabel;
    private Label algoLabel;
    private Label stepLabel;

    private Button greedyBtn;
    private Button sortedBtn;
    private Button backtrackBtn;
    private Button reloadBtn;
    private Slider speedSlider;
    private Label speedLabel;

    private ToggleButton instantToggle;
    private ToggleButton animToggle;
    private ToggleGroup modeGroup;

    public FrontPanel() {
        canvas = new MapCanvas();
        animator = new ColoringAnimator(canvas);
        setCenter(canvas);

        VBox rightPanel = createRightPanel();
        setRight(rightPanel);
        BorderPane.setMargin(rightPanel, new Insets(10));

        loadChina();
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(210);

        Label titleLabel = new Label("中国地图着色");
        titleLabel.setFont(new Font(18));

        Separator sep1 = new Separator();

        // === 着色算法按钮 ===
        Label algoLabelTitle = new Label("着色算法");
        algoLabelTitle.setFont(new Font(12));

        greedyBtn = new Button("贪心着色");
        greedyBtn.setMaxWidth(Double.MAX_VALUE);
        greedyBtn.setOnAction(e -> runGreedy());

        sortedBtn = new Button("贪心+排序");
        sortedBtn.setMaxWidth(Double.MAX_VALUE);
        sortedBtn.setOnAction(e -> runSortedGreedy());

        backtrackBtn = new Button("回溯求最优");
        backtrackBtn.setMaxWidth(Double.MAX_VALUE);
        backtrackBtn.setOnAction(e -> runBacktrack());

        Separator sep2 = new Separator();

        // === 模式切换 ===
        Label modeLabel = new Label("着色模式");
        modeLabel.setFont(new Font(12));

        modeGroup = new ToggleGroup();
        instantToggle = new ToggleButton("瞬间着色");
        instantToggle.setMaxWidth(Double.MAX_VALUE);
        instantToggle.setToggleGroup(modeGroup);
        instantToggle.setSelected(true);
        instantToggle.setOnAction(e -> {
            animationMode = false;
            speedSlider.setDisable(true);
        });

        animToggle = new ToggleButton("动画着色");
        animToggle.setMaxWidth(Double.MAX_VALUE);
        animToggle.setToggleGroup(modeGroup);
        animToggle.setOnAction(e -> {
            animationMode = true;
            speedSlider.setDisable(false);
        });

        // === 速度滑块 ===
        Label speedTitleLabel = new Label("动画速度");
        speedTitleLabel.setFont(new Font(12));

        speedSlider = new Slider(30, 1000, 300);
        speedSlider.setDisable(true);
        speedSlider.setBlockIncrement(10);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            animator.setSpeed(newVal.longValue());
            speedLabel.setText((int) Math.round(newVal.doubleValue()) + " ms");
        });

        speedLabel = new Label("300 ms");

        HBox speedRange = new HBox();
        speedRange.setSpacing(4);
        Label fastLabel = new Label("快");
        Label slowLabel = new Label("慢");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        speedRange.getChildren().addAll(fastLabel, spacer, slowLabel);

        Separator sep3 = new Separator();

        // === 统计信息 ===
        Label statsLabel = new Label("统计信息");
        statsLabel.setFont(new Font(12));

        colorCountLabel = new Label("颜色总数：--");
        timeLabel = new Label("耗时：--");
        algoLabel = new Label("算法：--");
        stepLabel = new Label("");

        reloadBtn = new Button("重新加载");
        reloadBtn.setMaxWidth(Double.MAX_VALUE);
        reloadBtn.setOnAction(e -> {
            animator.stop();
            loadChina();
        });

        panel.getChildren().addAll(
            titleLabel,
            sep1,
            algoLabelTitle, greedyBtn, sortedBtn, backtrackBtn,
            sep2,
            modeLabel, instantToggle, animToggle,
            speedTitleLabel, speedSlider, speedLabel, speedRange,
            sep3,
            statsLabel, colorCountLabel, timeLabel, algoLabel, stepLabel,
            new Separator(),
            reloadBtn
        );

        return panel;
    }

    private void loadChina() {
        chinaData = MapFileHandler.load("data/china.map");
        canvas.setMapData(chinaData);
        resetStats();
    }

    private void resetStats() {
        colorCountLabel.setText("颜色总数：--");
        timeLabel.setText("耗时：--");
        algoLabel.setText("算法：--");
        stepLabel.setText("");
    }

    // === 算法执行入口 ===

    private void runGreedy() {
        ColoringResult r = GreedyColoring.color(chinaData.graph);
        handleResult(r, "贪心着色");
    }

    private void runSortedGreedy() {
        ColoringResult r = SortedGreedyColoring.color(chinaData.graph);
        handleResult(r, "贪心+排序");
    }

    private void runBacktrack() {
        ColoringResult r = BacktrackColoring.color(chinaData.graph);
        handleResult(r, "回溯求最优");
    }

    private void handleResult(ColoringResult r, String algoName) {
        if (animationMode && r.steps != null && !r.steps.isEmpty()) {
            // 动画模式：先重置图颜色，再通过动画逐步着色
            chinaData.graph.resetColors();
            canvas.draw();
            setButtonsDisabled(true);
            stepLabel.setText("步骤：0 / " + r.steps.size());

            animator.setSpeed((long) speedSlider.getValue());
            animator.play(r.steps, () -> {
                // 播放完成
                Platform.runLater(() -> {
                    updateStats(r, algoName);
                    setButtonsDisabled(false);
                    stepLabel.setText("步骤：" + r.steps.size()
                            + " / " + r.steps.size());
                });
            }, () -> {
                // 每步回调：更新进度
                Platform.runLater(() -> {
                    stepLabel.setText("步骤：" + animator.getCurrentStep()
                            + " / " + animator.getTotalSteps());
                });
            });
        } else {
            // 瞬间模式：算法已着色 graph，直接重绘画布
            canvas.draw();
            updateStats(r, algoName);
        }
    }

    private void updateStats(ColoringResult r, String algoName) {
        colorCountLabel.setText("颜色总数：" + r.colorCount);
        timeLabel.setText(String.format("耗时：%.2f ms", r.timeMillis()));
        algoLabel.setText("算法：" + algoName);
    }

    private void setButtonsDisabled(boolean disabled) {
        greedyBtn.setDisable(disabled);
        sortedBtn.setDisable(disabled);
        backtrackBtn.setDisable(disabled);
        reloadBtn.setDisable(disabled);
        instantToggle.setDisable(disabled);
        animToggle.setDisable(disabled);
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
git commit -m "feat: add animation mode toggle, speed slider, and step progress to FrontPanel"
```

---

### Task 9: 最终验证

- [ ] **Step 1: 运行全部单元测试**

```bash
mvn test
```
Expected: All tests pass (MyArrayListTest: 11, GraphTest: 7, BacktrackColoringTest: 4, GreedyColoringTest: 2, SortedGreedyColoringTest: 3, SortUtilTest: 2, MapFileHandlerTest: 2)

- [ ] **Step 2: 启动程序手动验证**

```bash
mvn javafx:run
```

手动验证清单：
1. ~~默认"瞬间着色"模式已选中，速度滑块禁用~~
2. ~~点击"贪心着色" → 地图瞬间着色完成，统计信息更新~~
3. ~~点击"贪心+排序" → 同上~~
4. ~~点击"回溯求最优" → 同上，颜色数 ≤ 贪心~~
5. ~~切换到"动画着色"模式 → 速度滑块启用~~
6. ~~点击"贪心着色" → 逐省填充动画，步骤进度更新~~
7. ~~拖动速度滑块 → 动画速度变化~~
8. ~~点击"回溯求最优" → 看到 TRY/UNDO/COMMIT 过程，最终颜色数正确~~
9. ~~动画播放中按钮全部禁用~~
10. ~~点击"重新加载" → 动画停止，地图重置~~

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: final verification, all tests pass, animation feature complete"
```
