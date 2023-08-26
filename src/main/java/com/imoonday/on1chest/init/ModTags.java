package com.imoonday.on1chest.init;

import com.imoonday.on1chest.OnlyNeedOneChest;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class ModTags {

    public static final TagKey<Block> CONNECT_BLOCK = TagKey.of(RegistryKeys.BLOCK, OnlyNeedOneChest.id("connect_block"));
}
