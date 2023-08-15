package com.imoonday.on1chest;

import com.imoonday.on1chest.init.ModBlocks;
import com.imoonday.on1chest.init.ModItems;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class OnlyNeedOneChest implements ModInitializer {

    /**
     * Bugs:
     * EMI免费制作
     * 数量过多取出卡顿
     * 和部分快捷键冲突
     * <p>
     * Suggestions:
     * 内存块设置(设置：关闭是否连接)
     * 限制箱子上限 默认64
     */

    public static final Identifier C2S = id("c2s");

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        ModScreens.register();
        registerGlobalReceiver();
    }

    private void registerGlobalReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(C2S, (server, player, handler, buf, sender) -> {
            NbtCompound nbt = buf.readUnlimitedNbt();
            server.execute(() -> {
                if (player.currentScreenHandler instanceof IScreenDataReceiver receiver) {
                    receiver.receive(nbt);
                }
            });
        });
    }

    public static Identifier id(String id) {
        return new Identifier("on1chest", id);
    }
}