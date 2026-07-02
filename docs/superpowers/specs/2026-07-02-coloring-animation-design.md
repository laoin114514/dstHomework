# 地图着色动画 — 设计文档

**日期：** 2026-07-02
**项目：** 地图着色系统
**目标：** 为现有三种着色算法添加"瞬间着色"和"展示着色过程"两种模式，支持逐省份填充动画和回溯算法尝试过程的实时可视化。

## 1. 总体架构

新增 `ColoringStep` 步骤模型和 `ColoringAnimator` 动画控制器，三种算法从"直接修改 Graph 颜色"改为"返回 `List<ColoringStep>` 步骤列表"，由动画控制器驱动 UI 逐帧播放。

```
ui/
├── ColoringStep.java        ← 新增：步骤模型（纯数据类）
├── ColoringAnimator.java    ← 新增：动画控制器（Timeline 驱动）
├── MapCanvas.java           ← 修改：新增 colorProvince / uncolorProvince 方法
├── FrontPanel.java          ← 修改：新增模式切换、速度滑块、步骤进度
└── MainApp.java             ← 不变

algorithm/
├── ColoringResult.java      ← 修改：新增 steps 字段
├── GreedyColoring.java      ← 修改：返回步骤列表
├── SortedGreedyColoring.java← 修改：返回步骤列表
└── BacktrackColoring.java   ← 修改：返回步骤列表，重构递归生成 TRY/COMMIT/UNDO
```

## 2. 步骤模型 `ColoringStep`

```java
public class ColoringStep {
    public enum Action { TRY, COMMIT, UNDO }

    public Action action;        // 动作类型
    public int provinceIndex;    // 涉及的省份索引
    public int color;            // 颜色编号（UNDO 时忽略）
    public String description;   // 描述文本，如 "新疆 ← 颜色1"
}
```

| Action | 含义 | 使用场景 |
|--------|------|----------|
| `TRY` | 试探性着色 | 回溯算法尝试某个颜色，可能后续被撤销 |
| `COMMIT` | 确认着色 | 贪心算法每步；回溯找到可行解后批量标记 |
| `UNDO` | 撤销着色 | 回溯回退时，取消之前的 TRY |

## 3. 动画控制器 `ColoringAnimator`

位于 `ui/` 包，负责驱动动画流程：

- **Timeline 驱动**：使用 JavaFX `Timeline`，每 `delayMs` 毫秒触发一帧
- **步骤消费**：从 `List<ColoringStep>` 逐条读取，根据 Action 调用 `MapCanvas.colorProvince()` 或 `MapCanvas.uncolorProvince()`
- **速度控制**：`delayMs` 从 UI 滑块读取，范围 30ms（快）~ 1000ms（慢），默认 300ms
- **播放控制**：`play(steps)` 开始播放，`pause()` / `resume()` 暂停继续，`setSpeed(delayMs)` 调整速度
- **完成回调**：`onFinished` 回调更新统计信息（颜色数、耗时、步骤进度）

### MapCanvas 新增方法

- `colorProvince(int index, int color)` — 给单个省份着色并重绘
- `uncolorProvince(int index)` — 取消单个省份颜色（恢复灰色）并重绘

两个方法复用现有 `drawProvince` 的内部逻辑，仅对单省份做增量更新以保持性能。

## 4. 算法改造

### 4.1 `GreedyColoring`

最简单。按顺序遍历省份，每个省份的着色直接生成一条 `COMMIT` 步骤。步骤数 = 省份数。

### 4.2 `SortedGreedyColoring`

同理。按度数降序排列后，每省一条 `COMMIT`。排序过程不产生步骤。

### 4.3 `BacktrackColoring` — 重点改造

现有递归结构改为逐步骤记录：

```
backtrack(idx, maxColors, currentColors, steps):
    if idx == n:
        将此轮有效 TRY 批量标记为 COMMIT
        return true

    for c in 1..maxColors where c < bestColorCount:
        if isValid(idx, c):
            currentColors[idx] = c
            steps.add(new TRY(idx, c))

            if backtrack(idx + 1, maxColors, ...):
                return true
            else:
                steps.add(new UNDO(idx))       // 回退
                currentColors[idx] = 0
    return false
```

外层循环：从排序贪心上界 k 开始，逐步降低颜色数：
- 成功 → 保留该轮步骤为当前最佳，k--，继续尝试
- 失败 → 还原上一轮步骤，结束搜索

最终 `ColoringResult` 携带最佳解的步骤列表。`timeNanos` 记录的是算法计算时间（不含动画播放时间）。

### 4.4 `ColoringResult` 扩展

新增字段：
- `List<ColoringStep> steps` — 步骤列表（瞬间模式可为 null）

## 5. UI 改造

### 5.1 右侧面板布局

```
┌─────────────────────┐
│   着色算法           │
│  [贪心着色]          │
│  [贪心+排序]         │
│  [回溯求最优]        │
├─────────────────────┤
│  ◉ 瞬间着色          │  ← ToggleButton 组
│  ○ 动画着色           │
├─────────────────────┤
│  动画速度  [━━●━━]   │  ← Slider（仅动画模式启用）
│  快          慢      │
├─────────────────────┤
│   统计信息           │
│  颜色总数：--        │
│  耗时：--            │
│  算法：--            │
│  步骤：--/--         │  ← 动画模式显示进度
├─────────────────────┤
│  [重新加载]          │
└─────────────────────┘
```

### 5.2 交互逻辑

- **瞬间模式**：速度滑块禁用/隐藏，点击算法按钮直接完成着色，行为与现在一致（算法生成步骤但跳过动画，直接应用最终结果）
- **动画模式**：速度滑块启用，点击算法按钮 → 调用算法生成步骤列表 → `ColoringAnimator.play()` → 进度实时显示
- **播放中锁定**：三个算法按钮和重新加载按钮禁用，防重复点击。播放完成后恢复
- **模式切换**：动画播放中不允许切换模式

## 6. 异常处理

- **无数据时点算法** → 弹出警告对话框（复用现有逻辑）
- **动画中切换地图/重新加载** → 自动停止动画，重置画布
- **步骤数过多** → 最大步骤阈值 50000，超出则弹出提示"动画步骤过多，建议使用瞬间模式"
- **速度滑块极值** → 下限取 30ms（JavaFX 60fps 约 16ms/帧，30ms 保证每帧完成渲染）
- **窗口 resize** → 动画播放中 `MapCanvas.layoutChildren` 正常自适应，无需特殊处理

## 7. 测试策略

| 测试对象 | 测试内容 |
|----------|----------|
| `GreedyColoring` | 步骤列表长度 = 省份数，每步均为 COMMIT |
| `SortedGreedyColoring` | 步骤列表长度 = 省份数，每步均为 COMMIT |
| `BacktrackColoring` | 步骤列表非空，COMMIT 数量 ≤ 省份数，每省至多一条 COMMIT，相邻省颜色不冲突 |
| `ColoringAnimator` | 给定固定步骤列表，play() 结束后所有步骤正确执行；pause/resume/setSpeed 行为正确 |
| `MapCanvas` 新增方法 | colorProvince 后该省颜色正确；uncolorProvince 后恢复灰色 |
| 手动验证 | 三种算法在动画/瞬间模式下各运行一次，确认无卡顿、步骤进度显示正确、滑块调速生效 |
