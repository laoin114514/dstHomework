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
        // 构造一个需要 3 色的图: 0-1-2-3-4-5 链 + 额外边
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
