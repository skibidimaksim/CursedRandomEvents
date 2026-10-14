package com.skibidimaksim.cursedrandomevents;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CursedItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CursedRandomEvents.MOD_ID);

    public static final DeferredItem<Item> CURSED_MULTI_TOOL =
            ITEMS.registerItem(
                    "cursed_multi_tool",
                    Item::new,
                    new Item.Properties()
                            .durability(Tiers.NETHERITE.getUses())
            );

    private CursedItems() {}
}
