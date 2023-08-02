package com.mod.mixin;

import com.mod.block.dispenser.ExtraBoatDispenserBehavior;
import com.mod.entity.ExtendedBoatEntityType;
import com.mod.registry.ItemRegister;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBehavior.class)
public interface DispenserBehaviorMixin {
    @Inject(method = "registerDefaults", at = @At("TAIL"))
    private static void registerDefaults(CallbackInfo ci) {
        DispenserBlock.registerBehavior(ItemRegister.CRIMSON_BOAT, new ExtraBoatDispenserBehavior(ExtendedBoatEntityType.CRIMSON));
        DispenserBlock.registerBehavior(ItemRegister.WARPED_BOAT, new ExtraBoatDispenserBehavior(ExtendedBoatEntityType.WARPED));
        DispenserBlock.registerBehavior(ItemRegister.CRIMSON_CHEST_BOAT, new ExtraBoatDispenserBehavior(ExtendedBoatEntityType.CRIMSON, true));
        DispenserBlock.registerBehavior(ItemRegister.WARPED_CHEST_BOAT, new ExtraBoatDispenserBehavior(ExtendedBoatEntityType.WARPED, true));
    }
}
