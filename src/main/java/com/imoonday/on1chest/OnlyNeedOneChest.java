package com.imoonday.on1chest;

import com.imoonday.on1chest.init.*;
import com.imoonday.on1chest.utils.IScreenDataReceiver;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class OnlyNeedOneChest implements ModInitializer {

    /**
     * BUG:
     * TODO:
     * 兼容REI,JEI,EMI等自动补全配方
     * 容器识别器：识别后方的容器方块并让访问器正常使用
     * 漏斗线缆：吸收上方一格内的掉落物并存入最近的内存块中
     */

    public static final Identifier C2S = id("c2s");

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModScreens.register();
        ModGameRules.register();
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