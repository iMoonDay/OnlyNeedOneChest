package com.imoonday.on1chest.blocks;

import com.imoonday.on1chest.blocks.entities.QuickCraftingTableBlockEntity;
import com.imoonday.on1chest.init.ModBlockEntities;
import com.imoonday.on1chest.utils.ConnectBlock;
import com.imoonday.on1chest.utils.CraftingRecipeTreeManager;
import com.imoonday.on1chest.utils.MultiInventory;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuickCraftingTableBlock extends BlockWithEntity implements ConnectBlock {

    public QuickCraftingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient || hand == Hand.OFF_HAND) {
            return ActionResult.SUCCESS;
        }
//        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        if (world.getBlockEntity(pos) instanceof QuickCraftingTableBlockEntity entity) {
            List<Inventory> inventories = ConnectBlock.getConnectedBlocks(world, pos).stream().filter(pair -> pair.getLeft().getBlockEntity(pair.getRight()) instanceof Inventory).map(pair -> ((Inventory) pair.getLeft().getBlockEntity(pair.getRight()))).toList();
            MultiInventory multiInventory = new MultiInventory(inventories);
            int i = 0;
            CraftingRecipeTreeManager.CraftResult craftResult;
            Set<ItemStack> except = ItemStackSet.create();
            while (i++ < 10) {
                ItemStack itemStack = player.getStackInHand(hand);
                craftResult = entity.getRecorder().getCraftResult(multiInventory, itemStack, except);
                if (craftResult.isCrafted()) {
                    except.addAll(craftResult.getCost());
                    Text cost = Texts.join(craftResult.getCost(), stack -> stack.getName().copy().append("(%d)".formatted(stack.getCount())).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack)))));
                    if (i == 1) {
                        player.sendMessage(Text.empty());
                        player.sendMessage(itemStack.getName());
                    }
                    player.sendMessage(Text.literal("配方" + i + ":"));
                    player.sendMessage(Text.literal("消耗: ").append(cost));
                    Set<ItemStack> remainder = craftResult.getRemainder();
                    if (!remainder.isEmpty()) {
                        Text text = Texts.join(remainder, stack -> stack.getName().copy().append("(%d)".formatted(stack.getCount())).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack)))));
                        player.sendMessage(Text.literal("剩余: ").append(text));
                    }
                } else if (i == 1) {
                    boolean missing = craftResult.isMissing();
                    if (!missing) {
                        player.sendMessage(Text.literal("无可用配方"), true);
                    }
                    if (missing) {
                        Set<Map<Ingredient, Integer>> maps = craftResult.getMissing();
                        int count = 0;
                        for (Map<Ingredient, Integer> map : maps) {
                            Text text = Texts.join(map.entrySet(), entry -> entry.getKey().getMatchingStacks()[0].getName().copy().append("(" + entry.getValue() + ")").styled(style -> entry.getKey().getMatchingStacks().length < 2 ? style : style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("可选：").append(Texts.join(Arrays.stream(entry.getKey().getMatchingStacks()).skip(1).toList(), ItemStack::getName))))));
                            player.sendMessage(Text.empty());
                            player.sendMessage(Text.literal("缺少: "));
                            player.sendMessage(Text.literal("配方%d: ".formatted(++count)).append(text));
                        }
                    }
                    break;
                }
            }
        }
        return ActionResult.CONSUME;
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new CraftingScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), this.getName());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new QuickCraftingTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, ModBlockEntities.QUICK_CRAFTING_TABLE_BLOCK_ENTITY, QuickCraftingTableBlockEntity::tick);
    }
}
