package com.mod.mixin;

import com.mod.entity.ExtraBoatEntity;
import com.mod.entity.ExtraChestBoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.mod.entity.ExtendedBoatEntityType.CRIMSON;
import static com.mod.entity.ExtendedBoatEntityType.WARPED;

@Mixin(BoatItem.class)
public abstract class BoatItemMixin extends Item {
    public BoatItemMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    private @Final BoatEntity.Type type;

    @Shadow
    private @Final boolean chest;

    @Unique
    private ExtraBoatEntity createEntity(World world, HitResult result) {
        if (chest)
            return new ExtraChestBoatEntity(world, result.getPos().x, result.getPos().y, result.getPos().z);
        return new ExtraBoatEntity(world, result.getPos().x, result.getPos().y, result.getPos().z);
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BoatItem;createEntity(Lnet/minecraft/world/World;Lnet/minecraft/util/hit/HitResult;)Lnet/minecraft/entity/vehicle/BoatEntity;"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (type == CRIMSON || type == WARPED) {
            BlockHitResult hitResult = BoatItem.raycast(world, user, RaycastContext.FluidHandling.ANY);
            ItemStack itemStack = user.getStackInHand(hand);
            ExtraBoatEntity boatEntity = this.createEntity(world, hitResult);
            boatEntity.setVariant(type);
            boatEntity.setYaw(user.getYaw());
            if (!world.isSpaceEmpty(boatEntity, boatEntity.getBoundingBox()))
                cir.setReturnValue(TypedActionResult.fail(itemStack));
            if (!world.isClient) {
                world.spawnEntity(boatEntity);
                world.emitGameEvent(user, GameEvent.ENTITY_PLACE, hitResult.getPos());
                if (!user.getAbilities().creativeMode)
                    itemStack.decrement(1);
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            cir.setReturnValue(TypedActionResult.success(itemStack, world.isClient()));
        }
    }

    /*
    @Inject(method = "createEntity", at = @At("RETURN"), cancellable = true)
    private void createEntity(World world, HitResult hitResult, CallbackInfoReturnable<BoatEntity> cir) {
        if (type == CRIMSON || type == WARPED) {
            cir.setReturnValue(chest ? new ExtraChestBoatEntity(world, hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z) : new ExtraBoatEntity(world, hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z));
        }
    }
     */
}
