package com.mod.mixin;

import com.mod.entity.ExtraBoatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    protected boolean touchingWater;

    @Shadow
    public abstract Entity getVehicle();

    @Inject(method = "checkWaterState", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/Entity;getVehicle()Lnet/minecraft/entity/Entity;"), cancellable = true)
    private void checkWaterState(CallbackInfo ci) {
        Entity e = getVehicle();
        if (e instanceof ExtraBoatEntity && !e.isSubmergedInWater()) {
            touchingWater = false;
            ci.cancel();
        }
    }
}
