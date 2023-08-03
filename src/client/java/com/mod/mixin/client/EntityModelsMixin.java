package com.mod.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.mod.entity.ExtraBoatEntityModel;
import com.mod.entity.ExtraChestBoatEntityModel;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.mod.entity.ExtendedBoatEntityType.CRIMSON;
import static com.mod.entity.ExtendedBoatEntityType.WARPED;

@Mixin(EntityModels.class)
public abstract class EntityModelsMixin {
    /*
    @ModifyVariable(method = "getModels", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", remap = false))
    private static ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builderModifier(ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder) {
        TexturedModelData boatTexturedModelData = ExtraBoatEntityModel.getTexturedModelData();
        TexturedModelData chestBoatTexturedModelData = ExtraChestBoatEntityModel.getTexturedModelData();
        builder.put(EntityModelLayers.createBoat(CRIMSON), boatTexturedModelData);
        builder.put(EntityModelLayers.createBoat(WARPED), boatTexturedModelData);
        builder.put(EntityModelLayers.createChestBoat(CRIMSON), chestBoatTexturedModelData);
        builder.put(EntityModelLayers.createChestBoat(WARPED), chestBoatTexturedModelData);
        return builder;
    }
     */
}
