package com.mod.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static com.mod.ExtraBoats.MODID;

public class SoundRegister {
    public static SoundEvent ENTITY_BOAT_PADDLE_LAVA = registerSound("entity_boat_paddle_lava");

    public static void registerSounds() {}

    private static SoundEvent registerSound(String id) {
        Identifier identifier = new Identifier(MODID, id);
        SoundEvent soundEvent = SoundEvent.of(identifier);
        return Registry.register(Registries.SOUND_EVENT, identifier, soundEvent);
    }
}
