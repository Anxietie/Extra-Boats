package com.mod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.TypeSpecificPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(TypeSpecificPredicate.Deserializers.class)
public abstract class TypeSpecificPredicateDeserializersMixin {
    @Inject(method = "method_47838", at = @At("HEAD"), cancellable = true)
    private static void method_47838(Entity entity, CallbackInfoReturnable<Optional> cir) {

    }
}
