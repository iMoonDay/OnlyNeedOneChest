# 唯一箱子

**[English](README-en_us.md)** | **需要Cloth Config API作为前置(可选)**

## 模组简介

此模组包含多种不同容量的存储方块，配合访问器使用，可在一个界面里访问所有箱子。支持自定义显示设置、过滤、中文或拼音搜索等功能。

![logo](https://i.postimg.cc/tJFXFPL9/logo.png)

[视频展示](https://www.bilibili.com/video/av830034396/)

## 内容介绍

### 存储内存块

- **材质**: 木、铜、铁、金、钻石、下界合金、黑曜石、玻璃
- **容量**: 木质27格起，每增一种材料容量+27格，最高为下界合金162格。
- **特殊材质**:
    - 黑曜石135格，防爆。
    - 玻璃27格，可实时展示存储物品。
  - 展示999格，可大量存储同一类型物品并实时展示。

### 存储访问块/存储处理块

- **存储访问块**: 直接访问所有连接的存储内存块，进行存储操作。
- **存储处理块**: 在访问的同时进行合成物品。

### 存储空白块

- 被访问块正常识别连接，但无存储功能。

### 扩容模块

- 对应各种材料的扩容模块（不可升级材料除外）。
- 直接提升存储内存块容量，每次消耗一个。

### 基础/高级远程访问器

- **基础**: 同一维度任意位置访问绑定坐标的存储访问/操作块。
- **高级**: 基础功能上支持跨维度访问。

### 回收站

- 九格存储空间，满时自动清除最后一格物品。
- 清除时优先存入面前容器，否则消失。

### 连接线缆

- 连接两处不相连的连接方块（模组内所有方块为连接方块）。

![v0-2](https://i.postimg.cc/nhnGC5KC/v0-2.png)

### 物品导出器

- 贴在容器上，每5tick向内存块传输1组物品直至空。
- 右击指定物品种类，潜行右击清除或切换匹配模式。
- 匹配模式仅传输内存块已有物品。

### 内存提取器

- 功能与物品导出器相反，从内存块提取物品进容器。
- 可指定物品种类和匹配模式。

### 无线连接器

- 放置在连接方块上，通过端口远程连接两处内存块等，无距离和维度限制。

### 转换模块

- 潜行右击箱子转换成木质存储内存块，物品一同转移。

### 配方处理块/配方记录卡

- 将配方记录卡放进配方处理块中，并将一个配方的材料放入合成区域，然后将配方记录卡取出，即可将一个配方存进配方记录卡中。
- 配方记录卡可以存放在配方处理块中，此时配方处理块会自动尝试合成该配方(需要红石信号激活)。
- 可以通过**物品导出器**和**内存提取器**配合来实现自动输入输出。

### 压缩存储内存块

- 无序合成提高压缩等级，每级增加27格容量。

### 压缩升级模块

- 提高压缩存储内存块压缩等级。

### 快捷合成台(实验性)

- 连接任意存储块，自动一键合成选定物品，支持追溯源材料。

### 内存转换器

- 使任意容器能被访问器访问，如箱子、熔炉、潜影盒等。

## 游戏规则

- **maxMemoryRange**: 访问器最大同时访问内存块数量。
