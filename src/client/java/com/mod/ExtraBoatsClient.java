package com.mod;

import com.mod.registry.RendererRegistry;
import net.fabricmc.api.ClientModInitializer;

public class ExtraBoatsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		RendererRegistry.registerRenderers();
	}
}