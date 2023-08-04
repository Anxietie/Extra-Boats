package com.mod.mixin;

import com.mod.entity.ExtraBoatEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract Entity getVehicle();

    @Shadow
    private int fireTicks;

    @Inject(method = "setOnFireFromLava", at = @At("HEAD"), cancellable = true)
    private void setOnFireFromLava(CallbackInfo ci) {
        Entity vehicle = getVehicle();
        if (vehicle instanceof ExtraBoatEntity) {
            if (fireTicks > 0 && fireTicks % 20 == 0)
                ((Entity)(Object)this).damage(((Entity)(Object)this).getDamageSources().onFire(), 1.0f);
            ci.cancel();
        }
    }
}
