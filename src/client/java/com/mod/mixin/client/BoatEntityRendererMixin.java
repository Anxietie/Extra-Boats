package com.mod.mixin.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.*;
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

@Mixin(BoatEntityRenderer.class)
public abstract class BoatEntityRendererMixin {
    /*
    @Shadow
    @Mutable
    private @Final Map<BoatEntity.Type, Pair<Identifier, CompositeEntityModel<BoatEntity>>> texturesAndModels;
     */

    @Unique
    private static String getTexture(BoatEntity.Type type, boolean chest) {
        return chest ? "textures/entity/chest_boat/" + type.getName() + ".png" : "textures/entity/boat/" + type.getName() + ".png";
    }

    @Unique
    private static CompositeEntityModel<BoatEntity> createModel(EntityRendererFactory.Context ctx, BoatEntity.Type type, boolean chest) {
        EntityModelLayer entityModelLayer = chest ? EntityModelLayers.createChestBoat(type) : EntityModelLayers.createBoat(type);
        ModelPart modelPart = ctx.getPart(entityModelLayer);
        if (type == BoatEntity.Type.BAMBOO)
            return chest ? new ChestRaftEntityModel(modelPart) : new RaftEntityModel(modelPart);
        return chest ? new ChestBoatEntityModel(modelPart) : new BoatEntityModel(modelPart);
    }

    @Inject(method = "method_32163", at = @At("HEAD"), cancellable = true)
    private void method_32163(boolean chest, EntityRendererFactory.Context ctx, BoatEntity.Type type, CallbackInfoReturnable<Pair> cir) {
        if (type == CRIMSON || type == WARPED)
            cir.setReturnValue(Pair.of(new Identifier(MODID, getTexture(type, chest)), createModel(ctx, type, chest)));
    }

    /*
    @Inject(method = "render(Lnet/minecraft/entity/vehicle/BoatEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void render(BoatEntity boatEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Pair<Identifier, CompositeEntityModel<BoatEntity>> currentPair = texturesAndModels.get(CRIMSON);
        Pair<Identifier, CompositeEntityModel<BoatEntity>> newPair = new Pair<>(new Identifier(MODID, currentPair.getFirst().getPath()), currentPair.getSecond());
        Map<BoatEntity.Type, Pair<Identifier, CompositeEntityModel<BoatEntity>>> t;
        for (Map.Entry<BoatEntity.Type, Pair<Identifier, CompositeEntityModel<BoatEntity>>> e : texturesAndModels.entrySet()) {

        }
    }
    */
}
