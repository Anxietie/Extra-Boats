package com.mod.mixin.client;

import com.mod.entity.ExtraBoatEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {
    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) { super(world, pos, yaw, gameProfile); }

    @Shadow
    public Input input;
    @Shadow
    private boolean riding;

    @Inject(method = "tickRiding", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getControllingVehicle()Lnet/minecraft/entity/Entity;"), cancellable = true)
    private void tickRiding(CallbackInfo ci) {
        Entity e = getControllingVehicle();
        if (e instanceof ExtraBoatEntity boatEntity) {
            boatEntity.setInputs(input.pressingLeft, input.pressingRight, input.pressingForward, input.pressingBack);
            riding |= input.pressingLeft || input.pressingRight || input.pressingForward || input.pressingBack;
            ci.cancel();
        }
    }
}
