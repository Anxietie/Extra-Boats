package com.mod.mixin;

import com.mod.entity.ExtendedBoatEntityType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.StringIdentifiable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(BoatEntity.Type.class)
public abstract class BoatEntityTypeMixin {
	@SuppressWarnings("ShadowTarget")
	@Shadow
	@Mutable
	private static @Final BoatEntity.Type[] field_7724;

	@Shadow
	@Mutable
	public static @Final StringIdentifiable.Codec<BoatEntity.Type> CODEC;

	@Unique
	private static final List<BoatEntity.Type> $TYPES = new ArrayList<>();

	@Invoker("<init>")
	private static BoatEntity.Type example$newType(String internalName, int internalId, Block baseBlock, String name) {
		throw new AssertionError();
	}

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void example$addType(CallbackInfo ci) {
		List<BoatEntity.Type> types = new ArrayList<>(Arrays.asList(field_7724));
		int ordinal = types.get(types.size() - 1).ordinal();

		ExtendedBoatEntityType.CRIMSON = example$initType("CRIMSON", ++ordinal, Blocks.CRIMSON_PLANKS, "crimson");
		ExtendedBoatEntityType.WARPED = example$initType("WARPED", ++ordinal, Blocks.WARPED_PLANKS, "warped");

		types.addAll($TYPES);
		field_7724 = types.toArray(new BoatEntity.Type[0]);
		CODEC = StringIdentifiable.createCodec(BoatEntity.Type::values);
	}

	@Unique
	private static BoatEntity.Type example$initType(String internalName, int internalId, Block baseBlock, String name) {
		$TYPES.add(example$newType(internalName, internalId, baseBlock, name));
		return $TYPES.get($TYPES.size() - 1);
	}

	@Inject(method = "getType(I)Lnet/minecraft/entity/vehicle/BoatEntity$Type;", at = @At("HEAD"), cancellable = true)
	private static void getType(int type, CallbackInfoReturnable<BoatEntity.Type> cir) {
		int n = BoatEntity.Type.values().length - $TYPES.size();
		if (type >= n) cir.setReturnValue($TYPES.get(type - n));
	}
}