package com.mod.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SummonCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SummonCommand.class)
public abstract class SummonCommandMixin {
    @Shadow
    private static @Final SimpleCommandExceptionType FAILED_EXCEPTION;
    @Shadow
    private static @Final SimpleCommandExceptionType FAILED_UUID_EXCEPTION;

    @Inject(method = "summon", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;copy()Lnet/minecraft/nbt/NbtCompound;"), cancellable = true)
    private static void summon(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType, Vec3d pos, NbtCompound nbt, boolean initialize, CallbackInfoReturnable<Entity> cir) throws CommandSyntaxException {
        String entityTypeValue = entityType.registryKey().getValue().toString();
        // not a boat
        if (!entityTypeValue.equals("minecraft:boat") && !entityTypeValue.equals("minecraft:chest_boat"))
            return;

        // doesnt have type nbt
        String boatEntityType = nbt.contains("Type") ? nbt.get("Type").asString() : null;
        if (boatEntityType == null)
            return;

        // toString() returns the same as asString() but with an extra set of quotes
        // ex: asString() -> "oak"       toString() -> ""oak""
        // isnt of type crimson or warped
        if (!boatEntityType.equals("crimson") && !boatEntityType.equals("warped"))
            return;

        NbtCompound nbtCompound = nbt.copy();
        nbtCompound.putString("id", entityTypeValue.equals("minecraft:boat") ? "anx:extra_boat" : "anx:extra_chest_boat");
        ServerWorld serverWorld = source.getWorld();
        Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, serverWorld, (entityx) -> {
            entityx.refreshPositionAndAngles(pos.x, pos.y, pos.z, entityx.getYaw(), entityx.getPitch());
            return entityx;
        });
        if (entity == null)
            throw FAILED_EXCEPTION.create();
        else {
            if (initialize && entity instanceof MobEntity)
                ((MobEntity)entity).initialize(source.getWorld(), source.getWorld().getLocalDifficulty(entity.getBlockPos()), SpawnReason.COMMAND, null, null);
            if (!serverWorld.spawnNewEntityAndPassengers(entity))
                throw FAILED_UUID_EXCEPTION.create();
            else
                cir.setReturnValue(entity);
        }
    }

    @Inject(method = "execute", at = @At("HEAD"))
    private static void execute(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType, Vec3d pos, NbtCompound nbt, boolean initialize, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        String entityTypeValue = entityType.registryKey().getValue().toString();
        if (entityTypeValue.equals("anx:extra_boat") || entityTypeValue.equals("anx:extra_chest_boat"))
            throw FAILED_EXCEPTION.create();
    }
}
