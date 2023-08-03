package com.mod.mixin.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.fluid.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(RenderLayers.class)
public abstract class RenderLayersMixin {
    @Inject(method = "method_23681", at = @At("TAIL"))
    private static void method_23681(HashMap map, CallbackInfo ci) {
        RenderLayer renderLayer = RenderLayer.getTranslucent();
        map.put(Fluids.LAVA, renderLayer);
        map.put(Fluids.FLOWING_LAVA, renderLayer);
    }
}
