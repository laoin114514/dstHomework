# io 模块 — 文件读写

## 模块职责

负责地图数据文件的读取和写入，使用自定义文本格式存储地图信息。

## 文件格式

```
# 注释行（以 # 开头）
PROVINCE id name x1,y1 x2,y2 x3,y3 ...
EDGES id neighbor1,neighbor2,...
```

- `PROVINCE` 行：省份编号、名称、多边形顶点坐标序列
- `EDGES` 行：省份编号、邻接省份编号列表
- 坐标格式：`x,y`，逗号分隔

## MapFileHandler

### save — 写入

1. 遍历 `MapData.provinces`，写入每个省份的 `PROVINCE` 行
2. 遍历 `MapData.graph`，写入每个顶点的 `EDGES` 行（只写非空的）
3. 使用 **UTF-8 编码**显式指定（避免 Windows 中文环境下 GBK 默认编码导致的乱码）

### load — 读取

1. 逐行读取，跳过空行和 `#` 开头的注释行
2. `PROVINCE` 行：解析 id、名称（支持多词名称如 "North Dakota"）、坐标序列
3. `EDGES` 行：解析邻接关系，支持逗号或空格分隔的邻居列表
4. 构建完整的 `MapData` 对象返回

### 名称解析

省份名称可能包含空格（如 "District of Columbia"）。解析时从 id 之后逐个读取 token，直到遇到第一个坐标（含逗号的 token），之间的所有 token 用空格拼接为名称。

## 为什么用文件而不是数据库？

课程要求"使用文件存储数据，不能使用数据库"。自定义文本格式的优势：
- 人类可读，可直接用文本编辑器修改
- 无需额外依赖
- 适合课程作业的数据量级
