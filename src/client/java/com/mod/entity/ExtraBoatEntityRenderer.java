package com.mod.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ModelWithWaterPatch;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

import java.util.Map;

import static com.mod.ExtraBoats.MODID;
import static com.mod.entity.ExtendedBoatEntityType.CRIMSON;
import static com.mod.entity.ExtendedBoatEntityType.WARPED;

@Environment(EnvType.CLIENT)
public class ExtraBoatEntityRenderer extends EntityRenderer<ExtraBoatEntity> {
    private final Map<BoatEntity.Type, Pair<Identifier, CompositeEntityModel<ExtraBoatEntity>>> texturesAndModels;

    public ExtraBoatEntityRenderer(EntityRendererFactory.Context ctx, boolean chest) {
        super(ctx);
        this.shadowRadius = 0.8F;
        this.texturesAndModels = ImmutableMap.of(CRIMSON, Pair.of(new Identifier(MODID, getTexture(CRIMSON, chest)), this.createModel(ctx, CRIMSON, chest)), WARPED, Pair.of(new Identifier(MODID, getTexture(WARPED, chest)), this.createModel(ctx, WARPED, chest)));
    }

    private CompositeEntityModel<ExtraBoatEntity> createModel(EntityRendererFactory.Context ctx, BoatEntity.Type type, boolean chest) {
        EntityModelLayer entityModelLayer = chest ? EntityModelLayers.createChestBoat(type) : EntityModelLayers.createBoat(type);
        ModelPart modelPart = ctx.getPart(entityModelLayer);
        return chest ? new ExtraChestBoatEntityModel(modelPart) : new ExtraBoatEntityModel(modelPart);
    }

    private static String getTexture(BoatEntity.Type type, boolean chest) {
        return chest ? "textures/entity/chest_boat/" + type.getName() + ".png" : "textures/entity/boat/" + type.getName() + ".png";
    }

    public void render(ExtraBoatEntity boatEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0F, 0.375F, 0.0F);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - f));
        float h = (float)boatEntity.getDamageWobbleTicks() - g;
        float j = boatEntity.getDamageWobbleStrength() - g;
        if (j < 0.0F) {
            j = 0.0F;
        }

        if (h > 0.0F) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(h) * h * j / 10.0F * (float)boatEntity.getDamageWobbleSide()));
        }

        float k = boatEntity.interpolateBubbleWobble(g);
        if (!MathHelper.approximatelyEquals(k, 0.0F)) {
            matrixStack.multiply((new Quaternionf()).setAngleAxis(boatEntity.interpolateBubbleWobble(g) * 0.017453292F, 1.0F, 0.0F, 1.0F));
        }

        Pair<Identifier, CompositeEntityModel<ExtraBoatEntity>> pair = this.texturesAndModels.get(boatEntity.getVariant());
        Identifier identifier = pair.getFirst();
        CompositeEntityModel<ExtraBoatEntity> compositeEntityModel = pair.getSecond();
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
        compositeEntityModel.setAngles(boatEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(compositeEntityModel.getLayer(identifier));
        compositeEntityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        if (!boatEntity.isSubmergedInWater()) {
            VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getWaterMask());
            if (compositeEntityModel instanceof ModelWithWaterPatch modelWithWaterPatch)
                modelWithWaterPatch.getWaterPatch().render(matrixStack, vertexConsumer2, i, OverlayTexture.DEFAULT_UV);
        }

        matrixStack.pop();
        super.render(boatEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    public Identifier getTexture(ExtraBoatEntity boatEntity) {
        return this.texturesAndModels.get(boatEntity.getVariant()).getFirst();
    }
}
