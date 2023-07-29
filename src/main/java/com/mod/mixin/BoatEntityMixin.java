package com.mod.mixin;

import com.mod.registry.ItemRegister;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mod.entity.ExtendedBoatEntityType.CRIMSON;
import static com.mod.entity.ExtendedBoatEntityType.WARPED;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {
    @Shadow
    public abstract BoatEntity.Type getVariant();

    @Inject(method = "asItem", at = @At("HEAD"), cancellable = true)
    private void asItem(CallbackInfoReturnable<Item> cir) {
        BoatEntity.Type variant = getVariant();
        if (variant == CRIMSON) cir.setReturnValue(ItemRegister.CRIMSON_BOAT);
        if (variant == WARPED) cir.setReturnValue(ItemRegister.WARPED_BOAT);
    }
}
