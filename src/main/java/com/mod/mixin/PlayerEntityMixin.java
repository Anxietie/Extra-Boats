package com.mod.mixin;

import com.mod.entity.ExtraBoatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {
    public PlayerEntityMixin(EntityType<?> type, World world) { super(type, world); }

    @Shadow
    public abstract void increaseStat(Identifier stat, int amount);

    @Inject(method = "increaseRidingMotionStats(DDD)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;getVehicle()Lnet/minecraft/entity/Entity;"), cancellable = true)
    private void increaseRidingMotionStats(double dx, double dy, double dz, CallbackInfo ci) {
        int i;
        if (hasVehicle() && (i = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0f)) > 0) {
            Entity e = getVehicle();
            if (e instanceof ExtraBoatEntity) {
                increaseStat(Stats.BOAT_ONE_CM, i);
                ci.cancel();
            }
        }
    }
}
