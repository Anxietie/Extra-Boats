package com.mod.registry;

import com.mod.entity.ExtraBoatEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RendererRegistry {
    public static void registerRenderers() {
        EntityRendererRegistry.register(EntityRegister.EXTRA_BOAT, context -> new ExtraBoatEntityRenderer(context, false));
        EntityRendererRegistry.register(EntityRegister.EXTRA_CHEST_BOAT, context -> new ExtraBoatEntityRenderer(context, true));
    }
}
