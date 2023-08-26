package com.imoonday.on1chest.blocks.entities;

import com.imoonday.on1chest.blocks.StorageRecycleBinBlock;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.screen.StorageRecycleBinScreenHandler;
import com.imoonday.on1chest.utils.ImplementedInventory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StorageRecycleBinBlockEntity extends BlockEntity implements ImplementedInventory, ExtendedScreenHandlerFactory, SidedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private int outputTime;
    private boolean receiving;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return receiving ? 1 : 0;
        }

        @Override
        public void set(int index, int value) {

        }


        @Override
        public int size() {
            return 1;
        }
    };

    public StorageRecycleBinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORAGE_RECYCLE_BIN_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, StorageRecycleBinBlockEntity entity) {
        entity.receiving = world.isReceivingRedstonePower(pos);
        boolean before = state.get(StorageRecycleBinBlock.LIT);
        boolean lit = entity.outputTime-- > 0;
        if (lit != before) {
            world.setBlockState(pos, state.with(StorageRecycleBinBlock.LIT, lit), Block.NOTIFY_LISTENERS);
            world.updateComparators(pos, state.getBlock());
        }
        if (entity.outputTime < 0) {
            entity.outputTime = 0;
        }
        if (entity.getUsedSlots() >= entity.inventory.size() - 1) {
            if (!entity.inventory.get(0).isEmpty()) {
                for (int i = entity.inventory.size() - 2; i >= 0; i--) {
                    if (entity.inventory.get(i + 1).isEmpty()) {
                        entity.inventory.set(i + 1, entity.inventory.get(i).copyAndEmpty());
                    }
                }
            }
            if (entity.getUsedSlots() == entity.inventory.size()) {
                ItemStack stack = entity.inventory.get(entity.inventory.size() - 1);
                if (stack.isEmpty()) {
                    return;
                }
                ItemStack itemStack = stack.copyAndEmpty();
                Direction direction = state.get(StorageRecycleBinBlock.FACING);
                Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
                if (inventory != null) {
                    HopperBlockEntity.transfer(entity, inventory, itemStack, direction.getOpposite());
                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS);
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.2f);
                }
                entity.outputTime = 2;
            }
        }
    }

    public int getUsedSlots() {
        return (int) this.getItems().stream().filter(stack -> !stack.isEmpty()).count();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return this.getCachedState().getBlock().getName();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new StorageRecycleBinScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {

    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        if (nbt.contains("outputTime", NbtElement.INT_TYPE)) {
            this.outputTime = nbt.getInt("outputTime");
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("outputTime", outputTime);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return isNotOutputSide(side) ? new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8} : new int[]{1, 2, 3, 4, 5, 6, 7, 8};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    private boolean isNotOutputSide(Direction side) {
        return this.world == null || this.world.getBlockState(pos).get(StorageRecycleBinBlock.FACING) != side || !world.isReceivingRedstonePower(pos);
    }
}
