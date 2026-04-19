package com.offhandtweaks.util;

import com.offhandtweaks.OffhandTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.core.registries.Registries;

/**
 * Classifies an offhand {@link ItemStack} into one of the configurable categories.
 *
 * Ordering matters: the first matching category wins. A shield is never food, food
 * is never a light source, etc. Items that don't match any concrete category fall
 * into {@link Category#OTHER} so the "allowOtherBlocksRMB" toggle covers the long tail.
 */
public final class ItemCategorizer {

    /** Datapack tag for light-source items. Users / modpacks can extend this. */
    public static final TagKey<Item> LIGHT_SOURCES = TagKey.create(
            Registries.ITEM,
            new ResourceLocation(OffhandTweaks.MODID, "light_sources")
    );

    public enum Category {
        SHIELD,
        FOOD,
        LIGHT_SOURCE,
        OTHER,
        NONE
    }

    private ItemCategorizer() {}

    public static Category classify(ItemStack stack) {
        if (stack.isEmpty()) return Category.NONE;

        Item item = stack.getItem();

        // 1. Shield — covers vanilla + modded shields (anything that extends ShieldItem).
        if (item instanceof ShieldItem) {
            return Category.SHIELD;
        }

        // 2. Food — uses Forge's edibility check (null-safe for items with no FoodProperties).
        if (item.isEdible()) {
            return Category.FOOD;
        }

        // 3. Light sources — tag-driven so modpacks can extend without recompiling.
        if (stack.is(LIGHT_SOURCES)) {
            return Category.LIGHT_SOURCE;
        }

        // 4. Anything else: placeable blocks, tools, etc.
        //    We return OTHER for any non-empty stack so "allowOtherBlocksRMB=false" is a blanket off-switch.
        if (item instanceof BlockItem) {
            return Category.OTHER;
        }

        // Non-block items (tools, potions, etc.) are still "other" as far as the config is concerned.
        return Category.OTHER;
    }
}
