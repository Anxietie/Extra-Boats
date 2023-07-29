package com.mod.mixin.client;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mod.ExampleMod.MODID;
import static com.mod.entity.ExtendedBoatEntityType.CRIMSON;
import static com.mod.entity.ExtendedBoatEntityType.WARPED;

@Mixin(EntityModelLayers.class)
public abstract class EntityModelLayersMixin {
    @Unique
    private static EntityModelLayer create(String id, String layer) {
        return new EntityModelLayer(new Identifier(MODID, id), layer);
    }

    @Inject(method = "createBoat", at = @At("TAIL"), cancellable = true)
    private static void createBoat(BoatEntity.Type type, CallbackInfoReturnable<EntityModelLayer> cir) {
        if (type == CRIMSON || type == WARPED) {
            cir.setReturnValue(create("boat/" + type.getName(), "main"));
        }
    }

    @Inject(method = "createBoat", at = @At("TAIL"), cancellable = true)
    private static void createChestBoat(BoatEntity.Type type, CallbackInfoReturnable<EntityModelLayer> cir) {
        if (type == CRIMSON || type == WARPED) {
            cir.setReturnValue(create("chest_boat/" + type.getName(), "main"));
        }
    }
}