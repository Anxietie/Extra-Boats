package com.mod.entity;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.BoatEntityModel;

@Environment(EnvType.CLIENT)
public class ExtraChestBoatEntityModel extends ExtraBoatEntityModel {
    private static final String CHEST_BOTTOM = "chest_bottom";
    private static final String CHEST_LID = "chest_lid";
    private static final String CHEST_LOCK = "chest_lock";

    public ExtraChestBoatEntityModel(ModelPart modelPart) {
        super(modelPart);
    }

    protected ImmutableList.Builder<ModelPart> getParts(ModelPart root) {
        ImmutableList.Builder<ModelPart> builder = super.getParts(root);
        builder.add(root.getChild(CHEST_BOTTOM));
        builder.add(root.getChild(CHEST_LID));
        builder.add(root.getChild(CHEST_LOCK));
        return builder;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ExtraBoatEntityModel.addParts(modelPartData);
        modelPartData.addChild(CHEST_BOTTOM, ModelPartBuilder.create().uv(0, 76).cuboid(0.0F, 0.0F, 0.0F, 12.0F, 8.0F, 12.0F), ModelTransform.of(-2.0F, -5.0F, -6.0F, 0.0F, -1.5707964F, 0.0F));
        modelPartData.addChild(CHEST_LID, ModelPartBuilder.create().uv(0, 59).cuboid(0.0F, 0.0F, 0.0F, 12.0F, 4.0F, 12.0F), ModelTransform.of(-2.0F, -9.0F, -6.0F, 0.0F, -1.5707964F, 0.0F));
        modelPartData.addChild(CHEST_LOCK, ModelPartBuilder.create().uv(0, 59).cuboid(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F), ModelTransform.of(-1.0F, -6.0F, -1.0F, 0.0F, -1.5707964F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }
}
