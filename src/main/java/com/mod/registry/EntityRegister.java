package com.mod.registry;

import com.mod.entity.ExtraBoatEntity;
import com.mod.entity.ExtraChestBoatEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.mod.ExtraBoats.MODID;

public class EntityRegister {
    public static final EntityType<ExtraBoatEntity> EXTRA_BOAT = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MODID, "extra_boat"),
            FabricEntityTypeBuilder.<ExtraBoatEntity>create(SpawnGroup.MISC, ExtraBoatEntity::new)
                    .dimensions(EntityDimensions.fixed(1.375f, 0.5625f))
                    .trackRangeBlocks(10)
                    .fireImmune()
                    .build()
    );

    public static final EntityType<ExtraChestBoatEntity> EXTRA_CHEST_BOAT = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(MODID, "extra_chest_boat"),
            FabricEntityTypeBuilder.<ExtraChestBoatEntity>create(SpawnGroup.MISC, ExtraChestBoatEntity::new)
                    .dimensions(EntityDimensions.fixed(1.375f, 0.5625f))
                    .trackRangeBlocks(10)
                    .fireImmune()
                    .build()
    );

    public static void registerEntities() {};
}
