# model 模块 — 自实现数据结构

## 模块职责

提供地图着色所需的全部底层数据结构，**不依赖任何 Java 集合类库**（`java.util.*`），全部手动实现。

## 类层次

```
Point  ──→  Province  ──→  MapData
                ↑
MyArrayList  ←──  Graph  ──→  MapData
```

## 各类详解

### Point — 坐标点

最简单的数据单元，存储多边形的一个顶点坐标 `(x, y)`。在 `Province.polygon` 中串联成多边形的轮廓。

### MyArrayList\<T\> — 自实现动态数组

替代 `java.util.ArrayList` 的基础容器，是整个项目的存储基石。

| 特性 | 实现方式 |
|------|---------|
| 底层存储 | `Object[]` 数组，初始容量 8 |
| 扩容策略 | 容量不足时翻倍（×2），均摊 O(1) |
| 删除元素 | 后续元素前移，末尾置 null 防内存泄漏 |
| 遍历支持 | 实现 `Iterable<T>`，支持 for-each 语法 |
| 查找 | `indexOf` 线性查找，正确处理 null 值 |

**为什么不用 `java.util.ArrayList`？** 课程要求关键数据结构需自己实现。

### Graph — 邻接表图

地图着色的核心数据结构，用邻接表表示省份间的相邻关系。

**设计选择：邻接表 vs 邻接矩阵**

地图是稀疏图，每个省平均只邻接 3~6 个省。中国 34 省若用邻接矩阵需 1156 个存储单元，大部分为空。邻接表按需存储，空间效率更高。

**内部结构：三个平行数组**

```
names[i]   → 顶点 i 的名称（如 "北京"）
colors[i]  → 顶点 i 的颜色（0 = 未着色）
adjList[i] → 顶点 i 的邻居索引列表
```

顶点用数组下标标识，无需额外的 id 映射。

**核心方法**

- `addVertex(name)` — 三个数组同步添加
- `addEdge(from, to)` — 无向边，双向添加，`indexOf` 防重复
- `removeVertex(index)` — 删除后修正所有邻居列表中的索引偏移
- `getNeighbors(index)` / `getDegree(index)` — 邻接查询
- `getColor/setColor/resetColors` — 颜色管理（供着色算法调用）

### Province — 省份实体

```
id       → 省份编号
name     → 省份名称（如 "河北省"）
polygon  → MyArrayList<Point>，多边形顶点序列（≥3 个点）
color    → 着色后的颜色编号
```

### MapData — 地图数据聚合

顶层容器，将省份列表和邻接图绑定在一起，保证两者索引同步。

- `addProvince(p)` — 同时向 provinces 列表和 graph 中添加
- `removeProvince(index)` — 同时删除，graph 自动修正索引
- `addAdjacency(from, to)` — 代理到 graph.addEdge

## 设计原则

1. **无外部依赖** — 不引 `java.util.ArrayList/HashMap` 等高级集合，纯手动实现
2. **索引一致性** — Province 列表和 Graph 顶点一一对应，通过下标关联
3. **单一职责** — 每个类只做一件事，Point 存坐标，Province 存属性，Graph 存关系
