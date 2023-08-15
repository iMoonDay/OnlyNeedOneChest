package com.imoonday.on1chest.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.IndexedIterable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {

    @Shadow
    public abstract ByteBuf writeBoolean(boolean value);

    @Shadow
    public abstract boolean readBoolean();

    @Shadow
    public abstract int readInt();

    @Shadow
    @Nullable
    public abstract <T> T readRegistryValue(IndexedIterable<T> registry);

    @Shadow
    public abstract @Nullable NbtCompound readNbt();

    @Redirect(method = "writeItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeByte(I)Lio/netty/buffer/ByteBuf;"))
    public ByteBuf writeByte(PacketByteBuf instance, int value) {
        return value > Byte.MAX_VALUE ? instance.writeInt(value) : instance.writeByte(value);
    }

    @Inject(method = "writeItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;Ljava/lang/Object;)V", shift = At.Shift.BEFORE))
    public void writeByte(ItemStack stack, CallbackInfoReturnable<PacketByteBuf> cir) {
        this.writeBoolean(stack.getCount() > Byte.MAX_VALUE);
    }

    @Inject(method = "readItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;", shift = At.Shift.BEFORE), cancellable = true)
    public void writeByte(CallbackInfoReturnable<ItemStack> cir) {
        if (this.readBoolean()) {
            Item item = this.readRegistryValue(Registries.ITEM);
            int count = this.readInt();
            ItemStack itemStack = new ItemStack(item, count);
            itemStack.setNbt(this.readNbt());
            cir.setReturnValue(itemStack);
        }
    }

}
