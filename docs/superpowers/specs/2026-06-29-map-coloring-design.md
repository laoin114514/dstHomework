# 地图着色 — 数据结构综合实践 设计文档

**日期：** 2026-06-29
**选题：** 20 — 地图着色
**语言：** Java + JavaFX

## 1. 项目概述

实现地图着色程序，输入中国和美国地图数据，使用图着色算法为各省/州着色，使相邻省份颜色不同且颜色总数最少，并以图形界面展示。

## 2. 总体架构

```
map-coloring/
├── src/main/java/com/mapcoloring/
│   ├── model/              ← 自实现数据结构
│   │   ├── Graph.java         邻接表图
│   │   ├── Province.java      省份实体
│   │   ├── MapData.java       地图数据容器
│   │   └── MyArrayList.java   自实现动态数组
│   ├── algorithm/          ← 自实现着色算法
│   │   ├── GreedyColoring.java      贪心着色
│   │   ├── SortedGreedyColoring.java 贪心+度数排序
│   │   └── BacktrackColoring.java    回溯求最优解
│   ├── io/                 ← 自实现文件读写
│   │   └── MapFileHandler.java
│   ├── ui/                 ← JavaFX 图形界面
│   │   ├── MainApp.java        主窗口 + 标签页
│   │   ├── MapCanvas.java      Canvas 地图绑制
│   │   ├── FrontPanel.java     前台：看地图+着色结果
│   │   └── AdminPanel.java     后台：管理省份+邻接关系
│   └── Main.java
├── data/
│   ├── china.map          中国地图数据
│   └── usa.map            美国地图数据
```

**依赖关系：** `ui -> algorithm -> model`，`ui -> io -> model`，model 不依赖任何模块。

## 3. 自实现数据结构

### MyArrayList\<T\>
- 底层数组 + 手动扩容，实现增删查改、下标访问、迭代遍历
- 替代 java.util.ArrayList

### Graph（邻接表）
- vertices: MyArrayList\<Vertex\> — 顶点 (id, name, color)
- adjList: 每个顶点对应一个 MyArrayList\<Edge\> — 邻接边表
- 核心方法：addVertex, addEdge, getNeighbors(id), getDegree(id), getVertices
- 选用邻接表而非邻接矩阵：地图是稀疏平面图，每个省平均 3~6 个邻居

### Province
- id, name, polygon(MyArrayList\<Point\>), color

### Point
- double x, y — 多边形顶点坐标

## 4. 自实现算法

### 排序算法
- 冒泡排序或插入排序，用于按度数排序省份

### 查找算法
- 线性查找，用于查找未使用的最小颜色编号

### 着色算法

**算法1 — 贪心着色（低效对比方案）**
1. 任意顺序遍历省份
2. 为每个省份分配第一个不与已着色邻居冲突的颜色
3. 时间 O(V²)，不保证最优

**算法2 — 贪心+度数排序（优化方案）**
1. 将省份按度数从高到低排序
2. 依次分配第一个可用颜色
3. 时间 O(V²)，通常颜色更少

**算法3 — 简单回溯（最优方案）**
1. 对省份逐个尝试颜色 1, 2, 3...，不与已着色邻居冲突即通过
2. 冲突则换下一个颜色，所有颜色都失败则退回上一个省换色
3. 找到一个方案后，尝试用更少的颜色重做
4. 最终保证最少颜色数

三种算法递进对比：贪心 → 排序优化 → 回溯最优，方便答辩讲解。

## 5. 文件格式

```
# 注释行
PROVINCE id name x1,y1 x2,y2 x3,y3 ... x1,y1
EDGES id neighbor1,neighbor2,...
```

- PROVINCE 行定义省份及其多边形顶点（首尾需闭合）
- EDGES 行定义邻接关系
- # 开头为注释

## 6. 图形界面（JavaFX）

单窗口，双标签页：

**标签页1 — 地图着色（前台）：**
- 左侧：Canvas 地图画布，支持缩放/拖拽
- 右侧控制面板：地图切换（中国/美国）、算法选择（三个按钮）、统计信息（颜色总数、耗时）

**标签页2 — 数据管理（后台）：**
- 左侧：省份列表，支持选中、添加、删除
- 右侧：邻接关系编辑面板，显示已连接/未连接省份，支持连接/断开操作
- 底部：保存到文件、重新加载按钮

## 7. 异常处理

- 文件不存在/格式错误 → 弹窗提示
- 省份坐标不闭合 → 自动补全
- 邻接引用不存在的省份 → 跳过并警告

## 8. 测试策略

- 用已知结果的小规模地图验证三种算法的正确性
- 验证点：相邻省颜色不同、颜色数 ≤ 4、三种算法结果可对比
- 中国 34 省 + 美国 50 州作为完整输入验证

## 9. 项目构建

- 使用 Maven 管理 JavaFX 依赖
- JDK 17+
- JavaFX 通过 Maven 依赖引入，无需手动配置 module-path
