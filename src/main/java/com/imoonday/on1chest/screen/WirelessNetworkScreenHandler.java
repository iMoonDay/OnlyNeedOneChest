package com.imoonday.on1chest.screen;

import com.imoonday.on1chest.api.IScreenDataReceiver;
import com.imoonday.on1chest.blocks.entities.WirelessConnectorBlockEntity;
import com.imoonday.on1chest.init.ModScreens;
import com.imoonday.on1chest.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class WirelessNetworkScreenHandler extends ScreenHandler implements IScreenDataReceiver {

    private final WirelessConnectorBlockEntity network;
    private final PlayerInventory inventory;

    public WirelessNetworkScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, null);
    }

    public WirelessNetworkScreenHandler(int syncId, PlayerInventory playerInventory, WirelessConnectorBlockEntity network) {
        super(ModScreens.WIRELESS_NETWORK_SCREEN_HANDLER, syncId);
        this.inventory = playerInventory;
        this.network = network;
        int j;
        int i;
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 45 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 103));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < 27) {
                if (!this.insertItem(originalStack, 27, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }


    @Override
    public void receive(NbtCompound nbt) {
        if (nbt.contains("network", NbtElement.STRING_TYPE)) {
            network.setNetwork(nbt.getString("network"));
        }
        if (nbt.contains("update")) {
            NetworkHandler.sendToClient(inventory.player, "network", NbtString.of(network.getNetwork()));
        }
    }
}
