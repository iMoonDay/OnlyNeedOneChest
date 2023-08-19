package com.imoonday.on1chest.items;

import com.imoonday.on1chest.blocks.StorageAccessorBlock;
import com.imoonday.on1chest.blocks.entities.StorageAccessorBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RemoteAccessorItem extends Item {

    private final boolean crossDimensional;

    public RemoteAccessorItem(Settings settings, boolean crossDimensional) {
        super(settings);
        this.crossDimensional = crossDimensional;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("world", NbtElement.STRING_TYPE) && nbt.contains("x", NbtElement.INT_TYPE) && nbt.contains("y", NbtElement.INT_TYPE) && nbt.contains("z", NbtElement.INT_TYPE)) {
            String world1 = nbt.getString("world");
            if (!this.crossDimensional && world != null && !world.getRegistryKey().getValue().toString().equals(world1)) {
                tooltip.add(Text.translatable("item.on1chest.remote_accessor.tooltip.warn").formatted(Formatting.RED, Formatting.BOLD));
            }
            Identifier identifier = Identifier.tryParse(world1);
            if (identifier != null) {
                world1 = identifier.getPath();
            }
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            tooltip.add(Text.translatable("item.on1chest.remote_accessor.tooltip.pos", world1, x, y, z).formatted(Formatting.GRAY));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return hasValidPos(stack);
    }

    public boolean hasValidPos(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains("world", NbtElement.STRING_TYPE) && nbt.contains("x", NbtElement.INT_TYPE) && nbt.contains("y", NbtElement.INT_TYPE) && nbt.contains("z", NbtElement.INT_TYPE);
    }

    @Nullable
    public BlockPos getPos(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("world", NbtElement.STRING_TYPE) && nbt.contains("x", NbtElement.INT_TYPE) && nbt.contains("y", NbtElement.INT_TYPE) && nbt.contains("z", NbtElement.INT_TYPE)) {
            return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
        }
        return null;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ActionResult pass = tryUseBlock(stack, world, user);
        return pass == ActionResult.SUCCESS ? TypedActionResult.success(stack) : TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResult.FAIL;
        }
        ItemStack stack = context.getStack();
        BlockPos blockPos = context.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof StorageAccessorBlock && blockEntity instanceof StorageAccessorBlockEntity entity) {
            NbtCompound nbt = stack.getNbt();
            if (world.isClient || !player.isSneaking()) {
                return ActionResult.SUCCESS;
            }
            BlockPos pos = entity.getPos();
            int posX = pos.getX();
            int posY = pos.getY();
            int posZ = pos.getZ();
            if (nbt != null && nbt.contains("world", NbtElement.STRING_TYPE) && nbt.contains("x", NbtElement.INT_TYPE) && nbt.contains("y", NbtElement.INT_TYPE) && nbt.contains("z", NbtElement.INT_TYPE)) {
                String world1 = nbt.getString("world");
                int x = nbt.getInt("x");
                int y = nbt.getInt("y");
                int z = nbt.getInt("z");
                if (world.getRegistryKey().getValue().toString().equals(world1) && posX == x && posY == y && posZ == z) {
                    player.sendMessage(Text.translatable("item.on1chest.remote_accessor.tooltip.recorded"), true);
                    return ActionResult.SUCCESS;
                }
            }
            stack.getOrCreateNbt().putString("world", world.getRegistryKey().getValue().toString());
            stack.getOrCreateNbt().putInt("x", posX);
            stack.getOrCreateNbt().putInt("y", posY);
            stack.getOrCreateNbt().putInt("z", posZ);
            player.sendMessage(Text.translatable("item.on1chest.remote_accessor.tooltip.record", posX, posY, posZ), true);
        } else {
            if (player.isSneaking()) {
                return ActionResult.PASS;
            }
            return tryUseBlock(stack, world, player);
        }
        return ActionResult.CONSUME;
    }

    private ActionResult tryUseBlock(ItemStack stack, World world, PlayerEntity player) {
        if (world.isClient) {
            return ActionResult.PASS;
        }
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return ActionResult.PASS;
        }
        if (nbt.contains("world", NbtElement.STRING_TYPE) && nbt.contains("x", NbtElement.INT_TYPE) && nbt.contains("y", NbtElement.INT_TYPE) && nbt.contains("z", NbtElement.INT_TYPE)) {
            String world1 = nbt.getString("world");
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            RegistryKey<World> registryKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(world1));
            if (registryKey != null) {
                ServerWorld serverWorld = ((ServerWorld) world).getServer().getWorld(registryKey);
                if (serverWorld != null && (this.crossDimensional || world == serverWorld)) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (serverWorld.getBlockEntity(blockPos) instanceof StorageAccessorBlockEntity entity) {
                        player.openHandledScreen(entity);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        player.sendMessage(Text.translatable("item.on1chest.remote_accessor.tooltip.abnormal"), true);
        return ActionResult.CONSUME;
    }
}
