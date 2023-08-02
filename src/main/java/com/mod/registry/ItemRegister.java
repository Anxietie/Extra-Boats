package com.mod.registry;

import com.mod.entity.ExtendedBoatEntityType;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;

import static com.mod.ExtraBoats.MODID;

public class ItemRegister {
    private static final Collection<ItemStack> ITEMS = new ArrayList<>();

    public static final Item CRIMSON_BOAT = new BoatItem(false, ExtendedBoatEntityType.CRIMSON, new Item.Settings().maxCount(1));
    public static final Item CRIMSON_CHEST_BOAT = new BoatItem(true, ExtendedBoatEntityType.CRIMSON, new Item.Settings().maxCount(1));
    public static final Item WARPED_BOAT = new BoatItem(false, ExtendedBoatEntityType.WARPED, new Item.Settings().maxCount(1));
    public static final Item WARPED_CHEST_BOAT = new BoatItem(true, ExtendedBoatEntityType.WARPED, new Item.Settings().maxCount(1));

    /*
    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(BEACON))
            .entries((context, entries) -> {
                entries.addAll(ITEMS);
            })
            .displayName(Text.translatable("itemGroup.anx.item_group"))
            .build();
    */

    public static void registerItems() {
        registerItem("crimson_boat", CRIMSON_BOAT);
        registerItem("crimson_chest_boat", CRIMSON_CHEST_BOAT);
        registerItem("warped_boat", WARPED_BOAT);
        registerItem("warped_chest_boat", WARPED_CHEST_BOAT);
    }

    private static void registerItem(String id, Item item) {
        Registry.register(Registries.ITEM, new Identifier(MODID, id), item);
        ITEMS.add(new ItemStack(item));
    }

    public static void registerItemGroups() {
        // registerItemGroup("item_group", ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> content.addAfter(Items.BAMBOO_CHEST_RAFT, ITEMS)); // cringe experimental way
    }

    private static void registerItemGroup(String id, ItemGroup group) {
        Registry.register(Registries.ITEM_GROUP, new Identifier(MODID, id), group);
    }
}
