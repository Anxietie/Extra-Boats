package com.mod.mixin;

import com.mod.registry.ItemRegister;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mod.entity.ExtendedBoatEntityType.CRIMSON;
import static com.mod.entity.ExtendedBoatEntityType.WARPED;

@Mixin(ChestBoatEntity.class)
public abstract class ChestBoatEntityMixin extends BoatEntity {
    public ChestBoatEntityMixin(EntityType<? extends BoatEntity> entityType, World world) { super(entityType, world); }
    protected ChestBoatEntityMixin(World world, double x, double y, double z) { super(world, x, y, z); }

    @Inject(method = "asItem", at = @At("HEAD"), cancellable = true)
    private void asItem(CallbackInfoReturnable<Item> cir) {
        BoatEntity.Type variant = getVariant();
        if (variant == CRIMSON) cir.setReturnValue(ItemRegister.CRIMSON_CHEST_BOAT);
        if (variant == WARPED) cir.setReturnValue(ItemRegister.WARPED_CHEST_BOAT);
    }
}
