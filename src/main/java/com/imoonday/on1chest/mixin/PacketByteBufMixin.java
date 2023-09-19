package com.imoonday.on1chest.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {

    @Shadow
    public abstract ByteBuf writeInt(int value);

    @Shadow
    public abstract int readInt();

    @Inject(method = "writeItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/network/PacketByteBuf;", shift = At.Shift.AFTER))
    public void writeItemStack(ItemStack stack, CallbackInfoReturnable<PacketByteBuf> cir) {
        this.writeInt(stack.getCount());
    }

    @Inject(method = "readItemStack", at = @At("RETURN"), cancellable = true)
    public void readItemStack(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(cir.getReturnValue().copyWithCount(this.readInt()));
    }
}
