package com.mod.mixin;

import com.mod.registry.ItemRegister;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemGroups.class)
public abstract class ItemGroupsMixin {
    /*
    this is the COOL CHAD way to add the boats to the item group right after the rest of the boats
    this is an alternative method to avoid the experimental FabricItemGroupEntries#addAfter lol

    @Inject(method = "method_51328", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup$Entries;add(Lnet/minecraft/item/ItemConvertible;)V", ordinal = 74))
    private static void method_51328(ItemGroup.DisplayContext displayContext, ItemGroup.Entries entries, CallbackInfo ci) {
        entries.add(ItemRegister.CRIMSON_BOAT);
        entries.add(ItemRegister.CRIMSON_CHEST_BOAT);
        entries.add(ItemRegister.WARPED_BOAT);
        entries.add(ItemRegister.WARPED_CHEST_BOAT);
    }
     */
}
