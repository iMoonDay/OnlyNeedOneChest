# Only Need One Chest

**[中文](README.md)** | **Requires Cloth Config API**

## Mod Introduction

This mod includes various storage blocks with different capacities, which can be used in conjunction with accessors to
access all chests within a single interface. It supports custom display settings, filtering, search in Chinese or
Pinyin, and more.

![logo](https://i.postimg.cc/tJFXFPL9/logo.png)

[Video Demonstration](https://www.bilibili.com/video/av830034396/)

## Content Overview

### Storage Memory Blocks

- **Materials**: Wood, Copper, Iron, Gold, Diamond, Nether Alloy, Obsidian, Glass
- **Capacity**: Starting at 27 slots for wood, each material increases capacity by 27 slots, up to 162 slots for Nether
  Alloy.
- **Special Materials**:
    - Obsidian: 135 slots, explosion-proof.
    - Glass: 27 slots, displays stored items in real-time.

### Storage Accessor Block/Storage Processor Block

- **Storage Access Block**: Directly accesses all connected storage memory blocks for normal storage operations.
- **Storage Processing Block**: Allows item crafting while accessing storage.

### Blank Storage Block

- Recognized and connected by accessor blocks but has no storage function.

### Expansion Modules

- Corresponding expansion modules for each material (excluding non-upgradable materials).
- Directly increases the capacity of storage memory blocks, consuming one per use.

### Basic/Advanced Remote Accessors

- **Basic**: Accesses bound coordinates of storage accessor/processor blocks within the same dimension.
- **Advanced**: Adds cross-dimension access to the basic remote accessor's features.

### Recycle Bin

- Nine-slot storage space that automatically clears the last item when full.
- When clearing, items are prioritized to be stored in a container in front if not full; otherwise, they disappear.

### Connection Cables

- Connects two non-contiguous connection blocks (all blocks within the mod are connection blocks).

![v0-2](https://i.postimg.cc/nhnGC5KC/v0-2.png)

### Item Exporter

- Attaches to any container and transfers one stack of items to the nearest memory block every 5 ticks until empty.
- Right-click to specify item type; sneak-right-click to clear or switch matching mode.
- Matching mode only transfers existing item types in memory blocks.

### Memory Extractor

- Opposite function to the item exporter; extracts one stack of items from memory blocks into a container.
- Can specify item type and switch matching modes.

### Wireless Connector

- Placed on connection blocks; remotely connects two locations' memory blocks etc., through ports without distance or
  dimension limits.

### Conversion Module

- Sneak-right-click on a chest to convert it into a wooden storage memory block; items are transferred along with it.

### Recipe Processor Block/Recipe Record Card

- Put the recipe record card into the recipe processor block, put the materials of a recipe into the crafting slots, and
  then take out the recipe record card to save a recipe into the recipe record card.
- The recipe record card can be stored in the recipe processor block, and the recipe processor block will automatically
  try to craft the recipe (requires redstone signal activation).
- Automatic input and output can be achieved through the cooperation of **Item Exporter** and **Memory Extractor**.

### Compressed Storage Memory Block

- Increase compression level by crafting two or more compressed storage memory blocks together; each level adds 27 slots
  of capacity.

### Compression Upgrade Module

- Used to increase the compression level of compressed storage memory blocks.

### Quick Crafting Table (Experimental)

- Connects to any storage block for automatic one-click crafting of selected items; supports tracing raw materials.

### Memory Converter

- Allows any container to be accessed by accessors, such as chests, furnaces, shulker boxes, etc.

## Game Rules

- **maxMemoryRange**: The maximum number of memory blocks an accessor can access simultaneously.
